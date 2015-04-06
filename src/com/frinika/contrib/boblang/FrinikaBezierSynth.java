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

import java.io.Serializable;

import com.frinika.synth.Oscillator;
import com.frinika.synth.SynthRack;
import com.frinika.synth.Synth;

/**
 * @author Peter Johan Salomonsen
 */
public class FrinikaBezierSynth extends Synth{

    BezierWaves bezierWaves = new BezierWaves("Bezier synth - Copyright (c) Bob Lang");
    BezierEnvelopeShaper envelopeShaper;
    
    public FrinikaBezierSynth(SynthRack synth) {
        super(synth);
        bezierWaves.frinikaStartUp(this);
    }

    public BezierWaves getBezierWaves()
    {
        return bezierWaves;
    }
    
    @Override
    public Serializable getSettings() {
        return (Serializable) bezierWaves.p;
    }
    
    @Override
    public void loadSettings(Serializable settings) {
        bezierWaves.p = (BezierParams)settings; 
        changePatch(bezierWaves.p);
    }
    
    public void noteOn(int noteNumber, int velocity) {
        Oscillator voice = new FrinikaBezierVoice(this,noteNumber);
        voice.setNoteNumber(noteNumber);
        voice.setVelocity(velocity);
        addOscillator(noteNumber,voice);
    }

    public BezierEnvelopeShaper getEnvelopeShaper() {
        return envelopeShaper;
    }

    public void changePatch(BezierParams p) {
        envelopeShaper = new BezierEnvelopeShaper(p);        
    }
    
    @Override
    public void showGUI() {
        bezierWaves.setVisible(true);
    }
}
