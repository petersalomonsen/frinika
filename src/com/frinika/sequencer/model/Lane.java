/*
 * Created on Mar 2, 2006
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

package com.frinika.sequencer.model;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import uk.org.toot.audio.core.AudioBuffer.MetaInfo;

import com.frinika.project.ProjectContainer;

public abstract class Lane implements Selectable, EditHistoryRecordable,
		EditHistoryRecorder<Part>, Serializable {
	private static final long serialVersionUID = -2220043314267459844L;

//	static int colorCount = 0;

	transient boolean selected = false;

	
	transient MetaInfo channelLabel = null;

	//int colorID;

	Color color;
	
	protected List<Part> parts;

	/* this is the list of lanes contained within this lane. */
	protected List<Lane> children;

	/* this is for display */
	transient private int displayY;

	transient private int displayH;

	protected int height;

	/* flags for GUI */
	boolean open = true;

	boolean hidden = false;

	/* display lane index (how many lanes from the top is it displayed) */
	transient int visibleID;

	transient int uniqueID;  // is this used ??? PJL

	private String name;

	ProjectContainer project;

	/*
	 * to save the position in the children list. Only for use by the add/remove
	 * methods
	 */
	transient private int indexInList = -1;

	int soloMuteFlags = 0;

	/* Constructor for serializable interface */

	protected Lane() {
	}

	protected Lane(String name, ProjectContainer project) {
		height = 1; // Layout.getLaneItemHeight();
		this.project = project;
		parts = new Vector<Part>();
		children = new Vector<Lane>();
		setName(name);
	
		//	colorID = colorCount++;
	}

	public List<Part> getParts() {
		return parts;
	}

	
	/**
	 * 
	 * Remove all parts
	 *
	 */
	public void removeAll() {
		Vector<Part> partsCopy=new Vector<Part>(parts);
		for (Part part:partsCopy) {
			remove(part);
		}
	}
	
	/**
	 * remove a part.
	 * You must use this if you want the GUI to be maintained
	 * Call project.getEditHistoryContainer().notfiyObserers().
	 * 
	 */
	public void remove(Part part) {
		part.commitEventsRemove();
		synchronized(this) {
		parts.remove(part);
                }
		if (part.isSelected())
			part.lane.project.getPartSelection().removeSelected(part);
		project.getEditHistoryContainer().push(this,
				EditHistoryRecordableAction.EDIT_HISTORY_TYPE_REMOVE, part);
	}

	public void add(Part part) {
	//	System.out.println("Lane.add " + part);
		synchronized(this) {
		parts.add(part);
                }
                
		part.lane = this;
		part.commitEventsAdd();
		project.getEditHistoryContainer().push(this,
				EditHistoryRecordableAction.EDIT_HISTORY_TYPE_ADD, part);
	}

	public boolean isSelected() {
		return selected;
	}

	public int getHeight() {
		return height;
	}

//	public int getColorID() {
//		return colorID;
//	}

	public ProjectContainer getProject() {
		return project;
	}

	public void setSelected(boolean b) {
		selected = b;
	}

	public String getName() {
		return name;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * 
	 * @param lane
	 *            to add to children.
	 */
	public void addChildLane(Lane lane) {
		if (lane.indexInList == -1)
			children.add(lane);
		else
			children.add(lane.indexInList, lane);

		if (lane instanceof MidiLane) {
			((MidiLane) lane).attachFTW();
		}
	}
	
	public void addChildLane(int indexInList, Lane lane) {
		children.add(indexInList, lane);

		if (lane instanceof MidiLane) {
			((MidiLane) lane).attachFTW();
		}
	}
	

	/**
	 * Lane to add
	 * 
	 * @param lane
	 *            to remove
	 */
	public void removeChildLane(Lane lane) {

		int pos = children.indexOf(lane);
		lane.indexInList = pos;
		children.remove(lane);
		if (lane instanceof MidiLane)
			((MidiLane) lane).detachFTW();

	}

	/**
	 *  Return a flat view of the decendent lanes.  
	 * 
	 * @return a List of all the lanes including this and all decendents
	 */
	public List<Lane> getFamilyLanes() {
		Vector<Lane> lanes = new Vector<Lane>();
		lanes.add(this);
		if (children != null)
			for (Lane child : children) {
				lanes.addAll(child.getFamilyLanes());
			}
		return lanes;
	}

	/**
	 * If a lane is hidden it will not be displayed in the lane panel. Note that
	 * it's children might still be displayed (see isOpen())
	 * 
	 * @return
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * If a lane is open it's children might be displayed in the lane panel. if
	 * it is not open then none of it's decendents will be in the lane panel.
	 * 
	 * @return
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * 
	 * @return list of the Lanes children (contents of this folder)
	 */
	public List<Lane> getChildren() {
		return children;
	}

	/**
	 * Used by the partview for mapping onto the screen.
	 * 
	 * @return distance from the top of the display.
	 */
	public int getDisplayY() {
		return displayY;
	}

	public int getDisplayH() {
		return displayH;
	}

	void setDisplayPos(int y, int h, int id) {
		displayY = y;
		displayH = h;
		visibleID = id;
	}

	/**
	 * For the partview GUI.
	 * 
	 * @return lane position in the visible lane list
	 */
	public int getDisplayID() {
		return visibleID;
	}

	/**
	 * 
	 * Hide the lane in the part view. You will need to get the partview to
	 * rebuild for this to take effect.
	 */
	public void setHidden(boolean b) {
		hidden = b;
	}

	/**
	 * 
	 * Open the lane in the part view. You will need to get the partview to
	 * rebuild for this to take effect.
	 */
	public void setOpen(boolean b) {
		open = b;
	}

	public void setName(String name) {
		this.name = name;
		channelLabel = new MetaInfo(name);
	}

	/**
	 * Actions to be done when this lane is loaded
	 */
	public void onLoad() {
	//	System.out.println(" loading " + getParts().size());
		if (getParts() == null)
			return;
		for (Part part : getParts()) {
			try {
				part.onLoad();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (Lane lane : getChildren())
			lane.onLoad();
	}

	/**
	 * Remove the lane from the project.
	 * Override to do any subclass specific stuff.
	 * (used by undo) 
	 */
	public void removeFromModel() {
		project.remove(this);
		detachComponents();
		System.out.println(" REMOVE LANE ");
	}

	private void detachComponents() {
//		for (Part part : getParts()) {
//			part.detach();
//		}

		for (Lane lane : getChildren())
			lane.detachComponents();
	}


	/**
	 * Add the lane from the project.
	 * Override to do any subclass specific stuff.
	 * (used by redo)
	 */

	public void addToModel() {
		project.add(this);
		attachComponents();
		System.out.println(" Add LANE ");
	}

	/**
	 * For undo/redo
	 * 
	 */
	private void attachComponents() {
//		for (Part part : getParts()) {
//			part.attach();
//		}

		for (Lane lane : getChildren())
			lane.attachComponents();

	}

	public long leftTickForMove() {
		return 0;
	}

	public long rightTickForMove() {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		// System. out.println(colorID);
		// need to do this to make sure we get new colours creating lanes later
	//	colorCount = Math.max(colorCount, colorID + 1);
		height = 1;
		setName(name); // just to set the channelInfo
	}

	public void setHeight(int i) {
		height = i;
	}

	public void displayStructure(String prefix, PrintStream out) {
		out.println(prefix + " " + toString());
		prefix += "*";
		if (children != null)
			for (Lane lane : children) {
				lane.displayStructure(prefix, out);
			}
		if (parts != null)
			for (Part part : parts) {
				part.displayStructure(prefix, out);
			}
	}

	abstract public Part createPart();
	
	/**
	 * 
	 * @return icon for the gui (e.g. lane header)
	 */
	public abstract Icon getIcon();
}
