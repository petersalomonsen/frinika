package com.frinika.sequencer.model;
/*
 * Created on Mar 5, 2006
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * Objects which changes are to be recorded in the editHistoryContainer should implement this interface
 *  
 * EditHistory container records adding and removal of objects - if you need to change a property on the 
 * object you should remove it first, do your changes, and add it again.
 * 
 * On removal the EditHistoryContainer will attempt to clone the object - and if restored later
 * it will use the restoreFromClone method to restore the cloned data on the original instance.
 * @author Peter Johan Salomonsen
 */
public interface EditHistoryRecordable {
    /**
     * If restoration from a clone is needed - this method should return a clone
     * otherwise throw the CloneNotSupportedException
     * @return
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException;
    /**
     * On removal the EditHistoryContainer will attempt to clone the object - and if restored later
     * it will use the restoreFromClone method to restore the cloned data on the original instance.
     * @param object
     */
    public abstract void restoreFromClone(EditHistoryRecordable object);
}
