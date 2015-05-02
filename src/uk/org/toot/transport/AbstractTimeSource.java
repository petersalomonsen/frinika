// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

/**
 * A TimeSource that extrapolates from discrete previously set pairs of times,
 * time source location and System time location, both in microseconds.
 * Since the System time location is continuous, at least from our sampled
 * view of it, we can use it to generate effectively continuous time source
 * locations, even though typically we only know the actual value at highly
 * discrete intervals.
 * Should take account of glitches and wrap-round.
 */
abstract public class AbstractTimeSource implements TimeSource
{
    abstract protected void store(long tsys, long time);

    /**
     * Called with discrete known time source times, in microseconds.
     */
    public void setMicrosecondLocation(long time) {
        store(microsecondTime(), time);
    }

    abstract protected long extrapolate(long tsys);

    /**
     * Called to obtain the time source time, in microseconds, probably much
     * more frequently than time source times are actually updated.
     */
    public long getMicrosecondLocation() {
        return extrapolate(microsecondTime());
    }

    protected long microsecondTime() {
        return (long)(System.nanoTime() / 1000);
    }
}
