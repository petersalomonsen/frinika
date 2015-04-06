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

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.partview.PartView;
import com.frinika.sequencer.gui.pianoroll.PianoRoll;
import com.frinika.sequencer.gui.selection.MidiSelection;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.gui.AbstractDialogAction;
import javax.swing.JMenuItem;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.util.*;

/**
 * Abstract superclass for menu-actions that modify currently selected MIDI 
 * data. 
 * 
 * @author Jens Gulden
 */
abstract public class AbstractMidiAction extends AbstractDialogAction {
	
	protected Collection<MultiEvent> events;
	protected long startTick;
	protected long lastTick;
	protected long endTick;

	public AbstractMidiAction(ProjectFrame frame, String actionId) {
		super(frame, actionId);
	}
	
	public void performPrepare() {
		ProjectContainer project = frame.getProjectContainer();
		MidiSelection m = project.getMidiSelection();
		events = m.getSelected();
		if ((events == null) || (events.isEmpty())) {
			cancel();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if( ! (java.awt.EventQueue.getCurrentEvent().getSource() instanceof JMenuItem) ) { // event does not originate from JMenuItem, but from KeyStroke
			Object focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
			if( ! ((focusOwner instanceof PartView) || (focusOwner instanceof PianoRoll)) ) { // otherwise any gui-element catching single-key strokes would invoke menu
				return;
			}
		}
		super.actionPerformed(e);
	}

	protected void performAction() {
		
		// to modify the model, events must first be removed, later be re-added to the sequencer track
		Collection<MultiEvent> clones = new ArrayList<MultiEvent>();
		
		try {
			startTick = 0;
			lastTick = 0;
			endTick = 0;
			for (MultiEvent ev : events) {
				MultiEvent clone = (MultiEvent) (ev.clone());
				clones.add(clone);
				if ((startTick == 0) || (clone.getStartTick() < startTick)) {
					startTick = clone.getStartTick();
				}
				if (clone.getStartTick() > lastTick) {
					lastTick = clone.getStartTick();
				}
				if (clone.getEndTick() > endTick) {
					endTick = clone.getEndTick();
				}
			}
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace();
		}

		modifyEvents(clones);
		
		Iterator<MultiEvent> clonesIterator = clones.iterator();
		for (MultiEvent ev : events) {
			//if (ev instanceof NoteEvent) {
				ev.getPart().remove(ev);
				ev.restoreFromClone(clonesIterator.next());
				ev.getPart().add(ev);
				//me.commitAdd();
			//}
		}
	}				
	
	/**
	 * May be overwritten by subclasses for more complex modifying operations,
	 * otherwise by default this calls midifyNoteEvents with all selected NoteEvents.
	 */
	public void modifyEvents(Collection<MultiEvent> events) {
		Collection<NoteEvent> notes = new ArrayList<NoteEvent>();
		for (MultiEvent me : events) {
			if (me instanceof NoteEvent) {
				notes.add((NoteEvent)me);
			}
		}
		modifyNoteEvents(notes);
	}
			
	public void modifyNoteEvents(Collection<NoteEvent> events) {
		for (NoteEvent note : events) {
			modifyNoteEvent(note);
		}
	}
			
	public abstract void modifyNoteEvent(NoteEvent note);
	
	public MidiPart getMidiPart() {
		if ((events != null) && (!events.isEmpty())) {
			return events.iterator().next().getMidiPart();
		} else {
			return null;
		}
	}

	public MidiLane getMidiLane() {
		MidiPart part = getMidiPart();
		if (part != null) {
			return (MidiLane)part.getLane();
		} else {
			return null;
		}
	}
}
