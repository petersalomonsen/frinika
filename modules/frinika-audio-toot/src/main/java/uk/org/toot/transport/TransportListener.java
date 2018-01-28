// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

/**
 * A TransportListener listens to a Transport.
 * The Transport is either playing or stopped. It may be recording.
 * If it's stopped, you SHOULD BE ABLE TO change the location.
 **/
public interface TransportListener
{
    /**
     * Called when the transport has stopped.
     */
    void stop();

    /**
     * Called when the transport begins playing.
     */
    void play();

    /**
     * Called when the transport record mode changes.
     */
	void record(boolean rec);

    /**
     * Called when the transport locates to a new microsecond time.
     */
    void locate(long microseconds);
}
