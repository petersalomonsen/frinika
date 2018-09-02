// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

/**
 * Transport is either playing or stopped.
 * If it's stopped, you SHOULD BE ABLE TO change the location.
 **/
public interface Transport extends TransportListener, TimeSource
{
    /**
     * Add a TransportListener to this Transport.
     */
    void addTransportListener(TransportListener listener);

    /**
     * Removes a TransportListener from this Transport.
     */
    void removeTransportListener(TransportListener listener);

    /**
     * Reports whether this Transport is playing.
     */
    boolean isPlaying();

    /**
     * Reports whether this Transport is recording.
     */
    boolean isRecording();
}
