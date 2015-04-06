/*
 * Created on Apr 19, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

import static com.frinika.localization.CurrentLocale.getMessage;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.Item;
import com.frinika.sequencer.gui.pianoroll.DragEventListener;
import com.frinika.sequencer.gui.pianoroll.FeedbackEventListener;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;


/**
 * 
 *  The drag list is a copy of the multiEventSelection.
 * 
 *  The copy is created by calling startDrag
 *  
 *  The after calling startDrag the caller can
 *     modifying the items in the drag list (e.g. the pith start tick etc).
 *     use the notification methods in this class to inform the GUI of changes.
 * 
 *  There are two types of observer.
 *    DragEventListener is interested in changes to the drag list.
 *    FeedbackItemListener is interested in a single representive item.
 *    Tpically an element of the drag list (defined as a parameter to start drag)
 * 
 *  Calling endDrag(false) will cause the original selected events to be replaced by the dragList.
 *          endDrag(true) will leave the originals and add the dragList to the part.
 * 
 *  You can call directly invoke notifyFeedbackListeners(Item it) if you wish to  update the single item view listening 
 *  to the DragList.  (Any cleaner way to do this ?) 
 * 
 * @author pjl
 *
 *
 */
@SuppressWarnings("serial")
public class DragList extends Vector<Item> {
	
	private ProjectContainer project;
	transient Vector<DragEventListener> dragEventListeners;
	transient Vector<FeedbackEventListener> feedbackEventListeners;
	private Item dragItem;

//	MultiEvent referenceItem;
	
	public DragList(ProjectContainer project) {
		this.project=project;
		dragEventListeners = new Vector<DragEventListener>();
		feedbackEventListeners = new Vector<FeedbackEventListener>();
	}

	
	public void endDrag(boolean copy) {

		if (project.getMultiEventSelection().getSelected().isEmpty()) {
			System.out
					.println(" Selected list empty in pianoroll endDrag (why did you bother dragging nothing ?) ");
			project.getDragList().clear();
			return;
		}

		if (!copy) {
			project.getEditHistoryContainer().mark(getMessage("sequencer.pianoroll.drag_move_notes"));
			Iterator<Item> iter = project.getDragList().iterator();
			Vector<MultiEvent> list = new Vector<MultiEvent>(project
					.getMultiEventSelection().getSelected());
			for (MultiEvent ev : list) {
				ev.getPart().remove(ev);
				ev.restoreFromClone((MultiEvent)iter.next());
				ev.getPart().add(ev);
			}
			assert (!iter.hasNext());
			project.getMultiEventSelection().setSelected(list);
			
		} else {
			project.getEditHistoryContainer().mark(getMessage("sequencer.pianoroll.drag_copy_notes"));
			Iterator<Item> iter = project.getDragList().iterator();
			for (MultiEvent ev : project.getMultiEventSelection().getSelected()) {
				ev.getPart().add((MultiEvent)iter.next());
			}
			project.getMultiEventSelection().setSelected(
					(Collection) project.getDragList());

			assert (!iter.hasNext());
		}

		project.getEditHistoryContainer().notifyEditHistoryListeners();
		project.getMultiEventSelection().notifyListeners();

		clear();
		notifyFeedbackItemListeners(project.getMultiEventSelection()
				.getFocus());
		notifyDragEventListeners();
	}
	
	
	public void endDragController() {


		// TODO localization
	

		if (project.getMultiEventSelection().getSelected().isEmpty()) {
			System.out
					.println(" Selected list empty in controllerview endDrag (why did you bother dragging nothing ? ) ");
			return;
		}
		
		project.getEditHistoryContainer().mark("drag velocity");
		// TODO may not want to delete these ? CNTRL DRAG

		Iterator<Item> iter = iterator();
		Vector<MultiEvent> list =new Vector<MultiEvent>(project.getMultiEventSelection().getSelected());
		for (MultiEvent ev : list) {
			// if (!validEvent(ev)) continue;
			ev.getPart().remove(ev);
			ev.restoreFromClone((MultiEvent)iter.next());
			ev.getPart().add(ev);
		}

		project.getEditHistoryContainer().notifyEditHistoryListeners();

		clear();

	}

	/**
	 * 
	 * Start a drag.
	 * 
	 *
	 * @param dragItem reference item for displaying feedback (the copy of this item in the draglist can be observered as it is being draged)
	 *         getDragReferenceItem();
	 *  
	 */
	public void startDrag(Item dragItemRef) {

		// project.clearDragList();
		
	//	this.dragItem=dragItem;
		Vector<Item> dragList = project.getDragList();
		dragList.clear();
		
	//	referenceItem = null;
		for (MultiEvent it : project.getMultiEventSelection().getSelected()) {
			if (it instanceof NoteEvent) {
				try {
					NoteEvent dragNote = (NoteEvent) (it.clone());
					dragList.add(dragNote);

					if (it == dragItemRef) {
						dragItem=dragNote;
						notifyFeedbackItemListeners(dragNote);
					}

				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public void addDragEventListener(DragEventListener o) {
		dragEventListeners.add(o);

	}

	public void removeDragEventListener(DragEventListener o) {
		dragEventListeners.add(o);
	}


	public void notifyDragEventListeners() {
		for (DragEventListener l : dragEventListeners) {
			l.update();
		}	
	}
	
	
	public void addFeedbackItemListener(FeedbackEventListener o) {
		feedbackEventListeners.add(o);

	}

	public void removeFeedbackItemListener(FeedbackEventListener o) {
		feedbackEventListeners.add(o);
	}

	public void notifyFeedbackItemListeners(Item ev) {
		for (FeedbackEventListener l : feedbackEventListeners) {
			l.notifyFeedbackItemChanged(ev);
		}
	}

	public void notifyFeedbackItemListeners() {
		for (FeedbackEventListener l : feedbackEventListeners) {
			l.notifyFeedbackItemChanged(dragItem);
		}
	}


}
