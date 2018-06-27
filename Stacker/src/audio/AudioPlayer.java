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
    
    private static HashMap<String,Thread> threads = new HashMap<>();
    private static HashMap<String,Clip> clips = new HashMap<>();
    private static List<String> loopQueue = new LinkedList<>();
    private static boolean looping = false;
    private static boolean loopReset = false;
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
        loopReset = false;
    }
    
    public static void hardLoopEnd() {
        loopInterrupt = true;
        loopReset = false;
    }
    
    public static void playLoopAsync(String filename) {
        if(loopRunning) {
            loopQueue.add(filename);
            return;
        }
        loopRunning = true;
        looping = true;
        loopReset = true;
        Thread loopThread = new Thread(new Runnable() {
            public void run() {
                while(looping) {
                    if(loopReset) {
                        playAsync(filename);
                        loopReset = false;
                    }
                    if(loopInterrupt) {
                        System.out.println("ended");
                        looping = false;
                        break;
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
                    clips.get(audio).stop();
                    threads.remove(audio);
                    clips.remove(audio);
                }
            } catch(ConcurrentModificationException e) { }
        }
        
    }
    
    public static void endAudio(String filename) {
        if(threads.containsKey(filename)) {
            threads.get(filename).interrupt();
            clips.get(filename).stop();
            threads.remove(filename);
            clips.remove(filename);
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
        File audio = new File(PATH + filename + ".wav");
        if(audio.exists()) {
            Thread audioThread = new Thread(new Runnable() {
                public void run() {
                    playAudio(audio);
                    threads.remove(filename);
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
            clips.put(audio.getName().split("\\.")[0], clip);
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
        loopReset = true;
        loopRunning = false;
    }
    
}