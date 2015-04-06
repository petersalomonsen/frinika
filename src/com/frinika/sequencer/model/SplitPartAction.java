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

import java.io.FileNotFoundException;
import java.util.Vector;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.ItemPanel;

/**
 * Action to split selected parts at a given tick.
 * 
 * @author Paul
 * 
 */
public class SplitPartAction implements EditHistoryAction {

	ProjectContainer project;

	ItemPanel panel;

	Vector<Part> victims;

	Vector<Part> newParts = null;

	long splitTick;

	public SplitPartAction(ProjectContainer project, long tick) {

                // victims are the existing parts to be split
		victims = new Vector<Part>();
		splitTick = tick;

                for (Part part : project.getPartSelection().getSelected()) {
			Lane lane = part.getLane();
			if (lane instanceof MidiLane || lane instanceof AudioLane) {
				if (part.getStartTick() < tick && part.getEndTick() > tick)
					victims.add(part);
			}
		}
		this.project = project;
	}

	public void undo() {

		for (Part part : newParts) {
			part.commitEventsRemove();
			part.lane.parts.remove(part);
		}

		for (Part part : victims) {
			part.lane.parts.add(part);
			part.commitEventsAdd();
		}

		project.getPartSelection().removeSelected(newParts);
	}

	public void redo() {

		// Save some resources if this is really a redo
            // if newParts is not null then we have already done the split and we really are redoing it
		if (newParts != null) {
			for (Part part : victims) {
				part.commitEventsRemove();
				part.lane.parts.remove(part);
			}

			for (Part part : newParts) {
				((MidiPart) part).commitEventsAdd();
				// part.lane.parts.add(part);
			}
			return;
		}

                // Here if we are splitting for the first time
		newParts = new Vector<Part>();
		// Part focus = project.getPartSelection().getFocus();
		// Part fff=null;

		for (Part part : victims) {
			if (part instanceof MidiPart)
				midiPartRedo((MidiPart) part);
			else if (part instanceof AudioPart)
				audioPartRedo((AudioPart) part);
		}

		project.getPartSelection().removeSelected(victims);
		project.getPartSelection().addSelected(newParts);

	}

	void midiPartRedo(MidiPart part) {
		part.commitEventsRemove();
		part.lane.parts.remove(part);

		MidiPart left = new MidiPart((MidiLane) part.getLane());
		left.setStartTick(part.getStartTick());
		left.setEndTick(splitTick);
		MidiPart right = new MidiPart((MidiLane) part.getLane());

		right.setStartTick(splitTick);
		right.setEndTick(part.getEndTick());
		// if (part == focus) fff=focus;
		newParts.add(left);
		newParts.add(right);

		MidiPart dst;

		for (MultiEvent ev : ((MidiPart) part).getMultiEvents()) {
			if (ev.getStartTick() < splitTick)
				dst = left;
			else
				dst = right;

			MultiEvent eNew = (MultiEvent) ev.deepCopy(dst);
			dst.getMultiEvents().add(eNew);
		}
		left.commitEventsAdd();
		right.commitEventsAdd();

	}

	void audioPartRedo(AudioPart part) {
		// part.commitEventsRemove();
		part.lane.parts.remove(part);

		AudioPart left;

		left = (AudioPart) part.deepCopy(part.getLane());
		left.getEvelope().setTFall(0);
		left.setStartTick(part.getStartTick());
		left.setEndTick(splitTick);
		try {
			left.onLoad();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AudioPart right = (AudioPart) part.deepCopy(part.getLane());
		right.getEvelope().setTRise(0);

		right.setStartTick(splitTick);
		right.setEndTick(part.getEndTick());
		try {
			right.onLoad();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if (part == focus) fff=focus;
		newParts.add(left);
		newParts.add(right);

		// left.commitEventsAdd();
		// right.commitEventsAdd();

	}

}