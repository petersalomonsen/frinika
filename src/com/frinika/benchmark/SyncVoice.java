/*
 * Created on Jun 27, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

package com.frinika.benchmark;

import com.frinika.voiceserver.Voice;
import com.frinika.global.FrinikaConfig;
import com.frinika.sequencer.FrinikaSequencer;

public abstract class SyncVoice extends Voice{

	public long realStartTime;
	
	protected long framePtr;   // frames from start of sample
	protected boolean glitched = true;
	protected FrinikaSequencer sequencer;

	protected SyncVoice(FrinikaSequencer seq) {
		this.sequencer = seq;
		framePtr=0;
	
	}
	
	protected boolean doSync(int tol) {	
		long playTime = sequencer.getMicrosecondPosition();
	
		
		final long realFramePtr = (long) (((playTime-realStartTime) * FrinikaConfig.sampleRate) / 1000000);
		
		long glitch = realFramePtr - framePtr;
		//stem.out.println("  GLITCH   tol" + glitch  + "   "  + tol ) ;
		if ( Math.abs(glitch) > tol) {
			framePtr = realFramePtr;
			System.out.println(" Correct " + (glitch));
			correctGlitch();		
		}
		return true;
	}


	/** 
	 * Called when framePtr goes out of sync
	 *
	 */
	abstract protected void correctGlitch();
}
