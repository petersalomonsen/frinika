/*
 * Created on Feb 10, 2007
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

package com.frinika.sequencer.gui;

import javax.swing.JComboBox;

import com.frinika.sequencer.gui.virtualkeyboard.VirtualKeyboard;

/**
 * GUI element for selecting a MIDI-note from a drop-down-list.
 * 
 * @author Jens Gulden
 */
public class NoteSelector extends JComboBox {
	
	private static String[] ITEMS;

	static { // class-initializer
		ITEMS = new String[128];
		for (int i = 0; i < 128; i++) {
			ITEMS[i] = VirtualKeyboard.getNoteString(127 - i);
		}
	}
	
	public NoteSelector() {
		super(ITEMS);
	}
	
	public NoteSelector(int initialNote) {
		this();
		setNote(initialNote);
	}
	
	public int getNote() {
		return 127 - getSelectedIndex();
	}
	
	public void setNote(int note) {
		setSelectedIndex(127 - note);
	}
}
