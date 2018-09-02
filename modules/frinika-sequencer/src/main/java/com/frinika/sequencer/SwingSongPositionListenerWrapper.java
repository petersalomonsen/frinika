/*
 * Created on 5.5.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

import javax.swing.SwingUtilities;

public class SwingSongPositionListenerWrapper implements SongPositionListener {

    private class SongPositionListenerRunnable implements Runnable {

        long tick;

        @Override
        public void run() {
            listener.notifyTickPosition(tick);
        }
    }

    SongPositionListener listener;

    public SwingSongPositionListenerWrapper(SongPositionListener listener) {
        this.listener = listener;
    }

    @Override
    public void notifyTickPosition(long tick) {
        SongPositionListenerRunnable r = new SongPositionListenerRunnable();
        r.tick = tick;
        SwingUtilities.invokeLater(r);
    }

    @Override
    public boolean requiresNotificationOnEachTick() {
        return listener.requiresNotificationOnEachTick();
    }
}
