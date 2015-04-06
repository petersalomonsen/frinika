/*
 * Created on 2 Apr 2008
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

package com.frinika.sequencer.model.tools;

import java.util.Vector;

import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;

public class Tools {

	public static Vector<MidiPart> splitParts(Vector<MidiPart> selected,
			long ticksPerBeat) {
		
		Vector<MidiPart> newParts = new Vector<MidiPart>();

		for (MidiPart part : selected) {

			MidiLane lane = (MidiLane) part.getLane();
			lane.remove(part);

			MidiPart newPart = null; // new MidiPart((MidiLane) lane);

			long gap = ticksPerBeat * 2;
			long lastTick = -1;

			newPart = new MidiPart((MidiLane) lane);
			newParts.add(newPart);

			for (MultiEvent ev : part.getMultiEvents()) {

				long t1 = ev.getStartTick();
				if (lastTick == -1)
					lastTick = ev.getEndTick();

				if (t1 - lastTick > gap) {
					newPart.setBoundsFromEvents();
					newPart.commitEventsAdd();
					newPart = new MidiPart((MidiLane) lane);
					newParts.add(newPart);
					lastTick = ev.getEndTick();
				}

				newPart.add(ev);
			}
			if (newPart != null) {
				newPart.setBoundsFromEvents();
				newPart.commitEventsAdd();
			}

		}
		return newParts;
	}

}
