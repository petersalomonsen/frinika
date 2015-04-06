/*
 * Created on Feb 26, 2006
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
package com.frinika.sequencer.gui.clipboard;

import java.io.Serializable;
import java.util.Collection;
import java.util.Vector;

import com.frinika.sequencer.model.MultiEvent;

/**
 * @author Peter Johan Salomonsen
 */
public class MultiEventClipboardData implements Serializable {
    private static final long serialVersionUID = 1L;

    Vector<MultiEvent> multiEvents = new Vector<MultiEvent>();
    long referenceTick;
    
    /**
     * When inserted the referenceTick is subtracted from all startTicks in the multiEvents.
     * A cloned copy of the multiEvents is kept in this object.
     * @param referenceTick
     * @param multiEvents
     */
    public MultiEventClipboardData(long referenceTick,Collection<MultiEvent> multiEvents)
    {
        this.referenceTick = referenceTick;
        for(MultiEvent multiEvent : multiEvents)
        {
            try {
                MultiEvent me = (MultiEvent)multiEvent.clone();
                me.setStartTick(me.getStartTick()-referenceTick);
                this.multiEvents.add(me);
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return Returns a cloned copy of the multiEvents in this object
     */
    public Collection<MultiEvent> getClonedMultiEvents() {
        Vector<MultiEvent> multiEvents = new Vector<MultiEvent>();
        for(MultiEvent multiEvent : this.multiEvents)
            try {
                multiEvents.add((MultiEvent)multiEvent.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        return multiEvents;
    }
}
