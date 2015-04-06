/*
 * Created on Feb 19, 2006
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
 * If you want to make sure that the changes in a MultiEvent is recorded in the EditHistory
 * you should wrap this class around your MultiEvent change code. It applies to one single
 * MultiEvent and make sure that all neccesities are done in order to record the change.
 * 
 * Recording a MultiEvent change in the editHistory implies removing the MultiEvent before the
 * change - doing the actual change - and then adding the multiEvent again.
 * 
 * @author Peter Johan Salomonsen
 */
public abstract class MultiEventChangeRecorder {
    
    /**
     * When invoking this constructor all the history recording and the actual change will be done
     * @param changeText - The text to be visible in the undo menu for this change
     * @param multiEvent - The multiEvent you want to change
     */
    public MultiEventChangeRecorder(String changeText, MultiEvent multiEvent)
    {
        multiEvent.getPart().getEditHistoryContainer().mark(changeText);
        multiEvent.getPart().remove(multiEvent);
        doChange(multiEvent);
        multiEvent.getPart().add(multiEvent);
        multiEvent.getPart().getEditHistoryContainer().notifyEditHistoryListeners();
    }
    
    public abstract void doChange(MultiEvent multiEvent);
}
