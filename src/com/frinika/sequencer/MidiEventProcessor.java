/*
 * Created on 06 August 2005
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

package com.frinika.sequencer;

import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.NoteEvent;

/**
 * 
 * @author Peter Johan Salomonsen
 *
 */
public abstract class MidiEventProcessor {
    long startTick;
    long endTick;
    boolean[] channels = new boolean[16];
    
    public abstract void processNoteEvent(NoteEvent event);
    public abstract void processControllerEvent(ControllerEvent event);

    /**
     * Set channels that may pass through the filter
     * @param channels Array of integers containing channel numbers
     */
    public void setChannels(int[] channels)
    {
        for(int n=0;n<16;n++)
            this.channels[n] = false;
        for(int channel : channels)
            this.channels[channel] = true;
    }

    public boolean canProcessChannel(int channel)
    {
        return channels[channel];
    }
    
    public long getStartTick() {
        return startTick;
    }

    public void setStartTick(long startTick)
    {
        this.startTick = startTick;
    }

    public long getEndTick() {
        return endTick;
    }

    public void setEndTick(long endTick)
    {
        this.endTick = endTick;
    }

}
