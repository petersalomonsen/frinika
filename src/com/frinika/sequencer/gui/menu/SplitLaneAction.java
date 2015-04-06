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
package com.frinika.sequencer.gui.menu;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.Part;

public class SplitLaneAction extends AbstractAction {

	/**
	 * 
	 */
	
	private static final long serialVersionUID = 1L;
	private ProjectFrame project;
	
	public SplitLaneAction(ProjectFrame project) {
		super(getMessage("sequencer.project.split_lane"));
		this.project=project;		
	}
	
	public void actionPerformed(ActionEvent arg0) {
		//project.
		Lane lane=project.getProjectContainer().getLaneSelection().getFocus();
		if (lane == null) {
			project.infoMessage("Please select a Lane before Spliting!");
			return;
		}
		
		List<Part> parts = lane.getParts();
		if (parts.isEmpty()) {
			project.infoMessage("Lane is empty!");
			return;
		}
		
		if (parts.size() != 1 ){
			project.infoMessage("Lane must only contain 1 part!");
			return;			
		}
		
		Part part = parts.get(0);

		if (! (part instanceof MidiPart)) {
			project.infoMessage("Please select  MidiLane!");
			return;			
		}
		
        project.getProjectContainer().getEditHistoryContainer().mark(getMessage("sequencer.project.split_lane"));	
		
		MidiPart mPart=(MidiPart)part;
		
		MidiPart newPart = new MidiPart((MidiLane)lane);
		long ticksPerBeat=project.getProjectContainer().getSequence().getResolution();
		long gap=ticksPerBeat/2;
		long lastTick=Long.MAX_VALUE;
		long tStart=0;
		for (MultiEvent ev: mPart.getMultiEvents()) {
			long t1=ev.getStartTick();
			if (t1-lastTick > gap ) {
				tStart=t1;
				newPart=new MidiPart((MidiLane)lane);
			}
			lastTick=ev.getEndTick();
			newPart.add(ev);
		}
		
		lane.remove(part);

		project.getProjectContainer().getEditHistoryContainer().notifyEditHistoryListeners();
	}

}
