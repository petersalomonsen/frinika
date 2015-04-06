/*
 * Created on Sep 23, 2004
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

import com.frinika.voiceserver.Voice;
import com.frinika.voiceserver.VoiceInterrupt;
import com.frinika.synth.envelope.MidiVolume;

/**
 * @author peter
 *
 */
public abstract class Oscillator extends Voice{
	protected float frequency;
	protected float increment;
	protected float level;
	protected float position = 0;
	protected int sampleRate;
	protected boolean release = false;
	protected boolean triggeredRelease = false;
        protected Synth synth;
	
	public Oscillator(Synth synth)
	{
            this.synth = synth;
            sampleRate = synth.getFrinikaSynth().samplerate;
	}
	
	public void setNoteNumber(int noteNumber)
	{
		frequency = getFrequency(noteNumber);
		updateIncrement();
	}
	
	public final static float getFrequency(int noteNumber)
	{
		return((float)(440.0 * Math.pow(2.0,((noteNumber-69.0)/12.0))) );
	}
	
	private final static float getIncrement(float frequency, int sampleRate)
	{
		return((float)((2.0 * Math.PI * frequency) / (sampleRate * 1.0)));
	}
	
	protected void updateIncrement()
	{
		increment = getIncrement(frequency,sampleRate);	
	}
	
    public void setVelocity(int velocity)
    {
        level = MidiVolume.midiVolumeToAmplitudeRatio(velocity);
    }
    
	public void release()
	{
        if(!triggeredRelease)
		{
            synth.getAudioOutput().interruptTransmitter(this,new VoiceInterrupt()

				{
					public void doInterrupt() {
						release = true;				
					}
			
				});
            triggeredRelease = true;
        }
	}
}
