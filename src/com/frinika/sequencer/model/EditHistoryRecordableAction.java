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


/**
 * EditHistoryRecordableActions handles add/remove operations on editHistoryRecordables
 * It will automatically handle everything involving cloning, undo and redo etc.
 * @author Peter Johan Salomonsen
 */
public class EditHistoryRecordableAction implements EditHistoryAction {
    private int editHistoryType;

    /**
     * Either a MultiEvent or MidiEvent
     */
    
    EditHistoryRecordable recordable;
    EditHistoryRecordable recordableClone = null;
    
    EditHistoryContainer editHistoryContainer;
    EditHistoryRecorder editHistoryRecorder;
    
    public static final int EDIT_HISTORY_TYPE_ADD = 0;
    public static final int EDIT_HISTORY_TYPE_REMOVE = 1;
    
    /**
     * 
     * @param editHistoryContainer
     * @param recorder 
     * @param editHistoryType
     * @param recordable - Either a MultiEvent or MidiEvent
     */
    public EditHistoryRecordableAction(EditHistoryContainer editHistoryContainer,EditHistoryRecorder recorder, int editHistoryType, EditHistoryRecordable recordable)
    {
        this.editHistoryRecorder = recorder;
        this.editHistoryContainer = editHistoryContainer;
        this.editHistoryType = editHistoryType;
        this.recordable = recordable;
        try
        {
            recordableClone = (EditHistoryRecordable)recordable.clone();
        }
        catch(Exception e)
        {
            
        }
    }
    
    /**
     * @return Returns the editHistoryType.
     */
    public int getEditHistoryType() {
        return editHistoryType;
    }

    /**
     * @return Returns the event (Either a MultiEvent or a MidiEvent) affected by this entry
     */
    public EditHistoryRecordable getRecordable() {
        return recordable;
    }

    /**
     * This method should be called by the EditHistory container
     *
     */
    @SuppressWarnings("unchecked")
    public void undo()
    {
        editHistoryContainer.disableRecording();

        if(editHistoryType==EDIT_HISTORY_TYPE_ADD)
        {
            editHistoryRecorder.remove(recordable);
            //recordable.undoAdd();
        }
        else if(editHistoryType==EDIT_HISTORY_TYPE_REMOVE)
        {
            
            if(recordableClone!=null)
            {
                try {
                    /**
                     * We need a clone of the current state if we're going to redo
                     */
                    EditHistoryRecordable redoClone = (EditHistoryRecordable)recordable.clone();
                    recordable.restoreFromClone(recordableClone);
                    recordableClone = redoClone;
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                
            }
            editHistoryRecorder.add(recordable);
            //recordable.undoRemove();
        }
        
        editHistoryContainer.enableRecording();
        //System. out.println("Reverse "+this.toString());
    }
    
    /**
     * This method should be called by the EditHistory container
     *
     */
    @SuppressWarnings({"unchecked"})
    public void redo()
    {
        editHistoryContainer.disableRecording();
        if(editHistoryType==EDIT_HISTORY_TYPE_REMOVE)
        {
            if(recordableClone!=null)
            {
                try {
                    /**
                     * We need a clone of the current state if we're going to undo again
                     */
                    EditHistoryRecordable undoClone = (EditHistoryRecordable)recordable.clone();
                    recordable.restoreFromClone(recordableClone);
                    recordableClone = undoClone;
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                
            }
            editHistoryRecorder.remove(recordable);
            //recordable.redoRemove();
        }
        else if(editHistoryType==EDIT_HISTORY_TYPE_ADD)
        {
            editHistoryRecorder.add(recordable);
            // recordable.redoAdd();
        }
        editHistoryContainer.enableRecording();
    }

    @Override
    public String toString() 
    {
        String editHistoryTypeString = (editHistoryType==EDIT_HISTORY_TYPE_ADD) ? "Add" : "Remove";
        return "EditHistoryEntry: "+editHistoryTypeString+" "+recordable;
    }
    
    /**
     * Return a cloned EditHistoryEntry with the opposite editHistoryType. Used to notify listeners when undoing in order to indicate that
     * the previous action was reversed
     * @return
     */
    public EditHistoryRecordableAction getInvertedClone()
    {
        return new EditHistoryRecordableAction(editHistoryContainer,editHistoryRecorder,(~editHistoryType) & 0x01,recordable);
    }
}
