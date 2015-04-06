/*
 * Created on Apr 20, 2007
 *
 * Copyright (c) 2006-2007 Karl Helgason
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

public class NotationEvent extends MultiEvent {

	private static final long serialVersionUID = 1L;
	
    public NotationEvent(MidiPart part, long startTick)
    {
   		super(part, startTick);
    }	

	public long getEndTick() {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void commitRemoveImpl() {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void commitAddImpl() {
	}

	public void restoreFromClone(EditHistoryRecordable object) {		
		NotationEvent evt=(NotationEvent)object;
		this.part = evt.part;
		this.startTick = evt.startTick;
	}

}
