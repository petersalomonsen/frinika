/*
 * Created on Mar 14, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

import java.util.Collection;
import java.util.Vector;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.Selectable;

public abstract  class SelectionContainer<T extends Selectable> implements SelectionFocusable {

	/**
	 * The selectionStartTick is the timebase of the selection. It's neccesary
	 * when copying events in order to place them correctly in time when pasting
	 * them to another location.
	 * 
     * The selectionStartTick is used by the tracker - since you may want to select a number of rows - but the first event in the selection might
     * not be on the first row. Let's say the first row was a beat row - and you want to paste into another beat row. Then you would want your pasted
     * data to appear as the selection. Another issue is when the selected data is not 100% quantized. Using the selectionStartTick reference solves 
     * that issue as well.
     * 
     * Set to -1 if not used
	 */
	private long selectionStartTick = -1;
    private int selectionLeftColumn; // Also used by the tracker
    
	protected T focus = null;

	Vector<T> selectedList = new Vector<T>();

	Vector<SelectionListener<T>> selectionListeners = new Vector<SelectionListener<T>>();
	ProjectContainer project;
	boolean dirty=false;
	
	public SelectionContainer(ProjectContainer project){
		this.project=project;
	
	}
	
	/**
	 * Add one T to the selection
	 * 
	 * @param item
	 */
	public void addSelected(T item) {
		selectedList.add(item);
		item.setSelected(true);
		// Notify selection listeners
		Vector<T> list = new Vector<T>();
		list.add(item);
		setFocus(item);
		
		
	//	notifyListeners();
		/*for (SelectionListener<T> selectionListener : selectionListeners)
			selectionListener.addedToSelection(this, list);
		*/
		project.setSelectionFocus(this);
		dirty=true;
	}

	/**
	 * Add multiple items to the selection
	 * 
	 * @param Lanes
	 */
	public void addSelected(Collection<? extends T> list) {
		selectedList.addAll(list);
		for (T item : list)
			item.setSelected(true);

		if (list.size() != 0 ) focus=list.iterator().next();

		// Notify selection listeners
	//	notifyListeners();
/*		for (SelectionListener<T> selectionListener : selectionListeners)
			selectionListener.addedToSelection(this, list);
*/		
		project.setSelectionFocus(this);
		dirty=true;
	}

	public void setSelected(T item) {
		Vector<T> list = new Vector<T>();
		list.add(item);
		setSelected(list);
	}
	
	/**
	 * Clear then add multiple items to the selection
	 * 
	 * @param Lanes
	 */
	public void setSelected(Collection<? extends T> list) {
		for (T t : selectedList) {
			t.setSelected(false);
		}
		selectedList.removeAllElements();
        selectionStartTick = -1;
        focus=null;
		
		for (T item : list)
			item.setSelected(true);
		
		selectedList.addAll(list);
		
		if (list.size() !=0 ) setFocus(focus=list.iterator().next());
	//	notifyListeners();
		project.setSelectionFocus(this);
		dirty=true;
	}

	@SuppressWarnings("unchecked")
	public void setSelectedX(Collection<Selectable> list) {
		for (T t : selectedList) {
			t.setSelected(false);
		}
		selectedList.removeAllElements();
        selectionStartTick = -1;
        focus=null;
		
		for (Selectable item : list)
			item.setSelected(true);
		
		selectedList.addAll((Collection<T>)list);
		
		if (list.size() !=0 ) setFocus(focus=(T)list.iterator().next());
	//	notifyListeners();
		project.setSelectionFocus(this);
		dirty=true;	
	}
	
	/**
	 * Remove one T to the selection
	 * 
	 * @param item
	 */
	public void removeSelected(T item) {
		if (item== focus) focus=null;
		selectedList.remove(item);
		item.setSelected(false);
		// Notify selection listeners
		Vector<T> list = new Vector<T>();
		list.add(item);
		
	//	notifyListeners();
/*		for (SelectionListener<T> selectionListener : selectionListeners)
			selectionListener.removedFromSelection(this, list);
*/	
		dirty=true;

	}

	/**
	 * Add multiple Lanes to the selection
	 * 
	 * @param Lanes
	 */
	public void removeSelected(Collection<? extends T> list) {
		if (list.contains(focus)) focus=null;
		selectedList.removeAll(list);

		for (T item : list) {
			//if (item instanceof Selectable)
				item.setSelected(false);
		}

		// Notify selection listeners
	//	notifyListeners();
/*		for (SelectionListener<T> selectionListener : selectionListeners)
			selectionListener.removedFromSelection(this, list);
*/	
		dirty=true;
	
	}

	/**
	 * Clear the selection
	 */
	public void clearSelection() {
		focus=null;
        selectionStartTick = -1;
		for (T t : selectedList) {
			t.setSelected(false);
		}

		selectedList.clear();

//		notifyListeners();
		// Notify selection listeners
	/*	for (SelectionListener<T> selectionListener : selectionListeners)
			selectionListener.selectionCleared(this);*/
		dirty=true;

	}

	/**
	 * @return Returns the selectionStartTick.
	 */
	public long getSelectionStartTick() {
		return selectionStartTick;
	}

	/**
	 * The selectionStartTick is the timebase of the selection. It's neccesary
	 * when copying events in order to place them correctly in time when pasting
	 * them to another location.
	 * 
	 * An editor should always set a selectionStartTick when starting on a new
	 * selection. It's a good idea to base the starttick on the current snap (or
	 * quantize) resolution.
	 * 
	 * @param selectionStartTick
	 */
	public void setSelectionStartTick(long selectionStartTick) {
		this.selectionStartTick = selectionStartTick;
		dirty=true;

	}

	/**
	 * Returns the selected Lanes. Note that you should NOT add or remove events
	 * directly from this returned collection. Use the provided methods from
	 * this container.
	 * 
	 * @return
	 */
	public Collection<T> getSelected() {
		return selectedList;
	}

	public Collection<Selectable> getObjects() {
		return (Collection<Selectable>) selectedList;
	}
	/**
	 * Add a LaneSelectionListener to this container
	 * 
	 * @param listener
	 */
	public void addSelectionListener(SelectionListener<T> listener) {
		selectionListeners.add(listener);
	}

	/**
	 * Remove a LaneSelectionListener from this container
	 * 
	 * @param listener
	 */
	public void removeSelectionListener(SelectionListener<T> listener) {
		selectionListeners.remove(listener);
	}

	/**
	 * I need a focus so I will get it from here for now. This might move (PJL).
	 * 
	 * 
	 * @return
	 */
	public T getFocus() {
		return focus;
		/*
		 * if (selectedList.size() == 0 ) return null; else return
		 * selectedList.elementAt(0);
		 */
	}
	
	/**
	 * Override this null implementation to get focus to follow containers
	 *
	 */
	protected abstract void setMetaFocus();
	
	public void setFocus(T focus) {
		if (focus == this.focus && project.getSelectionFocus() == this) return;
		this.focus=focus;	
		setMetaFocus();
	//	notifyListeners();
		project.setSelectionFocus(this);
		dirty=true;
	}

	public  void notifyListeners() {
		if (!dirty) return;
		for (SelectionListener<T> selectionListener : selectionListeners)
			selectionListener.selectionChanged(this);
		dirty=false;
	}

    public void setSelectionLeftColumn(int selectionLeftColumn) {
        this.selectionLeftColumn = selectionLeftColumn;
        dirty=true;
    }

    public int getSelectionLeftColumn() {
        return selectionLeftColumn;
    }
    
    public void setDirty() {
    	dirty=true;
    }
}
