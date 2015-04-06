/*
 * Created on Feb 7, 2007
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
import com.frinika.sequencer.model.NoteEvent;
import javax.swing.JComponent;

/**
 * Menu-action for transposing selected MIDI notes.
 * 
 * @author Jens Gulden
 */
public class MidiTransposeAction extends AbstractMidiAction {

	int transpose = 1;
	
	public MidiTransposeAction(ProjectFrame frame) {
		super(frame, "sequencer.midi.transpose");
	}

	public void modifyNoteEvent(NoteEvent note) {
		note.setNote( note.getNote() + transpose );
	}
			
	protected JComponent createGUI() {
		return new MidiTransposeActionEditor(this);
	}
}
