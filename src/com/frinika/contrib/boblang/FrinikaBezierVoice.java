/*
 * Created on Nov 26, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.contrib.boblang;

import com.frinika.voiceserver.VoiceInterrupt;
import com.frinika.synth.Oscillator;

/**
 * @author Peter Johan Salomonsen
 */
public class FrinikaBezierVoice extends Oscillator {

    BezierPlayingNote bezierPlayingNote;
    FrinikaBezierSynth synth;
    
    public FrinikaBezierVoice(FrinikaBezierSynth synth, int noteNumber) {
        super(synth);
        this.synth = synth;
        
        BezierSynth bezierSynth = new BezierSynth (noteNumber, synth.getBezierWaves().p);
        bezierPlayingNote = new BezierPlayingNote(bezierSynth,
                                                  synth.getEnvelopeShaper(),
                                                  noteNumber,
                                                  1);
    }

    public void release()
    {
        if(!triggeredRelease)
        {
            synth.getAudioOutput().interruptTransmitter(this,new VoiceInterrupt()

                {
                    public void doInterrupt() {
                        release = true;
                        bezierPlayingNote.setRelease();
                    }
            
                });
            triggeredRelease = true;
        }
    }
    
    @Override
    public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
        bezierPlayingNote.getBuffer(synth.getPreOscillator().sampleBuffer,startBufferPos,endBufferPos);
        if(bezierPlayingNote.isFinished())
            synth.getAudioOutput().removeTransmitter(this);
    }

}
