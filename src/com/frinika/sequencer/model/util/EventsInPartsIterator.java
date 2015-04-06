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
package com.frinika.sequencer.model.util;

import java.util.Collection;
import java.util.Iterator;

import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.Part;

/**
 * Iterates on all the notes in given part list
 * 
 * IN the order part by part not sequentially
 * 
 * @author Paul
 * 
 */
public class EventsInPartsIterator implements Iterator<MultiEvent> {

	Iterator<Part> partIter = null;

	// Iterator<MultiEvent> noteIter = null;
	EventIterator eventIter = null;

	// MultiEvent next=null;
	EventFilter filter;

	public EventsInPartsIterator(Collection<Part> partList, EventFilter filter) {
		this.filter = filter;
		partIter = partList.iterator();
		advanceToNextMidiPart();
	}

	public EventsInPartsIterator(Part part, EventFilter filter) {
		this.filter = filter;
		partIter = null;
		eventIter = new EventIterator(((MidiPart) part).getMultiEvents(),
				filter); // .iterator();
	}

	private boolean advanceToNextMidiPart() {

		if (partIter == null ) return false;
		Part part = null;

		while (partIter.hasNext()) {
			part = partIter.next();
			if (part instanceof MidiPart) {
				Collection<MultiEvent> list = ((MidiPart) part)
						.getMultiEvents();
				if (list.size() == 0)
					continue;
				eventIter = new EventIterator(((MidiPart) part)
						.getMultiEvents(), filter); // .iterator();
				if (!eventIter.hasNext())
					continue;
				return true;
			}
		}
		eventIter = null;
		return false;
	}

	public boolean hasNext() {
		if (eventIter == null)
			return false;
		if (eventIter.hasNext())
			return true;

		// TODO Auto-generated method stub
		return advanceToNextMidiPart();
	}

	public MultiEvent next() {
		while (eventIter.hasNext()) {
			MultiEvent ev = eventIter.next();
			assert (ev != null);
			if (filter.isValidEvent(ev))	return  ev;
		}

		// He he sneak a bit of recursion in here (PJL)
		if (advanceToNextMidiPart())
			return next();
		return null;
	}

	public void remove() {
		assert (false);
		// TODO Auto-generated method stub
	}

}
