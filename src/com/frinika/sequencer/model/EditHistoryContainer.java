/*
 * Created on Dec 11, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import static com.frinika.localization.CurrentLocale.getMessage;

/**
 * The EditHistoryContainer monitors all edits on a EditHistoryRecorders. By setting
 * a marking pont using the mark method - it will be possible to roll back to
 * the state at the marking point at a later stage. It's also possible to roll
 * back and roll forward again. This gives undo/redo features to a
 * EditHistoryRecorder.
 * 
 * How to use? Simply call the mark method before any action or series of
 * actions on the sequence. Set the markstring according to a description of
 * what can be undone.
 * 
 * Remember that all actions on a EditHistoryRecordable has to be either add or
 * remove - also meaning that if you're just going to change a MidiEvent you
 * should always remove it - change it - and then add it to the track again.
 * 
 * If you have undo/redo menuitems reference them with the setMenuItem methods,
 * so that they're constantly updated according to the undo/redo marks. You can
 * also reuse your undo/redo menuitems in other windows. Do this by adding a the
 * undo/redo menuitems from this EditHistoryContainer rather than creating them
 * yourself.
 * 
 * @author Peter Johan Salomonsen
 */
public class EditHistoryContainer {
	private Vector<EditHistoryAction> editHistory = new Vector<EditHistoryAction>();

	private Vector<EditHistoryMark> editHistoryMarks = new Vector<EditHistoryMark>();

	private int redoMarkIndex = 0;

	private JMenuItem undoMenuItem;

	private JMenuItem redoMenuItem;

	private Vector<EditHistoryListener> editHistoryListeners = new Vector<EditHistoryListener>();
   
    /**
     * Set to false during undo or redo operations so that these operations are not recorded as well.
     */
    private boolean recordingEnabled = true;

	private int savedPosition = 0;
    
	public EditHistoryContainer() {
		// Create default undo and redo menuItems

		undoMenuItem = new JMenuItem();
		undoMenuItem.setText(getMessage("edithistorycontainer.editmenu.undo"));
		undoMenuItem.setEnabled(false);
		undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		undoMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				undo();

			}
		});

		redoMenuItem = new JMenuItem();
		redoMenuItem.setText(getMessage("edithistorycontainer.editmenu.redo"));
		redoMenuItem.setEnabled(false);
		redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		redoMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				redo();
			}
		});
	}

	/**
	 * Called by the FrinikaTrackWrapper add and remove methods when there are
	 * changes to the track.
	 * 
	 * @param edit_history_type
	 * @param track
	 * @param event
	 */
	public void push(EditHistoryRecorder recorder, int edit_history_type, EditHistoryRecordable event) {
        if(recordingEnabled)
            editHistory.add(new EditHistoryRecordableAction(this,recorder,edit_history_type, event));
	}

    /**
     * Push a generic editHistoryAction onto the recording
     * @param editHistoryAction
     */
    public void push(EditHistoryAction editHistoryAction)
    {
        if(recordingEnabled)
            editHistory.add(editHistoryAction);
    }
    
	/**
	 * Call the mark method before any action or series of actions on the
	 * sequence. Set the markstring according to a description of what can be
	 * undone.
	 * 
	 * @param markString
	 */
	public void mark(String markString) {
        if(!recordingEnabled)
            return;
        
        // System. out.println(" MARK ");

		if (redoMarkIndex < editHistoryMarks.size()) {
			// If we're pushing a new entry, one should not be able to redo
			int startIndex = editHistoryMarks.get(redoMarkIndex)
					.getEditHistoryIndex();
			int endIndex;
			if (redoMarkIndex < editHistoryMarks.size() - 1)
				endIndex = editHistoryMarks.get(redoMarkIndex + 1)
						.getEditHistoryIndex();
			else
				endIndex = editHistory.size();

			// System. out.println(startIndex+" "+endIndex+"
			// "+editHistory.size());

			for (int n = startIndex; n < endIndex; n++)
				editHistory.remove(editHistory.size() - 1);

			for (int n = redoMarkIndex; n < editHistoryMarks.size(); n++)
				editHistoryMarks.remove(editHistoryMarks.size() - 1);
		}

		editHistoryMarks
				.add(new EditHistoryMark(editHistory.size(), markString));
		redoMarkIndex = editHistoryMarks.size();

		updateMenuItems();
		// System. out.println(redoMarkIndex+" mark: "+markString);
	}

	private void notifyEditHistoryListeners(
		EditHistoryAction[] editHistoryActionArray) {
	//	System .out.println(" NOTIFY ");

		for (EditHistoryListener editHistoryListener : editHistoryListeners)
			editHistoryListener
					.fireSequenceDataChanged(editHistoryActionArray);
	}

	/**
	 * Clients should call this method when done with a marked action, in order
	 * to notify listeners for changes.
	 * 
	 */
	public void notifyEditHistoryListeners() {
		Vector<EditHistoryAction> editHistoryActions = new Vector<EditHistoryAction>();

		if (redoMarkIndex > 0) {
			int lastIndex = editHistoryMarks.get(redoMarkIndex - 1)
					.getEditHistoryIndex();
			int currentIndex;
			if (redoMarkIndex == editHistoryMarks.size())
				currentIndex = editHistory.size() - 1;
			else
				currentIndex = editHistoryMarks.get(redoMarkIndex)
						.getEditHistoryIndex() - 1;

			for (int n = currentIndex; n >= lastIndex; n--)
				editHistoryActions.add(editHistory.get(n));
		}

		/**
		 * TODO: PJS: What is this? Yes it can be zero, since you may undo everything... Why shouldn't it?
		 
		 * if size is zero then nothing has happened ?
		 * 
		 * PJL: OK if everything can cope with it being zero I commented it out because some stuff was falling over.
		 * array out of bounds if I recall correctly. 
		 * Maybe fixed now ?
		if (editHistoryActions.size() == 0 ) {
			try {
				throw new Exception(" Should the editHistory really be zero ");
			} catch (Exception e) {			
				e.printStackTrace();
				return;
			}
			
		}
		*/
		EditHistoryAction[] editHistoryActionArray = new EditHistoryAction[editHistoryActions
				.size()];
		editHistoryActions.toArray(editHistoryActionArray);

		notifyEditHistoryListeners(editHistoryActionArray);
	}

	/**
	 * Add an editHistory listener to this edithistorycontainer
	 * 
	 * @param editHistoryListener
	 */
	public void addEditHistoryListener(EditHistoryListener editHistoryListener) {
		editHistoryListeners.add(editHistoryListener);
	}

	/**
	 * Remove an editHistory listener from this edithistorycontainer
	 * 
	 * @param editHistoryListener
	 */
	public void removeEditHistoryListener(
			EditHistoryListener editHistoryListener) {
		editHistoryListeners.remove(editHistoryListener);
	}

	/**
	 * Redo actions up to the next mark
	 * 
	 */
	public void redo() {
		// Check if there's anything to redo
		if (redoMarkIndex < editHistoryMarks.size()) {
			int startIndex = editHistoryMarks.get(redoMarkIndex)
					.getEditHistoryIndex();
			int endIndex;
			if (redoMarkIndex < editHistoryMarks.size() - 1)
				endIndex = editHistoryMarks.get(redoMarkIndex + 1)
						.getEditHistoryIndex();
			else
				endIndex = editHistory.size();

			EditHistoryAction[] editHistoryActionArray = new EditHistoryAction[endIndex
					- startIndex];
			for (int n = startIndex; n < endIndex; n++) {
				editHistory.get(n).redo();
				editHistoryActionArray[n - startIndex] = editHistory.get(n);
			}

			redoMarkIndex++;

			notifyEditHistoryListeners(editHistoryActionArray);
			updateMenuItems();

			// System. out.println(redoMarkIndex+" redo ");
		}
	}

	/**
	 * Get the descriptive string of the actions that will be rolled back on the
	 * next undo
	 * 
	 * @return
	 */
	public String getNextUndoMarkString() {
		if (redoMarkIndex > 0)
			return editHistoryMarks.get(redoMarkIndex - 1).getMarkString();
		else
			return null;
	}

	/**
	 * Get the descriptive string of the actions that will be redone on the next
	 * redo
	 * 
	 * @return
	 */
	public String getNextRedoMarkString() {
		if (redoMarkIndex < editHistoryMarks.size())
			return editHistoryMarks.get(redoMarkIndex).getMarkString();
		else
			return null;
	}

	/**
	 * Undo actions back to the previous mark
	 * 
	 */
	public void undo() {
		if (redoMarkIndex > 0) {
			int lastIndex = editHistoryMarks.get(redoMarkIndex - 1)
					.getEditHistoryIndex();
			int currentIndex;
			if (redoMarkIndex == editHistoryMarks.size())
				currentIndex = editHistory.size() - 1;
			else
				currentIndex = editHistoryMarks.get(redoMarkIndex)
						.getEditHistoryIndex() - 1;

			EditHistoryRecordableAction[] editHistoryEntryArray = new EditHistoryRecordableAction[currentIndex
					- lastIndex + 1];
			for (int n = currentIndex; n >= lastIndex; n--) {
				editHistory.get(n).undo();
				// Use an inverted editHistoryType when notifying the listeners
                if(editHistory.get(n) instanceof EditHistoryRecordableAction)
                    editHistoryEntryArray[n - lastIndex] = ((EditHistoryRecordableAction)editHistory.get(n))
						.getInvertedClone();
			}

			redoMarkIndex--;

			notifyEditHistoryListeners(editHistoryEntryArray);
			updateMenuItems();
			// System. out.println(redoMarkIndex+" undo ");
		}
	}

	/**
	 * Update undo/redo menuitems so that they show info according to the
	 * undo/redo marks. Eventually disable them if there's nothing to undo or
	 * redo
	 * 
	 */
	public void updateMenuItems() {
		if (undoMenuItem != null) {
			String nextUndoMarkString = getNextUndoMarkString();
			if (nextUndoMarkString != null) {
				undoMenuItem
						.setText(getMessage("edithistorycontainer.editmenu.undo")
								+ " " + nextUndoMarkString);
				undoMenuItem.setEnabled(true);
			} else {
				undoMenuItem
						.setText(getMessage("edithistorycontainer.editmenu.undo"));
				undoMenuItem.setEnabled(false);
			}
		}
		if (redoMenuItem != null) {
			String nextRedoMarkString = getNextRedoMarkString();
			if (nextRedoMarkString != null) {
				redoMenuItem
						.setText(getMessage("edithistorycontainer.editmenu.redo")
								+ " " + nextRedoMarkString);
				redoMenuItem.setEnabled(true);
			} else {
				redoMenuItem
						.setText(getMessage("edithistorycontainer.editmenu.redo"));
				redoMenuItem.setEnabled(false);
			}
		}
	}

	/**
	 * @return Returns the redoMenuItem.
	 */
	public JMenuItem getRedoMenuItem() {
		return redoMenuItem;
	}

	/**
	 * @return Returns the undoMenuItem.
	 */
	public JMenuItem getUndoMenuItem() {
		return undoMenuItem;
	}

	/**
	 * Use to give a menuitem reference to the EditHistoryContainer so that it
	 * can update the menuitem text
	 * 
	 * @param redoMenuItem
	 */
	public void setRedoMenuItem(JMenuItem redoMenuItem) {
		this.redoMenuItem = redoMenuItem;
	}

	/**
	 * Use to give a menuitem reference to the EditHistoryContainer so that it
	 * can update the menuitem text
	 * 
	 * @param redoMenuItem
	 */
	public void setUndoMenuItem(JMenuItem undoMenuItem) {
		this.undoMenuItem = undoMenuItem;
	}

    /**
     * Tell if there has been any edits on this history container
     * @return
     */
	public boolean hasChanges()
    {
        if(redoMarkIndex==savedPosition )
            return false;
        else
            return true;
    }

    /**
     * @return Returns the recordingEnabled.
     */
    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }

    /**
     * Use during undo/redo operations so that these are not recorded in the EditHistory
     */
    public void disableRecording() {
        this.recordingEnabled = false;
    }
    
    /**
     * Use after undo/redo operations to reenable edithistory recording
     *
     */
    public void enableRecording() {
        this.recordingEnabled = true;
    }
    
    /**
     * Called by the ProjectContainer when saving the project so that hasChanges() will return false as long as there are no changes after the save 
     *
     */
    public void updateSavedPosition()
    {
    		savedPosition=redoMarkIndex;
    		notifyEditHistoryListeners();
    }
}
