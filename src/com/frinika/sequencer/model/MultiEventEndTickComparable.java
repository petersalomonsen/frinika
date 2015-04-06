/*
 * Created on May 25, 2006
 *
 * Copyright (c) 2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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

package com.frinika.sequencer.model;

/**
 * A MultiEvent wrapper class for creating a comparable based on the end tick of a MultiEvent.
 * @author Peter Johan Salomonsen
 */
public class MultiEventEndTickComparable implements Comparable {
    MultiEvent multiEvent;
    
    /**
     * Package private constructor - use MultiEvent.getMultiEventEndTickComparable instead
     * @param multiEvent
     */
    MultiEventEndTickComparable(MultiEvent multiEvent)
    {
        this.multiEvent = multiEvent;
    }

    public MultiEvent getMultiEvent() {
        return multiEvent;
    }
    
    public int compareTo(Object obj)
    {
        int ret = new Long(multiEvent.getEndTick()).compareTo(new Long(((MultiEventEndTickComparable)obj).getMultiEvent().getEndTick()));
        // In case the tick is the same use multiEventID for ordering
        if(ret==0 && obj != this)
        {
            return new Long(multiEvent.multiEventID).compareTo(new Long(((MultiEventEndTickComparable)obj).getMultiEvent().multiEventID));
        }
        else
            return ret;
    }    
}
