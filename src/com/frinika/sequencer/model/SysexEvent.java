/*
 * Created on Feb 1, 2007
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

package com.frinika.sequencer.model;

import static com.frinika.localization.CurrentLocale.getMessage;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.SysexMessage;
import javax.swing.JOptionPane;

import com.frinika.global.Toolbox;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.StringTokenizer;

/**
 * Event for system-exclusive MIDI data.
 * 
 * @see SysexMacro
 * @author Jens Gulden
 */
public class SysexEvent extends MultiEvent {

	private static final long serialVersionUID = 1L;

	protected String macro; // macro or raw data as string (starting with F0 or F7)
    transient protected MidiEvent[] midiEvents = new MidiEvent[0];
	
	public SysexEvent(MidiPart part, long startTick) {
		super(part, startTick);
	}
	
	public SysexEvent(MidiPart part, long startTick, String macro) {
		this(part, startTick);
		this.setMacroString(macro);
	}
	
	public SysexEvent(MidiPart part, long startTick, byte[] data) {
		this(part, startTick);
		this.setData(data);
	}
	
	/*public String getMacroString() {
		return macro;
	}*/

	public void setMacroString(String macro) {
		this.macro = macro;
		try {
			midiEvents = parseMacro(macro);
		} catch (InvalidMidiDataException imde) {
			//error(imde);
			midiEvents = new MidiEvent[0];
			imde.printStackTrace();
		}
	}

	public void setData(byte[] data) {
		// single raw sysex message
		try {
			SysexMessage syxm = new SysexMessage();
			syxm.setMessage(data, data.length);
			midiEvents = new MidiEvent[] { new MidiEvent(syxm, startTick) };
		} catch (InvalidMidiDataException imde) {
			imde.printStackTrace();
		}
	}
	
	public boolean isSuccessfullyParsed() {
		return (midiEvents != null);
	}
	
	protected MidiEvent[] parseMacro(String macro) throws InvalidMidiDataException {
		if ((macro == null) || (macro.trim().length() == 0)) {
			return new MidiEvent[0];
		}
		SysexMacro m = AbstractSysexMacro.findMacro(macro);
		if (m == null) {
			throw new InvalidMidiDataException("Cannot find macro '"+Toolbox.firstWord(macro)+"'.");
		}
		MidiMessage[] mm = null;
		mm = m.parseMessages(macro);
		if (mm == null) {
			throw new InvalidMidiDataException("Parsing of '"+macro+"' failed.");
		}
		MidiEvent[] me = new MidiEvent[mm.length];
		for (int i = 0; i < mm.length; i++) {
			me[i] = new MidiEvent(mm[i], getStartTick());
		}
		return me;
	}
	
	public void showEditorGUI(ProjectFrame frame) {
		final String oldMacroString = this.macro;
		boolean err;
		do {
			err = false;
			final String s = JOptionPane.showInputDialog(frame, getMessage("sequencer.sysex.edit_sysex") + ":", oldMacroString);
			if (s != null) {
				if ( ! s.equals(oldMacroString) ) {
					try {
						final MidiEvent[] events = parseMacro(s);
						final MidiEvent[] oldEvents = this.midiEvents;

						ProjectContainer project = frame.getProjectContainer();
						project.getEditHistoryContainer().mark(getMessage("sequencer.sysex.edit_sysex"));
						EditHistoryAction action = new EditHistoryAction() {
							public void redo() {
								SysexEvent.this.macro = s;
								SysexEvent.this.commitRemove();
								SysexEvent.this.midiEvents = events;
								SysexEvent.this.commitAdd();
							}
							
							public void undo() {
								SysexEvent.this.macro = oldMacroString;
								SysexEvent.this.commitRemove();
								SysexEvent.this.midiEvents = oldEvents;
								SysexEvent.this.commitAdd();
							}
						};
						action.redo(); // do it
						project.getEditHistoryContainer().push(action);
						project.getEditHistoryContainer().notifyEditHistoryListeners();
					} catch (InvalidMidiDataException imde) {
						frame.error(imde.getMessage());
						err = true;
					}
				}
			} else { // cancelled
				this.macro = oldMacroString;
			}
		} while (err);
	}
	
	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.MultiEvent#commitAdd()
	 */
	@Override
	public void commitAddImpl() {
    	for (int i = 0; i < midiEvents.length; i++) {
            getTrack().add(midiEvents[i]);
    	}
        zombie=false;
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.MultiEvent#commitRemove()
	 */
	@Override
	void commitRemoveImpl() {
    	for (int i = 0; i < midiEvents.length; i++) {
            getTrack().remove(midiEvents[i]);
    	}
        zombie=true;
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.MultiEvent#getEndTick()
	 */
	@Override
	public long getEndTick() {
		return getStartTick();
	}

	/* (non-Javadoc)
	 * @see com.frinika.sequencer.model.EditHistoryRecordable#restoreFromClone(com.frinika.sequencer.model.EditHistoryRecordable)
	 */
	public void restoreFromClone(EditHistoryRecordable object) {
		SysexEvent other = (SysexEvent)object;
		this.part = other.part;
		//this.macro = note.macro;
		this.setMacroString(other.macro);
		this.startTick = other.startTick;
	}

	// --- Tools ---
	
	public static byte[] parseHex(String s) {
		StringTokenizer st = new StringTokenizer(s, " \t\n\r", false);
		int size = st.countTokens();
		byte[] data = new byte[size];
		for (int i = 0; i < data.length; i++) {
			byte b;
			String tok = st.nextToken();
			try {
				b = (byte)( Integer.parseInt(tok, 16) & 0xff );
			} catch (NumberFormatException nfe) {
				b = -1;
			}
			data[i] = b; 
		}
		return data;
	}

	// --- Serialization ---

	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		setMacroString(macro); // re-parse to have midiEvents initialized
		//if (midiEvents.length == 0) {
		//	macro = ""; // invalidate string if parsing failed on loading
		//}
	}
}
