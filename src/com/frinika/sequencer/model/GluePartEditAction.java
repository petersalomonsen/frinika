/*
 * Created on 28-May-2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

import com.frinika.project.ProjectContainer;

/**
 * Action to split selected parts at a given tick.
 * 
 * @author Paul
 * 
 */
public class GluePartEditAction implements EditHistoryAction {

	ProjectContainer project;

	MidiPart origPart1;

	MidiPart origPart2 = null;

	MidiPart newPart = null;

	public GluePartEditAction(MidiPart part) {
		origPart1 = part;
		project = part.getLane().getProject();
	}

	public void undo() {

		if (newPart == null)
			return;
		newPart.commitEventsRemove();
		newPart.lane.parts.remove(newPart);

		origPart1.lane.parts.add(origPart1);
		origPart1.commitEventsAdd();

		origPart2.lane.parts.add(origPart2);
		origPart2.commitEventsAdd();
		project.getPartSelection().removeSelected(newPart);

	}

	public void redo() {

		if (newPart == null) {
			origPart2 = findNearest(origPart1);
			if (origPart2 == null) {
				System.out
						.println(" Need at least 2 parts in the lane to use glue");
				return;
			}

			newPart = new MidiPart((MidiLane) origPart1.getLane());

			newPart.setStartTick(Math.min(origPart1.getStartTick(),
					origPart2.getStartTick()));
			newPart.setEndTick(Math.max(origPart1.getEndTick(), origPart2.getEndTick()));

			for (MultiEvent ev : origPart1.getMultiEvents()) {
				MultiEvent eNew = (MultiEvent) ev.deepCopy(newPart);
				newPart.getMultiEvents().add(eNew);
			}

			for (MultiEvent ev : origPart2.getMultiEvents()) {
				MultiEvent eNew = (MultiEvent) ev.deepCopy(newPart);
				newPart.getMultiEvents().add(eNew);
			}

		} else {
			// newPart.lane.parts.add(newPart);
		}

		origPart1.commitEventsRemove();
		origPart1.lane.parts.remove(origPart1);
		origPart2.commitEventsRemove();
		origPart2.lane.parts.remove(origPart2);
		newPart.commitEventsAdd();

		project.getPartSelection().removeSelected(origPart1);
		project.getPartSelection().removeSelected(origPart2);
	}

	MidiPart findNearest(Part ref) {

		long dist = Long.MAX_VALUE;
		long start = ref.getStartTick();
		Part part2 = null;

		for (Part part : ref.getLane().getParts()) {
			if (part == ref)
				continue;
			long end2 = part.getEndTick();
			long start2 = part.getStartTick();
			if (start2 < start) {
				long dist2 = end2 - start;
				if (Math.abs(dist2) < dist) {
					part2 = part;
					dist = Math.abs(dist2);
				}

			}
		}
		return (MidiPart) part2;
	}

}