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

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.MidiPart;
import javax.swing.JComponent;
import java.util.*;

/**
 * Menu-action for changing the duration of currently selected MIDI notes.
 *  
 * @author Jens Gulden
 */
public class MidiDurationAction extends AbstractMidiAction {
	
	public final static int MODE_SET = 1;
	public final static int MODE_CHANGE = 2;
	public final static int MODE_LEGATO = 3;
	
	int mode = MODE_SET;
	long setTicks = 32;
	long changeTicks = 16;
	int legatoGap = 1;

	public MidiDurationAction(ProjectFrame frame) {
		super(frame, "sequencer.midi.duration");
	}

	protected JComponent createGUI() {
		return new MidiDurationActionEditor(this, frame);
	}
	
	@Override
	public void modifyNoteEvent(NoteEvent note) {
		switch (mode) {
			case MODE_SET:
				note.setDuration(setTicks);
				break;
			case MODE_CHANGE:
				note.setDuration(note.getDuration() + changeTicks);
				break;
			case MODE_LEGATO:
				note.setDuration(legatoDuration(note) - legatoGap);
				break;
		}
	}
	
	private static long legatoDuration(NoteEvent note) {
		// find next following note in same part
		MidiPart part = note.getMidiPart();
		long next = findFollowingNote(part.getMultiEvents(), note.getEndTick());
		if (next == -1) return note.getDuration();
		long d = next - note.getStartTick();
		return d;
	}
	
	private static long findFollowingNote(Collection<MultiEvent> events, long tick) {
		long next = Long.MAX_VALUE;
		for (MultiEvent event : events ) {
			if (event instanceof NoteEvent) {
				long start = event.getStartTick();
				if ((start > tick) && (start < next)) {
					next = start;
				}
			}
		}
		if (next != Long.MAX_VALUE) {
			return next;
		} else {
			return -1;
		}
	}

}
