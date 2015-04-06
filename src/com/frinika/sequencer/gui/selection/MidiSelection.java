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

package com.frinika.sequencer.gui.selection;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.Selectable;
import com.frinika.sequencer.model.Part;
import javax.swing.JMenuItem;
import java.util.*;

/**
 * Keeps track of changes in selections on MIDI events and
 * switches on/off menu items related t  MIDI funcitons. Also provides
 * a uniform interface for subclasses of AbstractMidiAction to get the MIDI data
 * to operate on.
 * This MidiSelection works as SelectionListener on both
 * PartSelection and MultiEvent selection. If one of these
 * contains a MIDI event, MidiSelection will fire a selectionChanged
 * event to its listeners.
 * MidiSelection "joins" PartSelection and MultiEventSelection,
 * and filters out non-Midi selections among those.
 * 
 * @author Jens Gulden
 */
public class MidiSelection extends MultiEventSelection implements SelectionListener {
	
	protected Collection<JMenuItem> menuItems = new ArrayList<JMenuItem>(); // list of MenuItems to automatically activate/deactivate depending on available MidiSelection
	
	public MidiSelection(ProjectContainer project) {
		super(project);
		project.getPartSelection().addSelectionListener(this);
		project.getMultiEventSelection().addSelectionListener(this);
	}
	
	public void selectionChanged(SelectionContainer sc) {
		Vector<MultiEvent> r = null;
		if (sc instanceof PartSelection) {
			// make sure there's not a MultiEventSelection at the same time, which would be preferred instead
			MultiEventSelection mes = project.getMultiEventSelection();
			if (mes != null) {
				r = (Vector<MultiEvent>)mes.getSelected();
				if ( ! r.isEmpty() ) {
					sc = mes; // use this one instead
				} else {
					r = null;
				}
			}
		}
		if (r == null) {
			Collection<Selectable> sel = sc.getSelected();
			r = getMultiEventsFromSelection(sel);
		}
		if ( ! r.isEmpty() ) {
			this.selectedList = r;
		} else {
			this.selectedList = null;
		}
		updateItems();
		dirty = true;
		notifyListeners();
	}
	
	public void setMetaFocus() {
		// nop
	}
	
	public boolean isSelectionAvailable() {
		boolean b = (this.selectedList != null) && (!this.selectedList.isEmpty());
		return b;
	}
	
	/**
	 * Get currently selected MidiPart, or null if no MidiPart is selected 
	 * @return
	 */
	public MidiPart getMidiPart() {
		Collection<Part> partSelection = project.getPartSelection().getSelected();
		if ((partSelection != null) && ( ! partSelection.isEmpty() )) {
			Part p = partSelection.iterator().next();
			if (p instanceof MidiPart) {
				return (MidiPart)p;
			}
		}
		if (this.selectedList != null) {
			if ( ! this.selectedList.isEmpty() ) {
				MultiEvent event = this.selectedList.firstElement();
				return event.getMidiPart();
			}
		}
		return null;
	}
	
	public MidiLane getMidiLane() {
		MidiPart p = getMidiPart();
		if (p != null) {
			return (MidiLane)p.getLane();
		} else {
			return null;
		}
	}
	
	protected static Vector<MultiEvent> getMultiEventsFromSelection(Collection<Selectable> sel) {
		Vector<MultiEvent> c = new Vector<MultiEvent>();
		for (Selectable o : sel) {
			if (o instanceof MidiPart) {
				for (MultiEvent me : ((MidiPart)o).getMultiEvents()) {
					c.add(me);
				}
			} else if (o instanceof MultiEvent) {
				c.add((MultiEvent)o);
			}
		}
		return c;
	}
	
	public void addMenuItem(JMenuItem item) {
		menuItems.add(item);
		item.setEnabled(isSelectionAvailable());
	}
	
	protected void updateItems() {
		boolean b = isSelectionAvailable();
		// update corresponding JMenuItems
		for (JMenuItem item : menuItems) {
			item.setEnabled(b);
		}
	}
}
