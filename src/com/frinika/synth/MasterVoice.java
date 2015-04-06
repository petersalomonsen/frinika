/*
 * Created on Okt 29, 2005
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
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth;

import rasmus.interpreter.sampled.util.Freeverb;

import com.frinika.voiceserver.Voice;
import com.frinika.voiceserver.VoiceServer;

/**
 * Default master effects for Frinika synthrack
 * 
 * Effects:
 * 	- Reverb
 * 
 * @author Peter Johan Salomonsen
 */
public class MasterVoice extends Voice {
	Freeverb freeverb = null;
	
    double reverbBufferIn[] = null;
    double reverbBufferOut[] = null;
    
    static MasterVoice defaultInstance = new MasterVoice();

    public final void addToReverb(int startBufferPos, int endBufferPos,float[] buffer)
    {
    	if(freeverb!=null && (reverbBufferIn == null || reverbBufferIn.length != buffer.length))
        {
        	reverbBufferIn = new double[buffer.length];
        	reverbBufferOut = new double[buffer.length];
        }
    	for(int n=startBufferPos;n<endBufferPos;n++)
    		reverbBufferIn[n] += buffer[n];
    }
	
	@Override
	public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
		if(reverbBufferIn != null)
		{
			freeverb.processReplace(reverbBufferIn, reverbBufferOut , startBufferPos, endBufferPos, 2);
			
			for(int n=startBufferPos;n<endBufferPos;n++)
				buffer[n] += (float)reverbBufferOut[n];
			
			//Clean up reverbBufferIn
			java.util.Arrays.fill(reverbBufferIn,0);
		}
    }
    
    public final void initialize(VoiceServer voiceServer){
    	freeverb = new Freeverb(voiceServer.getSampleRate(),1);
		freeverb.setdry(0);
		freeverb.setroomsize(0.9f);				
		freeverb.setdamp(0.9f);	
		freeverb.setwet(1);
		
		voiceServer.addTransmitter(this);
    }
    
    public static final MasterVoice getDefaultInstance()
    {
    	return defaultInstance;
    }
}
