/*
 * Created on Feb 11, 2007
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

import static com.frinika.localization.CurrentLocale.getMessage;
import com.frinika.gui.OptionsDialog;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.partview.PartView;
import com.frinika.sequencer.gui.virtualkeyboard.VirtualKeyboard;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.EditHistoryAction;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import java.util.*;

/**
 * Menu-action for non-realtime step-recording.
 * 
 * @author Jens Gulden
 */
public class MidiStepRecordAction extends AbstractAction {
	
	private final static String actionId = "sequencer.midi.step_record";

	long step = 128 / 2;
	long position;
	int lengthDiff = -4;
	int velocity = 100;
	boolean autoRecord = true;
	private ProjectFrame frame;
	private MidiStepRecordActionDialog dialog;
	MidiPart part;
	
	public MidiStepRecordAction(ProjectFrame frame) {
		super(getMessage(actionId));
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		
		if(!(java.awt.EventQueue.getCurrentEvent().getSource() instanceof JMenuItem))
			if(!(KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner() instanceof PartView)) return;
		
		ProjectContainer project = frame.getProjectContainer();
		position = project.getSequencer().getTickPosition();		
		part = project.getMidiSelection().getMidiPart();
		
		if (dialog == null) {
			dialog = new MidiStepRecordActionDialog(frame, this);
			OptionsDialog.centerOnScreen(dialog);
		}
		dialog.show();
	}
	
	/**
	 * Called back from dialog, when "record" is clicked, or triggered by auto-record. 
	 */
	int[] stepRecord(int[] notes) {
		MidiPart part = frame.getProjectContainer().getMidiSelection().getMidiPart(); 
		
		if (part != null) {
			ProjectContainer project = frame.getProjectContainer();
			SortedSet<Integer> inserted = new TreeSet<Integer>();
			
			project.getEditHistoryContainer().mark(getMessage(actionId));
			
			for (int i = 0; i < notes.length; i++) {
				int n = notes[i];
				if (n != -1) {
					insertNote(n);
					inserted.add(n);
				}
			}
			
			// advance step position, undoable
			final long oldPosition = position;
			position += step;
			final long newPosition = position;
			frame.getProjectContainer().getSequencer().setTickPosition(position);
			project.getEditHistoryContainer().push(new EditHistoryAction() {
				public void undo() {
					frame.getProjectContainer().getSequencer().setTickPosition(oldPosition);
				}
				public void redo() {
					frame.getProjectContainer().getSequencer().setTickPosition(newPosition);
				}
			});
			
			project.getEditHistoryContainer().notifyEditHistoryListeners();
			
			int[] result = new int[inserted.size()];
			Iterator<Integer> it = inserted.iterator();
			for (int i = 0; i < result.length; i++) {
				result[i] = it.next();
			}
			return result;
		} else {
			frame.message("Please select a part to record into.");
			return null;
		}
		
	}
	
	/**
	 * Called back from dialog, when "record" is clicked, or triggered by auto-record. 
	 */
	String stepRecord(String s) {
		int[] notes = parseNotes(s);
		int[] n = stepRecord(notes);
		if (n != null) {
			String r = formatNotes(n);
			return r;
		} else {
			return null;
		}
	}
	
	void insertNote(int note) {
		NoteEvent n = new NoteEvent(part, this.position, note, this.velocity, ((MidiLane)part.getLane()).getMidiChannel(), this.step + this.lengthDiff);
		part.add(n);
	}
	
	/**
	 * 
	 * @param s
	 * @return array of note numbers, may contain -1 entries to marks unparseable entries 
	 */
	static int[] parseNotes(String s) {
		StringTokenizer st = new StringTokenizer(s, " \t\n\r,;:", false);
		int[] notes = new int[st.countTokens()];
		int i = 0;
		while ( st.hasMoreTokens() ) {
			notes[ i++ ] = parseNote( st.nextToken() );
		}
		return notes;
	}
	
	static String formatNotes(int[] notes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < notes.length; i++) {
			String noteName = formatNote(notes[i]);
			if (i > 0) {
				sb.append(' ');
			}
			sb.append(noteName);
		}
		return sb.toString();
	}
	
	private final static String NOTES = "c d ef g a b"; 
	
	public static int parseNote(String s) { // TODO: move to global Tool-class
		int note;
		int mod = 0; // -1, 0, +1
		int octave;
		
		int len = s.length();
		if ( (len == 0) || (len > 3) ) return -1;
		
		char n = Character.toLowerCase( s.charAt(0) );
		note = NOTES.indexOf(n);
		if (note == -1) return -1;
		
		int octavePos = 1;
		switch (len) {
			case 1: 
					octave = VirtualKeyboard.Octave; // current octave as set for virtual keyboard  via menu
					break;
			case 3: char m = s.charAt(1);
					switch (m) {
						case '-': break;
						case '#': mod = 1; break;
						case 'b': mod = -1; break;
						default: return -1; // invalid
					}
					octavePos = 2;
					// fallthrough
			case 2: char oc = s.charAt(octavePos);
					octave = (int)oc - 48;
					if (octave < 0 || octave > 9) return -1;
					break;
			default: return -1;
		}
	
		int result = note + mod + (octave * 12);
		return result;
	}
	
	public static String formatNote(int note) { // TODO: move to global Tool-class
		String s = VirtualKeyboard.getNoteString(note);
		if (s.charAt(1)=='-') {
			return new StringBuffer().append(s.charAt(0)).append(s.charAt(2)).toString(); // remove middle '-' (also not required for parsing)
		} else {
			return s;
		}
	}
}
