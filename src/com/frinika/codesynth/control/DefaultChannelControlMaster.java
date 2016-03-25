
/*
 *
 * Copyright (c) 2016 Peter Johan Salomonsen ( http://petersalomonsen.com )
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
package com.frinika.codesynth.control;

import com.frinika.codesynth.control.effects.Echo;
import com.frinika.synth.envelope.MidiVolume;
import rasmus.interpreter.sampled.util.Freeverb;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class DefaultChannelControlMaster extends ChannelControlMaster {
    Freeverb freeverb;
    Echo echo;
    
    final void createEffects() {
	if(this.midiChannel!=null) {
	    if(freeverb==null) {
		freeverb = new Freeverb(this.midiChannel.getSynth().getFormat().getSampleRate(),1);
		freeverb.setdry(0);
		freeverb.setroomsize(0.9f);				
		freeverb.setdamp(0.9f);	
		freeverb.setwet(1);
	    }
	    if(echo==null) {
		echo = new Echo(midiChannel.getSynth());
	    }
	}
    }
    
    @Override
    public final void fillFrameBeforeNotes(float[] floatBuffer, int bufferPos, int channels) {
	
    }

    @Override
    public void fillBufferAfterNotes(final float[] floatBuffer, final int numberOfFrames, final int channels) {
	createEffects();
	
	echo.setEchoAmount(this.midiChannel.getController(22));
	echo.setEchoLength(this.midiChannel.getController(23));
	echo.fillBuffer(floatBuffer, numberOfFrames, channels);
	
	float midiVolRatio = MidiVolume.midiVolumeToAmplitudeRatio(this.midiChannel.getController(7));
	float reverbRatio = MidiVolume.midiVolumeToAmplitudeRatio(this.midiChannel.getController(91));
	
	double[] reverbIn = new double[numberOfFrames*channels];
	double[] reverbOut = new double[numberOfFrames*channels];
	
	for(int n=0;n<numberOfFrames*channels;n++) {
	    floatBuffer[n]*=midiVolRatio;	    
	    reverbIn[n] = floatBuffer[n];
	}			
		
	freeverb.processReplace(reverbIn, reverbOut , 0, numberOfFrames*channels, channels);	
			
	for(int n=0;n<numberOfFrames*channels;n++) {
	    floatBuffer[n] += reverbOut[n]*reverbRatio;	
	}
	
	
    }
    
    
    @Override
    public final void fillFrameAfterNotes(float[] floatBuffer, int bufferPos, int channels) {
	// Not used since fillBufferAfterNotes is overridden
    }
    
}
