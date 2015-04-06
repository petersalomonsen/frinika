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
 * This is a Dummy subclass of MultiEvent used to generate MultiEvents that are ordered as
 * the first among the MultiEvents with the same tick.
 * @author Peter Johan Salomonsen
 */
public final class SubsetMultiEvent extends MultiEvent {
    private static final long serialVersionUID = 1L;

    SubsetMultiEvent(long startTick) {
        super(startTick);
    }

    @Override
    public long getEndTick() {
    	return startTick;
    }
    
    @Override
    void commitRemoveImpl() { // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()
        
    }

    @Override
	public
    void commitAddImpl() { // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw(new CloneNotSupportedException("The SubsetMultiEvent is for internal use, and definately not to be cloned!"));
    }

    /**
     * n/a
     */
	public void restoreFromClone(EditHistoryRecordable object) {
		
	}
}
