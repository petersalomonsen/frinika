

package com.frinika.sequencer.gui.clipboard;


import java.util.Collection;
import java.util.Vector;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.Selectable;

public class MyClipboard { // implements SelectionListener<Part> {

	enum CType {
		PART, EVENT
	};

	CType cType;

	/*
	 * int pasteCount=0; long destTick1; long deltaTick;
	 */

	private static final Class EVENT = null;

	//private static final ProjectContainer ProjectFrame.fo = null;

	Vector<Selectable> copy;

	ProjectContainer srcProject;

	long leftTickOfCopy;

	long rightTickOfCopy;

	private int leftColumnOfCopy; // Used by tracker editor

	// private boolean blocknotify=false;

	private MyClipboard() {
//		this.project = project;
	}

	static MyClipboard instance;
	
	static public MyClipboard the() {
		if (instance == null) {
			instance=new MyClipboard();
		}
		return instance;
		
	}
	public void copy(Collection<Selectable> list, long selectionStartTick,
			int selectionLeftColumn,ProjectContainer srcProject) {
		copy(list,srcProject);
		leftTickOfCopy = selectionStartTick;
		leftColumnOfCopy = selectionLeftColumn;
		System.out.println("Left tracker column of copy: " + leftColumnOfCopy);

	}

	public void copy(Collection<? extends Selectable> collection,ProjectContainer srcProject) {
		this.srcProject=srcProject;
		copy = deepCopy(collection, null);
		leftTickOfCopy = Long.MAX_VALUE;
		rightTickOfCopy = Long.MIN_VALUE;
		leftColumnOfCopy = 0;

		if (copy.elementAt(0) instanceof Part)
			cType = CType.PART;
		if (copy.elementAt(0) instanceof MultiEvent)
			cType = CType.EVENT;

		for (Selectable it : copy) {
			if (it == null) continue; // HACK if 
			leftTickOfCopy = Math.min(leftTickOfCopy, it.leftTickForMove());
			rightTickOfCopy = Math.max(rightTickOfCopy, it.rightTickForMove());
		}
		System.out.println("Left tick of copy: " + leftTickOfCopy);
		
	}

	/**
	 * Default paste action to the sequencers tickposition and tracker column 0
	 * 
	 */
	public void paste(ProjectContainer dstProject) {
	//	ProjectContainer project=ProjectFrame.getFocusFrame().getProjectContainer();
		paste(dstProject.getSequencer().getTickPosition(), 0, false,dstProject);
	}

	/**
	 * Paste from the clipboard
	 * 
	 * @param tickDest -
	 *            the destination tick for the start of paste
	 * @param selectionLeftColumn -
	 *            used by tracker to determine which column to start the pasting
	 *            into
	 * @param trackerOverrideSnap - when pasting from the tracker - snap to the row rather than the piano roll snap setting
	 */
	public void paste(long tickDest, int selectionLeftColumn, boolean trackerOverrideSnap,ProjectContainer dstProject ) {

		if (copy == null || copy.size() == 0) {
			System.out.println(" Nothing in the paste buffer to paste!");
			return;
		}

		long dTick = tickDest - leftTickOfCopy;
		long tNew = -1;
		// long dur= rightTickOfCopy-leftTickOfCopy;
		Part srcPart = null;
		Part dstPart = null;
		if (cType == CType.EVENT) {
			if(!trackerOverrideSnap)
				dTick = dstProject.eventQuantize(dTick);
			srcPart = ((MultiEvent) copy.elementAt(0)).getPart();
			dstPart = dstProject.getPartSelection().getFocus();
			if (dstPart == null) {
				System.out
						.println(" Please set part focus before paste operation");
				return;
			}
		} else if (cType == CType.PART) {
			dTick = dstProject.partQuantize(dTick);
		}

		Vector<Selectable> paste=null;
		
		if ( dstProject != srcProject && cType == CType.PART) {
			Lane lane = dstProject.getLaneSelection().getFocus();
			if (lane == null) {
				System.out.println(" Please select a lane in the destination project ");
				return;
			}
			paste = deepCopy(copy, lane);
		} else {
			paste = deepCopy(copy, dstPart);
		}
		
		
		int deltaTrackerColumn = selectionLeftColumn - leftColumnOfCopy;
		System.out.println("Paste selectionLeftColumn: " + selectionLeftColumn
				+ " leftColumnOfCopy:" + leftColumnOfCopy
				+ " deltaTrackerColumn: " + deltaTrackerColumn);
		for (Selectable it : paste) {
			if (it instanceof MultiEvent) {
				MultiEvent evt = (MultiEvent) it;
				if (evt.getTrackerColumn() != null)
					// If there already is a tracker column then move it
					// according to deltaTrackerColumn
					evt.setTrackerColumn(evt.getTrackerColumn()
							+ deltaTrackerColumn);
				else
					evt.setTrackerColumn(selectionLeftColumn);
			}
			it.deepMove(dTick);
			
			it.addToModel();
		}

		/**
		 * set the pasted items as selected
		 */
		if (cType == CType.EVENT) {
			dstProject.getMultiEventSelection().setSelectedX(paste);
		} else if (cType == CType.PART) {
			dstProject.getPartSelection().setSelectedX(paste);
		}
	}

	/*
	 * if (pasteCount == 0) { destTick1=tickDest; } else if (pasteCount == 1){
	 * deltaTick = destTick1-tickDest; if (cType == CType.EVENT || cType
	 * ==CType.PART) { blocknotify=true;
	 * project.getSequencer().setTickPosition(tickDest + deltaTick);
	 * blocknotify=false; } } pasteCount++;
	 */

	private Vector<Selectable> deepCopy(
			Collection<? extends Selectable> collection, Selectable newParent) {
		Vector<Selectable> ret = new Vector<Selectable>();
		for (Selectable it : collection) {
			Selectable itClone = it.deepCopy(newParent);
			ret.add(itClone);
		}
		return ret;
	}

}
