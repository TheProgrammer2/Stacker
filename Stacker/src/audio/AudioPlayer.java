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
        File audio = new File(PATH + filename + ".wav");
        if(audio.exists()) {
            Thread audioThread = new Thread(new Runnable() {
                public void run() {
                    playAudio(audio);
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
            try {
                clip.start();
                listener.waitUntilDone();
            } catch(InterruptedException e) {
                System.out.println("Interrupted playing sound: " + e.getMessage());
            }
            finally {
                clip.close();
            }
        } catch(LineUnavailableException e) {
            System.out.println("Line Listener unavailable: " + e.getMessage());
        } catch(IOException e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }
    
}
