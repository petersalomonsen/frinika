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

public class MovePartEditAction implements EditHistoryAction {

	Part part;

	double dTick;

	Lane dstLane;

	Lane srcLane;

	public MovePartEditAction(Part part, double dTick, Lane dstLane) {
		this.part = part;
		this.srcLane = part.getLane();
		this.dTick = dTick;
		this.dstLane = dstLane;
	}

	public void undo() {
		
		// TODO put this into the Part interface.
//		if (part instanceof AudioPart) {
//		//	part( );
//		} else if (part instanceof MidiPart){	
//		part.startTick -= dTick;
//		part.endTick -= dTick;
//		 ((MidiPart)part).commitEventsRemove();
		// TODO there is a redundant remove add in the next call
		part.moveContentsBy(-dTick,srcLane);
//		if (srcLane != dstLane) {
//			dstLane.getParts().remove(part);
//			srcLane.getParts().add(part);
//			part.lane=srcLane;
//		}
//		((MidiPart)part).commitEventsAdd();
//		}

	}

	public void redo() {
//		part.startTick += dTick;
//		part.endTick += dTick;
//		if (part instanceof MidiPart) ((MidiPart)part).commitEventsRemove();
//		if (dstLane != srcLane) {			
//			srcLane.getParts().remove(part);
//			dstLane.getParts().add(part);
//			part.lane=dstLane;
//		}
		// TODO parent child structure
		part.moveContentsBy(dTick,dstLane);
//		if (part instanceof MidiPart) ((MidiPart)part).commitEventsAdd();

	}

	public Part getPart() {
		return part;
	}


}
