/*
 * Created on Nov 26, 2004
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

import com.frinika.voiceserver.VoiceInterrupt;
import com.frinika.synth.waveforms.Sinus;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class PreOscillator extends Oscillator {
	int amount = 0;
	float[] lfoBuffer;
	public float[] sampleBuffer = null;

	public int pitchBend;
	public float pitchBendFactor = 1;

	float lastLfoSample = 0;
	
	public PreOscillator(Synth synth)
	{
		super(synth);
		frequency = 7;
		updateIncrement();
	}
	
	public void setVibratoAmount(final int amount)
	{
		synth.getAudioOutput().interruptTransmitter(this, new VoiceInterrupt() {

			public void doInterrupt() {
				PreOscillator.this.amount = amount;
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see com.petersalomonsen.mystudio.mysynth.Oscillator#updateIncrement()
	 */
	protected void updateIncrement() {
		increment = (float) ((float)(2.0 * Math.PI * frequency )/ (float)sampleRate);
	}
	
	public final float[] getLfoBuffer()
	{
		return lfoBuffer;
	}
	
	public void setVibratoFrequency(final float frequency)
	{
		synth.getAudioOutput().interruptTransmitter(this, new VoiceInterrupt() {

			public void doInterrupt() {
				PreOscillator.this.frequency = frequency;
				updateIncrement();
			}
		});		
	}
	/* (non-Javadoc)
	 * @see com.petersalomonsen.mystudio.audio.IAudioOutputGenerator#fillBuffer(int, int, float[])
	 */
	public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
		if(sampleBuffer==null || sampleBuffer.length != buffer.length)
		{
			lfoBuffer = new float[buffer.length / 2];
			sampleBuffer = new float[buffer.length];
		}
		
		for(int n=(startBufferPos/2);(n<endBufferPos/2);n++)
		{
			if(amount!=0)
				lfoBuffer[n] = PitchCents.getPitchCent((int)(Sinus.getSin(position+=increment)*amount)) * pitchBendFactor;
			else
				lfoBuffer[n] = pitchBendFactor;
			
			sampleBuffer[n*2]=0;
			sampleBuffer[n*2+1]=0;
		} 
	}

	/**
	 * @return
	 */
	public final int getAmount() {
		return amount;
	} 

}
