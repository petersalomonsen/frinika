// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

/**
 * An AudioTimingStategy which uses Thread.sleep(long millis, long nanos)
 * and runs with priority Thread.MAX_PRIORITY.
 */
public class SleepTimingStrategy implements AudioTimingStrategy
{
    public int getThreadPriority() {
        return Thread.MAX_PRIORITY;
    }

    public void block(long nowNanos, long blockNanos) {
        try {
	        Thread.sleep(blockNanos / ONE_MILLION, (int)(blockNanos % ONE_MILLION));
        } catch ( InterruptedException ie ) {
        }
    }
}
