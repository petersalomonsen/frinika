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

package com.frinika.sequencer.gui.menu;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.gui.AbstractDialogAction;
import com.frinika.sequencer.gui.partview.PartView;
import com.frinika.sequencer.gui.selection.SelectionFocusable;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MidiPartGhost;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.Selectable;
import javax.swing.JComponent;
import javax.swing.JMenuItem;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * Menu-action for repeating the currently selected Part(s) or/and MIDI event(s).
 * As a special feature, repetitions of MidiParts can be created as "Ghosts",
 * which means that they appear as Parts in the Tracker-view, but do not contain
 * their own events. Instead, Ghosts are internally linked to the origial Part from
 * which they have been created. They represent the original Part transparently, but
 * are not editable themselves. All changed applied to the original Part will
 * immediately take effect on Ghosts also.
 * 
 * (Although this is implemented as an extension of AbstractMidiAction, it is not
 * actually a Midi-event related action only. But this is used for easier gui-dialog
 * invocation.)
 * 
 * @see com.frinika.sequencer.model.Ghost
 * @see com.frinika.sequencer.model.MidiPartGhost
 * @author Jens Gulden
 */
public class RepeatAction extends AbstractDialogAction {
	
	int repeat = 1;
	long repeatTicks;
	boolean ghost = false;
	long selectionLength;
	boolean selectionSupportsGhosts;
	protected Collection<Selectable> list;

	public RepeatAction(ProjectFrame frame) {
		super(frame, "sequencer.project.repeat");
		repeatTicks = frame.getProjectContainer().getSequence().getResolution() * 4 * 4; // default: 4 bars
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if(!(java.awt.EventQueue.getCurrentEvent().getSource() instanceof JMenuItem))
		if(!(KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner() instanceof PartView)) return;
		super.actionPerformed(e);
	}	

	protected JComponent createGUI() {
		return new RepeatActionEditor(this, frame.getProjectContainer());
	}

	protected void performPrepare() {
		ProjectContainer project = frame.getProjectContainer();
		SelectionFocusable focus = project.getSelectionFocus();
		if (focus != null) {
			list = focus.getObjects();
			if ( ! list.isEmpty() ) {
				// find first and last tick, and test if ghosts are possible
				selectionSupportsGhosts = false;
				long first = Long.MAX_VALUE;
				long last = 0;
				for (Selectable sel : list) {
					long start = Long.MAX_VALUE;
					long end = 0;
					if (sel instanceof Part) {
						start = ((Part)sel).getStartTick();
						end = ((Part)sel).getEndTick();
						if (sel instanceof MidiPart) {
							selectionSupportsGhosts = true;
						}
					} else if (sel instanceof MultiEvent) {
						start = ((MultiEvent)sel).getStartTick();
						end = ((MultiEvent)sel).getEndTick();
					}
					if (start < first) {
						first = start;
					}					
					if (end > last) {
						last = end;
					}					
				}
				if (first == Long.MAX_VALUE) {
					first = 0;
				}
				selectionLength = last - first; // must be set before askOptions is called
			} else {
				cancel();
			}
		} else {
			cancel();
		}
	}
	
	protected void performAction() {
		for (Selectable elem : list) {
			if (elem instanceof Part) {
				repeat((Part)elem);
			} else if (elem instanceof MultiEvent) {
				repeat((MultiEvent)elem);
			}
		}
	}
	
	private void repeat(Part part) {
		long tick = repeatTicks;
		Lane lane = part.getLane();
		for (int i = 0; i < repeat; i++) {
			if ((!ghost) || (!(part instanceof MidiPart)) || (part instanceof MidiPartGhost)) {
				part.copyBy(tick, lane);
			} else {
				// create ghosts instead of real copies
				assert (part instanceof MidiPart);
				createGhost((MidiPart)part, tick);
			}
			tick += repeatTicks;
		}
	}

	private void createGhost(MidiPart part, long deltaTicks) {
		MidiPartGhost ghost = new MidiPartGhost(part, deltaTicks);
		ghost.getLane().add(ghost);
	}

	private void repeat(MultiEvent event) {
		long tick = repeatTicks;
		Part part = event.getPart();
		Lane lane = part.getLane();
		for (int i = 0; i < repeat; i++) {
			copyBy(event, tick); // (no ghosts for MultiEvents, only for MidiParts)
			tick += repeatTicks;
		}
	}
	
	private static void copyBy(MultiEvent event, long deltaTicks) {
		try {
			MultiEvent newEvent = (MultiEvent)event.clone();
			long t = newEvent.getStartTick() + deltaTicks;
			newEvent.setStartTick(t);
			newEvent.getPart().add(newEvent);
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace();
		}
	}
	
}
