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

import java.util.Iterator;
import java.util.SortedSet;

import com.frinika.sequencer.model.MultiEvent;

public class EventIterator implements Iterator<MultiEvent> {

	Iterator<MultiEvent> eIter;
	MultiEvent next=null;
	private EventFilter filter;
	
	public EventIterator(SortedSet<MultiEvent> list,EventFilter filter) {
		eIter= list.iterator();
		this.filter=filter;
		seekNext();
	}
	
	public boolean hasNext() {
		return next != null ;
	}
	private void seekNext() {
		while(eIter.hasNext()) {
			if (filter.isValidEvent((next=eIter.next()))) return; 
		}
		next=null;
	}

	public MultiEvent next() {
		MultiEvent tmp=next;
		seekNext();
		return tmp;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
