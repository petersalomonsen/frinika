// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

/**
 * This class represents an audio connection.
 * The connection may be created with various flags.
 * 
 * @author st
 */
public abstract class AudioConnection
{
	/**
	 * PLAYBACK indicates that the audio is already recorded 
	 * such that audio recording can ignore these connections
	 */
	public final static int PLAYBACK = 1;
	
	/**
	 * SYSTEM indicates that the connection is created by the system
	 * rather than by a user. Typically this might inhibit a user from
	 * closing a connection.
	 */
	public final static int SYSTEM = 2;
	
	/**
	 * OUTPUT_LOCKED indicates that the connection may not ne connected
	 * from a different AudioOutput, the connection source is immutable.
	 */
	public final static int OUTPUT_LOCKED = 4;
	
	/**
	 * INPUT_LOCKED indicates that the connection may not ne connected
	 * to a different AudioInput, the connection destination is immutable.
	 */
	public final static int INPUT_LOCKED = 8;
	
    private int flags = 0;

    public AudioConnection(int flags) {
    	this.flags = flags;
    }
    
    public abstract void close();
    
     /**
     * @return String - the connection source.
     */
    public abstract String getOutputName();
    
    public abstract String getOutputLocation();
    
    /**
     * @return String - the connection destination.
     */
    public abstract String getInputName();
    
    public boolean isSystem() {
    	return (flags & SYSTEM) != 0;
    }
    
    public boolean isPlayback() {
    	return (flags & PLAYBACK) != 0;
    }
}
