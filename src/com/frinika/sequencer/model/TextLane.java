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

import com.frinika.global.Toolbox;
import com.frinika.project.ProjectContainer;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Text lane.
 * 
 * @author Jens Gulden
 */
public class TextLane extends Lane {
	
	private static final long serialVersionUID = 1L;

	transient protected ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

	static Icon iconTextLane = new javax.swing.ImageIcon(
			TextLane.class.getResource("/icons/new_track_text.gif"));
	
	static int nameCount=0;
	
	public TextLane(ProjectContainer project) {
		super("Text " + nameCount++, project);
		long ticks = project.getSequencer().getTickPosition();
		createNewTextPart(ticks);
	}
	
	public TextPart createNewTextPart(long ticks) {
		TextPart part = new TextPart(this);
		part.setStartTick(ticks);
		part.setEndTick(ticks + TextPart.DEFAULT_WIDTH);
		return part;
	}
	
	public Selectable deepCopy(Selectable parent) {
		return null;
	}

	public void deepMove(long tick) {
		// TODO Auto-generated method stub
	}

	public void restoreFromClone(EditHistoryRecordable object) {

	}

	public String getAllText(String delim) {
		ArrayList<String> l = new ArrayList<String>();
		for (Part p : getParts()) {
			String s = ((TextPart)p).getText().trim();
			l.add(s);
		}
		// remove empty parts at the end
		while ((!l.isEmpty()) && (l.get(l.size()-1).equals(TextPart.EMPTY_STRING))) {
			l.remove(l.size()-1);
		}
		return Toolbox.joinStrings(l, delim);
	}
	
	public void setAllText(String text, String delim) {
		List<String> stringParts = Toolbox.splitString(text, delim);
		List<Part> textParts = getParts();
		Iterator<Part> iterator = new ArrayList(textParts).iterator();
		long lastTick = 0;
		for (String s : stringParts) {
			TextPart textPart;
			if (iterator.hasNext()) {
				textPart = (TextPart)iterator.next();
			} else {
				textPart = createNewTextPart(lastTick + 128*4);
			}
			textPart.setText(s);
			lastTick = textPart.getEndTick();
		}
		// any text parts left? reset them to "no-content"
		while (iterator.hasNext()) {
			TextPart textPart = (TextPart)iterator.next();
			textPart.setText(TextPart.EMPTY_STRING);
		}
	}
	
	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}
	
	public void removeChangeListener(ChangeListener l) {
		changeListeners.remove(l);
	}
	
	void fireChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for (ChangeListener l : changeListeners) {
			l.stateChanged(e);
		}
	}
	
	// --- Serialization ---

	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		changeListeners = new ArrayList<ChangeListener>();
	}

	@Override
	public Part createPart() {
		// TODO Auto-generated method stub    (could probably use this ?)
		assert(false);
		return null;
	}

	@Override
	public Icon getIcon() {

		return iconTextLane;
	}
}
