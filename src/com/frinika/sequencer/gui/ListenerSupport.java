/*
 * Created on March 9, 2007
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

package com.frinika.sequencer.gui;

import java.util.Collection;
import java.util.HashSet;

/**
 * Helps handling listeners.

 * @author Jens Gulden
 */
public abstract class ListenerSupport<L, E> {
    
    Collection<L> l;
    
    /** Creates a new instance */
    public ListenerSupport() {
        l = new HashSet<L>();
    }
    
    public void addListener(L listener) {
        l.add(listener);
    }
    
    public void removeListener(L listener) {
        l.remove(listener);
    }
    
    public Collection<L> getListeners() {
        return l;
    }
    
    /**
     * Fire event to all.
     */
    public void notifyListeners(E e) {
        for (L listener : l) {
            notify(listener, e);
        }
    }
    
    /**
     * Fire event to a single listener.
     */
    public abstract void notify(L l, E e);
}
