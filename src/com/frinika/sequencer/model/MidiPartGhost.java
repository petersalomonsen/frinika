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

package com.frinika.sequencer.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.PrintStream;
import java.util.*;

import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.model.Selectable;
import com.frinika.sequencer.gui.partview.PartView;

/**
 * Ghost ("reference-copy") of a MidiPart. 
 * 
 * Ghosts appear as Parts in the Tracker-view, but do not contain their own 
 * events. Instead, Ghosts are internally linked to the origial Part from which
 * they have been created. They represent the original Part transparently, but
 * are not editable themselves. All changed applied to the original Part will
 * immediately take effect on Ghosts also.
 *  
 * @author Jens Gulden
 */
public class MidiPartGhost extends MidiPart implements Ghost, CommitListener {
	
	private static final long serialVersionUID = 1L;

	MidiPart referredPart;
	
	/**
	 * original-part's-multi-event -> cloned-multi-event
	 */
	transient Map<MultiEvent, MultiEvent> committedEvents = new HashMap<MultiEvent, MultiEvent>(); 
	
	public MidiPartGhost(MidiPart referredPart, Lane lane) {
		this();
		this.referredPart = referredPart;
		this.name = "Ghost of " + referredPart.name;
		this.lane = lane;
		referredPart.addCommitListener(this);
	}

	public MidiPartGhost(MidiPart referredPart, long deltaTicks) {
		this(referredPart, referredPart.getLane());
		this.setStartTick(referredPart.getStartTick() + deltaTicks);
		this.setEndTick(referredPart.getEndTick() + deltaTicks);
	}

	/**
	 * for cloning
	 */
	private MidiPartGhost() {
		super();
	}
	
	public MidiPart getReferredPart() {
		return referredPart;
	}

	public void setReferredPart(MidiPart referredPart) {
		this.referredPart = referredPart;
	}
	
	public long getDistance() { // distance to referred original
		return this.getStartTick() - getReferredPart().getStartTick();
	}
	
	@Override
	public void add(MultiEvent ev) {
		// nop - disable
	}

//	@Override
//	public void attach() {
//		super.attach(); // ???
//	}

	@Override
	public Object clone() throws CloneNotSupportedException {
    	MidiPartGhost clone=new MidiPartGhost();
    	clone.referredPart = referredPart; 
       	clone.name = name;
       	clone.lane = lane;
    	clone.setStartTick(getStartTick());
    	clone.setEndTick(getEndTick());
    	clone.selected = false;
    	return clone;
	}

	@Override
	public void commitEventsAdd() {
    	if (multiEvents == null ) return;
    	long distance = getDistance();
        for(MultiEvent multiEvent : getReferredPart().getMultiEvents())  {
        	commitAddClone(multiEvent);
        }
	}

	@Override
	public synchronized void commitEventsRemove() {
		for (MultiEvent event : committedEvents.values()) {
			event.commitRemove();
		}
		committedEvents.clear();
	}

	/**
	 * Called when the original MidiPart has undergone changes.
	 */
	public void commitAddPerformed(MultiEvent event) {
		commitAddClone(event); // mimic the same in ghost
	}

	/**
	 * Called when the original MidiPart has undergone changes.
	 */
	public synchronized void commitRemovePerformed(MultiEvent event) {
		commitRemoveClone(event); // mimic the same in ghost
	}
	
	protected MultiEvent commitAddClone(MultiEvent multiEvent) {
		try {
	    	MultiEvent newEvent = (MultiEvent)multiEvent.clone();
	    	newEvent.part = this;
	    	newEvent.startTick += getDistance();
        	newEvent.commitAdd();
        	if (committedEvents == null) {
        		committedEvents = new HashMap<MultiEvent, MultiEvent>();
        	}
        	committedEvents.put(multiEvent, newEvent);
	    	return newEvent;
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace();
			return null;
		}
	}

	protected void commitRemoveClone(MultiEvent multiEvent) {
		MultiEvent clone = committedEvents.remove(multiEvent);
		if (clone != null) {
			clone.commitRemove();
		}
	}

	@Override
	public void copyBy(double deltaTick, Lane dst) {
		MidiPartGhost clone = new MidiPartGhost(this.getReferredPart(), dst);
		clone.setStartTick(getStartTick()+deltaTick);
		clone.setEndTick(getEndTick()+deltaTick);
	}

	@Override
	public Selectable deepCopy(Selectable parent) {
		try {
			MidiPartGhost clone = (MidiPartGhost)this.clone();
			if (parent == null) {
				clone.lane=lane;
			}
		    clone.name="Copy of "+name;
		    clone.color=color;
		    return clone;
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace();
			return null;
		}
	}

	@Override
	public void deepMove(long tick) {
		setStartTick(getStartTick() + tick);
		setEndTick(getEndTick() + tick);	
	}

//	@Override
//	public void detach() {
//		super.detach(); // ???
//	}

	@Override
	public void drawThumbNail(Graphics2D g, Rectangle rect, PartView panel) {
		int xShift = (int)panel.userToScreen(this.getDistance());
		g.translate(xShift, 0);
		getReferredPart().drawThumbNail(g, rect, panel); // delegate
		g.translate(-xShift, 0);
	}

	@Override
	public EditHistoryContainer getEditHistoryContainer() {
		return getReferredPart().getEditHistoryContainer(); // delegate
	}

	@Override
	public int getMidiChannel() {
		return getReferredPart().getMidiChannel(); // delegate
	}

	@Override
	public SortedSet<MultiEvent> getMultiEvents() {
		return super.getMultiEvents(); // assert .isEmpty()
	}

	@Override
	public SortedSet<MultiEvent> getMultiEventSubset(long startTick, long endTick) {
		return super.getMultiEventSubset(startTick, endTick); // assert .isEmpty()
	}

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public int[] getPitchRange() {
		return getReferredPart().getPitchRange(); // delegate
	}

	@Override
	public FrinikaTrackWrapper getTrack() {
		return super.getTrack();
	}

	@Override
	public void importFromMidiTrack(long startTickArg, long endTickArg) {
		// nop
	}

	@Override
	public void moveContentsBy(double dTick, Lane dstLane) {
		super.moveContentsBy(dTick, dstLane);
	}

	@Override
	protected void moveItemsBy(long deltaTick) {
		commitEventsRemove();
		commitEventsAdd();
	}

	@Override
	public void onLoad() {
		super.onLoad();
	}

	@Override
	public void rebuildMultiEventEndTickComparables() {
		super.rebuildMultiEventEndTickComparables();
	}

	@Override
	public void remove(MultiEvent multiEvent) {
		// nop - disabled
	}

	@Override
	public void restoreFromClone(EditHistoryRecordable o) {
    	MidiPartGhost clone=(MidiPartGhost)o;
       	this.referredPart = clone.referredPart;
       	this.lane = clone.lane;
       	this.name = clone.name;
    	setStartTick(clone.getStartTick());
    	setEndTick(clone.getEndTick());
    	// selection 
    	selected=false; // clone.selected;     
	}

	@Override
	public void setBoundsFromEvents() {
		// nop
	}

	@Override
	public void setName(String name) {
		super.setName(name);
	}

//	@Override
//	public void update(MultiEvent multiEvent) {
//		// nop
//	}

	@Override
	public void addToModel() {
		super.addToModel();
	}

	@Override
	public void displayStructure(String prefix, PrintStream out) {
		super.displayStructure(prefix, out);
	}

//	@Override
//	public int getColorID() {
//		return getReferredPart().getColorID(); // delegate
//	}

	public Color getColor() {
		return getReferredPart().getColor(); // delegate
	}

	
	
	@Override
	public long getDurationInTicks() {
		return getReferredPart().getDurationInTicks(); // delegate
	}

	@Override
	public long getEndTick() {
		return getStartTick() + getDurationInTicks();
	}

	@Override
	public Rectangle getEventBounds() {
		return getReferredPart().getEventBounds(); // delegate ???
	}

	@Override
	public Lane getLane() {
		return super.getLane();
	}

	@Override
	public long getStartTick() {
		return super.getStartTick();
	}

	@Override
	public boolean isSelected() {
		return super.isSelected();
	}

	@Override
	public long leftTickForMove() {
		return super.leftTickForMove();
	}

	@Override
	public void moveBy(long deltaTick) {
		super.moveBy(deltaTick);
	}

	@Override
	public void removeFromModel() {
		getReferredPart().removeCommitListener(this);
		commitEventsRemove();
		super.removeFromModel();
	}

	@Override
	public long rightTickForMove() {
		return super.rightTickForMove();
	}

	@Override
	public void setEndTick(double tick) {
		super.setEndTick(tick); // but will have no effect, see getEndTick()
	}

	@Override
	public void setSelected(boolean b) {
		super.setSelected(b);
	}

	@Override
	public void setStartTick(double tick) {
		super.setStartTick(tick);
	}

}
