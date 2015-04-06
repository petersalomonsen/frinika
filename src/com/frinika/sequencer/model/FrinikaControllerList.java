/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
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
package com.frinika.sequencer.model;

import javax.sound.midi.ShortMessage;

import com.frinika.sequencer.gui.pianoroll.ControllerHandle;

public class FrinikaControllerList implements ControllerListProvider {
	
	/**
     * 
     */
  

    static ControllerHandle[] cntrls;

	static {
		cntrls = new ControllerHandle[8];
		cntrls[0] = new ControllerHandle("Velocity", 0, 127, 0,ShortMessage.NOTE_ON);
		cntrls[1] = new ControllerHandle("Vibrato", 0, 127, 1,ShortMessage.CONTROL_CHANGE);
		cntrls[2] = new ControllerHandle("Volume", 0, 127, 7,ShortMessage.CONTROL_CHANGE);
		cntrls[3] = new ControllerHandle("Pan", 0, 127, 10,ShortMessage.CONTROL_CHANGE);
		cntrls[4] = new ControllerHandle("Distortion", 0, 127, 20,ShortMessage.CONTROL_CHANGE);
		cntrls[5] = new ControllerHandle("Echo", 0, 127, 22,ShortMessage.CONTROL_CHANGE);
		cntrls[6] = new ControllerHandle("Echo length", 0, 127, 23,ShortMessage.CONTROL_CHANGE);
		cntrls[7] = new ControllerHandle("Sustain", 0, 127, 64,ShortMessage.CONTROL_CHANGE);
		cntrls[7] = new ControllerHandle("Pitch Bend", -8192, 16383-8192 , 64,ShortMessage.PITCH_BEND);

	}

	public Object[] getList() {
		return cntrls;
	}
	

}
