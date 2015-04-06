/*
 * Created on Feb 6, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

package com.frinika.sequencer.model;

import com.frinika.global.FrinikaConfig;

/**
 * Data-record for providing options on how to play a MidiLane.
 * 
 * The data-fields of this class are directly accessed, so they are public. 
 * 
 * @see MidiLane.playOptions
 * @see com.frinika.sequencer.FrinikaSequencerPlayer
 * @author Jens Gulden
 */
public class MidiPlayOptions implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	public boolean muted = false;
	
	public boolean looped = false;
	
        // replace 128 with TICKS_PER_QUARTER
	public long loopedTicks = 16 * FrinikaConfig.TICKS_PER_QUARTER ; // 16 beats by default
	
	public long shiftedTicks = 0;
	
	public int velocityOffset = 0;
	
	public float velocityCompression = 0.0f;
	
	public int transpose = 0;
	
	public boolean preRendered = false; // A flag, that this track may be pre-rendered if available/possible
	
	transient public boolean preRenderedUsed = false; // if true, then sequencer should not play this track	( it's pre-rendered )                                                  // 

	public boolean quantizationActive = false;
	
	public Quantization quantization = new Quantization();
	
	// PJL
	
	public boolean drumMapped = false;   // if true then map the note numbers using noteMap
   	
	public int noteMap[];
}
