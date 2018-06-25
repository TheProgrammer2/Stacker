/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author MikaF
 */
public class AudioPlayer {
    
    public static final String PATH = System.getProperty("user.dir") + File.separator +
            "src" + File.separator +
            "res" + File.separator;
    
    public static HashMap<String,Thread> threads = new HashMap<>();
    private static List<String> loopQueue = new LinkedList<>();
    private static boolean looping = false;
    private static HashMap<String,Boolean> loopReset = new HashMap<>();
    private static String loopingAudio;
    private static String currentAudio;
    private static boolean loopInterrupt = false;
    private static boolean loopRunning = false;
    
    private static Thread queueThread;
    private static boolean handleQueue = true;
    
    static {
        queueThread = new Thread(new Runnable() {
            public void run() {
                while(handleQueue) {
                    if(loopQueue.size() > 0) {
                        softLoopEnd();
                        if(!loopRunning) {
                            playLoopAsync(loopQueue.get(0));
                            loopQueue.remove(0);
                        }
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) { }
                }
            }
        });
        queueThread.start();
    }
    
    public static void softLoopEnd() {
        looping = false;
        loopReset.put(loopingAudio, false);
    }
    
    public static void hardLoopEnd() {
        looping = false;
        loopInterrupt = true;
        loopReset.put(loopingAudio, false);
    }
    
    public static void playLoopAsync(String filename) {
        if(loopRunning) {
            loopQueue.add(filename);
            return;
        }
        loopingAudio = filename;
        loopRunning = true;
        looping = true;
        loopReset.put(filename, true);
        Thread loopThread = new Thread(new Runnable() {
            public void run() {
                while(looping) {
                    if(loopReset.get(filename)) {
                        playAsync(filename);
                        loopReset.put(filename, false);
                    }
                    if(loopInterrupt) {
                        endAudio(filename);
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) { }
                }
            }
        });
        loopThread.start();
    }
    
    public static void endAllAudio() {
        while(threads.keySet().size() > 0) {
            try {
                for(String audio : threads.keySet()) {
                    threads.get(audio).interrupt();
                    threads.remove(audio);
                }
            } catch(ConcurrentModificationException e) { }
        }
        
    }
    
    public static void endAudio(String filename) {
        if(threads.containsKey(filename)) {
            threads.get(filename).interrupt();
            threads.remove(filename);
        }
    }
    
    public static void playSync(String filename) {
        File audio = new File(PATH + filename + ".wav");
        if(audio.exists())
            playAudio(audio);
        else
            System.out.println("Unknown audio file: " + filename);
    }
    
    public static void playAsync(String filename) {
        currentAudio = filename;
        File audio = new File(PATH + filename + ".wav");
        if(audio.exists()) {
            Thread audioThread = new Thread(new Runnable() {
                public void run() {
                    playAudio(audio);
                    threads.remove(filename);
                    currentAudio = loopingAudio;
                }
            });
            threads.put(filename, audioThread);
            audioThread.start();
        } else
            System.out.println("Unknown audio file: " + filename);
    }
    
    private static void playAudio(File audio) {
        AudioListener listener = new AudioListener();
        AudioInputStream stream = null;
        try {
            stream = AudioSystem.getAudioInputStream(audio);
        } catch(IOException e) {
            System.out.println("Error playing sound: " + e.getMessage());
        } catch(UnsupportedAudioFileException e) {
            System.out.println("Unsupported audio file: " + e.getMessage());
        }
        if(stream == null)
            return;
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(listener);
            clip.open(stream);
            clip.start();
            while(!listener.isDone()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) { }
            }
            clip.stop();
        } catch(LineUnavailableException e) {
            System.out.println("Line Listener unavailable: " + e.getMessage());
        } catch(IOException e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
        if(audio.getName().contains(loopingAudio))
            loopReset.put(loopingAudio, true);
        loopRunning = false;
    }
    
}
