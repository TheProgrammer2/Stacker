/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audio;

import javax.sound.sampled.*;
import javax.sound.sampled.LineEvent.Type;

/**
 *
 * @author MikaF
 */
public class AudioListener implements LineListener {

    private boolean done = false;

    public boolean isDone() {
        return done;
    }
    
    @Override
    public void update(LineEvent event) {
        Type eventType = event.getType();
        if(eventType == Type.STOP)
            done = true;
    }
    
}
