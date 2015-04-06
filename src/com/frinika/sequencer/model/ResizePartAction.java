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

public class ResizePartAction implements EditHistoryAction {

	Part part;

	double startSec;
	double endSec;
	double startOrig;
	double endOrig;

	// TODO XXX
	public ResizePartAction(Part part, double start,double end) {
		this.part = part;
		this.startSec = start;
		this.endSec =end;
		this.startOrig = part.getStartInSecs();
		this.endOrig = part.getEndInSecs();
	}

	public void undo() {
		part.commitEventsRemove();
		part.setStartInSecs(startOrig);
		part.setEndInSecs(endOrig);
		part.commitEventsAdd();
	}

	public void redo() {
		part.commitEventsRemove();
		part.setStartInSecs(startSec);
		part.setEndInSecs(endSec);
		part.commitEventsAdd();
	}

	public Part getPart() {
		return part;
	}
}
