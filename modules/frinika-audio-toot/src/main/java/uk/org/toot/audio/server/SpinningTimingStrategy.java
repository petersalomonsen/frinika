// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An AudioTimingStategy which sleeps until the last millisecond before release,
 * and during the last millisecond the CPU is not released, but spinning in the
 * blocking loop.
 * 
 * @author Steve Taylor
 * @author Peter Johan Salomonsen
 */
public class SpinningTimingStrategy implements AudioTimingStrategy
{
    public int getThreadPriority() {
        return Thread.MAX_PRIORITY;
    }

    public void block(long nowNanos, long blockNanos) {
        long untilNanos = nowNanos + blockNanos;
        
        try {
            // Make sure there's always sleep
            Thread.sleep(0,10000);
   
            while ( System.nanoTime() < untilNanos )
            {
                if(untilNanos - System.nanoTime()>1000000L)
                    Thread.sleep(0,10000);
            }
        } catch (InterruptedException ex) {
                Logger.getLogger(SpinningTimingStrategy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
