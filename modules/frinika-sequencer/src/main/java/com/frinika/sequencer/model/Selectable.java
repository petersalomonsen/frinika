/*
 * Created on Mar 14, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

public interface Selectable {

    /**
     * set selected flag (for GUI use only)
     *
     * @param b
     */
    void setSelected(boolean b);

    /**
     * Complete copy of object.
     *
     * @param parent owner of the new object;
     * @return
     */
    Selectable deepCopy(Selectable parent);

    /**
     * Move object and all children by tick
     *
     * @param tick
     */
    void deepMove(long tick);

    /**
     *
     * Remove from model making sure the history is informed
     */
    void removeFromModel();

    /**
     *
     * Add to the model making sure the history is informed
     */
    void addToModel();

    /**
     * return the left tick mark for move operations without quantize a move to
     * destTick should move item by destTick - leftTickForMove();
     *
     * @return
     */
    long leftTickForMove();

    long rightTickForMove();
}
