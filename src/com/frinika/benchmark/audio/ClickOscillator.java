/*
 * Created on Dec 14, 2004
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
package com.frinika.benchmark.audio;

import com.frinika.benchmark.SyncVoice;
import com.frinika.sequencer.FrinikaSequencer;

/**
 * Output a impulse every period.
 * 
 * @author pjl
 * 
 */
public class ClickOscillator extends SyncVoice {


	
	private int periodInSamples;

	long clickFrame; 

	public ClickOscillator(FrinikaSequencer seq,int period) {
		super(seq);
		this.periodInSamples=period;
	}


	// HOW DO I KNOW WAT THE REAL TIME OF buffer is 
	public synchronized void fillBuffer(int startBufferPos, int endBufferPos,
			float[] buffer) {

		if (!sequencer.isRunning()) {
			return;
		}
	
		// HOW DO I KNOW IF OUTPUT HAS GLITCHED ?
		doSync(0);
		
		for (int i = startBufferPos; i < endBufferPos; i += 2) {
			framePtr++ ;
			float val;
			
			// Fairly nasty thing to do to an analog device !!!!!
			if (framePtr % periodInSamples == 0 ) {
				val= 1.0f;
				clickFrame=framePtr;
			}
			else val=0.0f;
			
			buffer[i]     += val;
			buffer[i + 1] += val;
		}
	}
	
	/**
	 * 
	 *  We only use framePtr which is already corrected by doSync
	 *
	 */
	protected void correctGlitch() {}
}
