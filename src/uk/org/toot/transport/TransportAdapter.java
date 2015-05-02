// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

/**
 * A default concrete implementation of a TransportListener which has null
 * implementations of each method.
 * Typically used when only a few methods actually require an implementation.
 */
public class TransportAdapter implements TransportListener
{
    public void stop() { }

    public void play() { }

	public void record(boolean rec) { }

    public void locate(long microseconds) { }
}
