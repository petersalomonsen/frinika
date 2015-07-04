/*
 * Created on Dec 9, 2004
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

import com.frinika.synth.envelope.MidiVolume;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class PostOscillator extends Oscillator {
	int overDriveAmount = 0;
	float echoAmount = 0;
	int echoLength = 48;
	float volume = MidiVolume.midiVolumeToAmplitudeRatio(100);
	
	float echoBuffer[] = null;
	int echoBufferPos = 0;
	
    Pan pan = new Pan(0.5f);
	float reverb = MidiVolume.midiVolumeToAmplitudeRatio(0);
        
    
	/**
	 * @param synth
	 */
	public PostOscillator(Synth synth) {
		super(synth);		
	}

	/* (non-Javadoc)
	 * @see com.petersalomonsen.mystudio.audio.IAudioOutputGenerator#fillBuffer(int, int, float[])
	 */
	public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
        // -------------------------- For debugging ------------------------------
	    // Need to find out whether it is preOscillator or preOscillator.sampleBuffer == null
        // Ref. bug http://sourceforge.net/tracker/index.php?func=detail&aid=1450319&group_id=131823&atid=722343
	    
        if(synth.preOscillator == null)
        {
        		System.out.println("FATAL: preOscillator is null - please add this line as comment to http://sourceforge.net/tracker/index.php?func=detail&aid=1450319&group_id=131823&atid=722343");
        		return;
        }
        if(synth.preOscillator.sampleBuffer == null)
        {
        		System.out.println("FATAL: preOscillator.sampleBuffer is null - please add this line as comment to http://sourceforge.net/tracker/index.php?func=detail&aid=1450319&group_id=131823&atid=722343");
        		return;
        }
        		
        // -----------------------------------------------------------------------
       
        for(int n=startBufferPos;n<endBufferPos;n++)
		{
            float sample = synth.preOscillator.sampleBuffer[n];
			
            // Overdrive
            
			if(overDriveAmount>0)
				sample = Overdrive.process(synth.preOscillator.sampleBuffer[n],overDriveAmount);
			
			// Echo
			
			if(echoBuffer!=null)
			{
				echoBufferPos %= echoBuffer.length;
				
				echoBuffer[echoBufferPos]*=echoAmount;
				float echoBufferSample = echoBuffer[echoBufferPos]; 
                                if(echoBufferPos%2==0) {
                                    echoBuffer[echoBufferPos] += sample;
                                } else {
                                    // Invert one channel to get stereo effect
                                    echoBuffer[echoBufferPos] += sample;
                                }
				sample+=echoBufferSample;

				echoBufferPos++;
			}
			
			// Gain
			sample *= volume;
            
			// Pan
			
            if((n & 0x01) == 0)
                sample *= pan.getLeftLevel();
            else
                sample *= pan.getRightLevel();
			
            synth.preOscillator.sampleBuffer[n]=sample * reverb;
            buffer[n] += sample;
		}
    
        if(reverb>0)
        	MasterVoice.getDefaultInstance().addToReverb(startBufferPos, endBufferPos, synth.preOscillator.sampleBuffer);
	}

	/**
	 * @param Overdrive amount
	 */
	public void setOverDriveAmount(int overDriveAmount) {
		this.overDriveAmount = overDriveAmount;
	}

	/**
	 * @param volume
	 */
	public void setVolume(float volume) {
		this.volume = volume;
	}

	public void setReverb(float reverb) {
		this.reverb = reverb;
	}
	/**
	 * @param echoAmount
	 */
	public void setEchoAmount(int echoAmount) {
		if(echoAmount>0)
                {	
                    //TODO: Resolve tempo from midi messages
			float ticksPerSecond = (synth.getFrinikaSynth().getTempoBPM() / 60f) * 32;
			if(echoBuffer==null)
                setEchoLength(echoLength);
		}
		else
			echoBuffer = null;
			
		this.echoAmount = echoAmount / 127f;
	}

	public void setEchoLength(int echoLength)
	{
            this.echoLength = echoLength;

            float ticksPerSecond = (synth.getFrinikaSynth().getTempoBPM() / 60f) * 32;
            int echoBufferSize = (int)((echoLength / ticksPerSecond) * sampleRate);
            if(echoBufferSize>0)
                echoBuffer = new float[echoBufferSize];
            else
                echoBuffer = null;
	}
    
    /**
     * Set pan amount 
     * @param position MIDI cc10 value (0-127)
     */
    public void setPan(int position)
    {
        pan = new Pan((float)position/127f);
    }
}
