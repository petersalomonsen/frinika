// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

/**
 * This interface defines the contract for a timing strategy for an
 * AudioServer.
 */
public interface AudioTimingStrategy
{
    final int ONE_MILLION = 1000000;

    /**
     * Return the required Thread priority for the timing strategy.
     */
    int getThreadPriority();

    /**
     * Block (don't return) for blockNanos.
     * nowNanos is supplied as a convenience.
     */
    void block(long nowNanos, long blockNanos);
}
