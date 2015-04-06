/*
 * Created on Feb 8, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

package com.frinika.sequencer.gui.menu.midi;

import javax.swing.JComponent;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.NoteEvent;
import java.util.*;

/**
 * Menu-action for setting the velocity of selected MIDI notes.
 * 
 * @author Jens Gulden
 */
public class MidiVelocityAction extends AbstractMidiAction {
	
	int startVelocity = 100;
	int endVelocity = 100;
	private long startTick;
	private long endTick;

	public MidiVelocityAction(ProjectFrame frame) {
		super(frame, "sequencer.midi.velocity");
	}

	public void modifyNoteEvents(Collection<NoteEvent> events) {
		startTick = Long.MAX_VALUE;
		endTick = Long.MIN_VALUE;
		for (NoteEvent note : events) {
			if (note.getStartTick() < startTick) {
				startTick = note.getStartTick();
			}
			if (note.getStartTick() > endTick) {
				endTick = note.getStartTick();
			}
		}
		super.modifyNoteEvents(events);
	}
			
	
	protected JComponent createGUI() {
		return new MidiVelocityActionEditor(this);
	}
	
	public void modifyNoteEvent(NoteEvent note) {
		int diff = endVelocity - startVelocity;
		long dist = endTick - startTick;
		int v = startVelocity + (int)Math.round(diff * ((double)(note.getStartTick() - startTick) / dist));
		if (v < 1) v = 1; else if (v > 127) v = 127;
		note.setVelocity(v);
	}
}
