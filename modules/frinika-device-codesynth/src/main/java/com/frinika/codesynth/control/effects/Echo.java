/*
 * Created on Mar 24, 2016
 *
 * Copyright (c) 2004-2016 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.codesynth.control.effects;

import com.frinika.codesynth.SynthContext;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class Echo {
    float echoBuffer[] = null;
    int echoBufferPos = 0;
    float echoAmount = 0;
    int echoLength = 48;

    SynthContext synthContext;
    
    public Echo(SynthContext synthContext) {
	this.synthContext = synthContext;
    }
        
    
    public void fillBuffer(float[] floatBuffer,int numberOfFrames,int channels) {
	if(echoBuffer!=null) {
	    for(int n=0;n<numberOfFrames*channels;n++) {
		float sample = floatBuffer[n];

		echoBufferPos %= echoBuffer.length;

		echoBuffer[echoBufferPos]*=echoAmount;
		float echoBufferSample = echoBuffer[echoBufferPos]; 

		echoBuffer[echoBufferPos] += sample;	   
		sample+=echoBufferSample;

		echoBufferPos++;
		
		floatBuffer[n] = sample;
	    }
	}	
    }
    
    /**
    * @param echoAmount
    */
    public void setEchoAmount(int echoAmount) {	   	
	 this.echoAmount = echoAmount / 127f;	   
    }

    public void setEchoLength(int echoLength)
    {
	if(echoLength==0) {
	    echoLength = this.echoLength;
	}
	if(echoBuffer==null || echoLength!=this.echoLength) {
	    
	    this.echoLength = echoLength;

	    float ticksPerSecond = (synthContext.getTempoBPM() / 60f) * 32;
	    int echoBufferSize = (int)((echoLength / ticksPerSecond) * synthContext.getSampleRate());
	    if(echoBufferSize>0) {
		echoBuffer = new float[echoBufferSize];					
	    } else {		  			
		echoBuffer = null;
	    }
	}
    }    
}
