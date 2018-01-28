// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.server;

/**
 * A single AudioClient plugs into an AudioServer so that it may access 
 * the server's hardware i/o in real-time.
 * @author st
 *
 */
public interface AudioClient
{
	/**
	 * Called by an AudioServer to process the specified number of frames.
	 * @param nFrames the number of frames to be processed
	 */
    void work(int nFrames);

    /**
     * When not enabled, work() may not be called and should be ignored if it
     * is called.
     */
    void setEnabled(boolean enabled);
}
