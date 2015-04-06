/*
 * Created on Apr 7, 2006
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
 * The EditHistoryRecorder interface should be implemented by classes that are able to
 * add and remove EditHistoryRecordables. Example is a part containing MultiEvents - or 
 * lane containing part.
 * 
 * The EditHistoryContainer will call add/remove methods when undoing/redoing.
 * 
 * Based on the EditHistoryRecorder class type, one can also filter the EditHistoryContainer
 * for specific EditHistoryEntries.
 * 
 * @author Peter Johan Salomonsen
 */
public interface EditHistoryRecorder<T> {
    public void add(T t);
    public void remove(T t);
}
