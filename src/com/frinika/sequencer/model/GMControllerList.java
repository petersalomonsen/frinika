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

import java.util.Vector;

import javax.sound.midi.ShortMessage;

import com.frinika.sequencer.gui.pianoroll.ControllerHandle;

public class GMControllerList implements ControllerListProvider {
	
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    static ControllerHandle[] cntrls;

	static {
		Vector<ControllerHandle> tmp=new Vector();
	
		tmp.add(new ControllerHandle("Velocity", 0, 127, 0,ShortMessage.NOTE_ON));
		tmp.add(new ControllerHandle("Modulation", 0, 127, 1,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Portamento Time", 0, 127, 5,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Portamento ON/OFF", 0, 127, 65,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Portamento control", 0, 127, 84,ShortMessage.CONTROL_CHANGE));	


		tmp.add(new ControllerHandle("Volume", 0, 127, 7,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Pan", 0, 127, 10,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Expression", 0, 127, 11,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Sostenuto", 0, 127, 66,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Soft Pedal", 0, 127, 67,ShortMessage.CONTROL_CHANGE));

		
		tmp.add(new ControllerHandle("Filter Freq.", 0, 127, 74,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Filter Q", 0, 127, 71,ShortMessage.CONTROL_CHANGE));	
		tmp.add(new ControllerHandle("Attack", 0, 127, 73,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Decay", 0, 127, 75,ShortMessage.CONTROL_CHANGE));
		
		tmp.add(new ControllerHandle("Vibrato Rate", 0, 127, 76,ShortMessage.CONTROL_CHANGE));	
		tmp.add(new ControllerHandle("Vibrato Depth", 0, 127, 77,ShortMessage.CONTROL_CHANGE));	
		tmp.add(new ControllerHandle("Vibrato Delay", 0, 127, 78,ShortMessage.CONTROL_CHANGE));	

		tmp.add(new ControllerHandle("Release", 0, 127, 72,ShortMessage.CONTROL_CHANGE));	
		tmp.add(new ControllerHandle("Reverb", 0, 127, 91,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Tremelo", 0, 127, 92,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Chorus", 0, 127, 93,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Varation.", 0, 127, 94,ShortMessage.CONTROL_CHANGE));		
		tmp.add(new ControllerHandle("Sustain", 0, 127, 64,ShortMessage.CONTROL_CHANGE));
		tmp.add(new ControllerHandle("Pitch Bend", -8192, 16383-8192 , 64,ShortMessage.PITCH_BEND));
		
		
		
		cntrls =new ControllerHandle[tmp.size()];
		tmp.toArray(cntrls);
	}
		
	public Object[] getList() {
		return cntrls;
	}
}
