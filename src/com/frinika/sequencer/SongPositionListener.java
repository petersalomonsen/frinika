/*
 * Created on Jul 24, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com / contact@petersalomonsen.com)
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

package com.frinika.sequencer;

/**
 * Use this interface to implement any component that depend on the song position.
 * This could be a metronome, a graphical song position indicator etc.
 * @author Peter Johan Salomonsen
 */
public interface SongPositionListener {
    /**
     * This method is called each time a new tick is played by the sequencer. Note
     * that this method should return as soon as possible (immediately).
     * @param tick
     */
    void notifyTickPosition(long tick);
    
    /**
     * Your implementation should return true if your listener requires to be notified 
     * for each and every tick. Otherwise (which is in most cases) return false. 
     * WARNING: If you return true on this method, you MUST return absolutely immediately 
     * everytime. Otherwise it may result in a halting song.
     * @return
     */
    boolean requiresNotificationOnEachTick();
}
