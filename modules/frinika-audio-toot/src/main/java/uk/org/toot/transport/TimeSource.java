// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

/**
 * A TimeSource specification, with microsecond accuracy.
 * long microseconds allows times of up to 294 thousand years.
 * If that isn't enough for you please contact me in 200 thousand years
 * and I'll fix it.
 */
public interface TimeSource
{
    /**
     * Returns the current transport time in microseconds.
     */
    long getMicrosecondLocation();
}
