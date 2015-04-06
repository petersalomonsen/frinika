/*
 * Created on Apr 20, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

package com.frinika.sequencer.model.notation;

import com.frinika.notation.NotationGraphics;
import com.frinika.sequencer.model.EditHistoryRecordable;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NotationEvent;

public class ClefChange extends NotationEvent {
	
	private static final long serialVersionUID = 1L;
	
	// Clef Presets (most common clefs)	
	// Treble   :   clef_type = G,  clef_pos =  2,  clef_octave = 0
	// Bass     :   clef_type = F,  clef_pos =  6,  clef_octave = 0
    // Alto     :   clef_type = C,  clef_pos =  4,  clef_octave = 0
    // Tenor    :   clef_type = C,  clef_pos =  6,  clef_octave = 0
	
	//                            clef_pos = 10
	//      / \      
	//      |  |    ----------    clef_pos = 8
    //      \ /
    //      /       ----------    clef_pos = 6
    //    /  |
    //  /   _|_     ----------    clef_pos = 4
    // |   / | \
    // |  |  |  |   ----------    clef_pos = 2
    // |   \ |  | 
    //  \_____/     ----------    clef_pos = 0
	//       |
	//    \_/                     clef_pos  = -2
	//
	
	public int clef_type = NotationGraphics.CLEF_G;
	public int clef_pos  = 2;	
	public int clef_octave = 0;    

	public ClefChange(MidiPart part, long startTick) {
		super(part, startTick);
	}
	
	public void restoreFromClone(EditHistoryRecordable object) {
		ClefChange evt=(ClefChange)object;
		this.clef_type = evt.clef_type;
		this.clef_pos = evt.clef_pos;
		this.clef_octave = evt.clef_octave;
	}	

}
