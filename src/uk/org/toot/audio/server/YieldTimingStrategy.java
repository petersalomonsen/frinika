// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

/**
 * An AudioTimingStategy which uses Thread.yield() and runs with priority
 * Thread.NORM_PRIORITY+1 to avoid blocking other threads excessively.
 * In practice this strategy is no better than the efficient SleepTimingStrategy
 * and all strategies are at the whim of Garbage Collection.
 */
public class YieldTimingStrategy implements AudioTimingStrategy
{
    public int getThreadPriority() {
        return Thread.NORM_PRIORITY+1;
    }

    public void block(long nowNanos, long blockNanos) {
        long untilNanos = nowNanos + blockNanos;
        while ( System.nanoTime() < untilNanos ) {
            Thread.yield();
        }
    }
}
