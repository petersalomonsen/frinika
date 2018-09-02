// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import javax.sound.midi.MidiUnavailableException;

/**
 * This class represents a connection from a MidiOutput to a MidiInput.
 * The connection may be created with various flags.
 * 
 * @author st
 */
public class MidiConnection
{
	/**
	 * PLAYBACK indicates that the Midi messages are already recorded 
	 * such that Midi recording can ignore these connections
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
	 * from a different MidiOutput, the connection source is immutable.
	 */
	public final static int OUTPUT_LOCKED = 4;
	
	/**
	 * INPUT_LOCKED indicates that the connection may not ne connected
	 * to a different MidiInput, the connection destination is immutable.
	 */
	public final static int INPUT_LOCKED = 8;
	
    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private MidiOutput from;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private MidiInput to;
    
    private int flags = 0;

    public MidiConnection(MidiOutput from, MidiInput to) {
    	this(from, to, 0);
    }
    
    /**
     * Create a connection from a MidiOutput to a MidiInput with
     * the specified flags.
     * @param from the MidiOutput to connect from
     * @param to the MidiInput to connect to.
     * @param flags for the connection.
     */
    public MidiConnection(MidiOutput from, MidiInput to, int flags) {
    	if ( from == null || to == null ) {
    		throw new IllegalArgumentException("MidiConnection constructor null argument");
    	}
//    	System.out.println("new Connection from "+from.getName()+" to "+to.getName());
        this.from = from;
        this.to = to;
        this.flags = flags;
        from.addConnectionTo(to);
    }

    public void connectTo(MidiInput to) throws MidiUnavailableException {
    	if ( (flags & INPUT_LOCKED) != 0 ) {
    		throw new IllegalStateException("MidiConnection input is locked");
    	}
    	if ( to == null ) {
    		throw new IllegalArgumentException("MidiConnection can't connectTo(null)");
    	}
        from.removeConnectionTo(this.to);
        this.to = to;
        from.addConnectionTo(to);
    }

    public void connectFrom(MidiOutput from) throws MidiUnavailableException {
    	if ( (flags & OUTPUT_LOCKED) != 0 ) {
    		throw new IllegalStateException("MidiConnection output is locked");
    	}
    	if ( from == null ) {
    		throw new IllegalArgumentException("MidiConnection can't connectFrom(null)");
    	}
        this.from.removeConnectionTo(to);
        this.from = from;
        this.from.addConnectionTo(to);
    }
    
    public void close() {
    	from.removeConnectionTo(to);
    	from = null;
    	to = null;
    }
    
    /**
     * @return MidiOutput - the connection source.
     */
    public MidiOutput getMidiOutput() {
    	return from;
    }
    
    /**
     * @return MidiInput - the connection destination.
     */
    public MidiInput getMidiInput() {
    	return to;
    }
    
    public boolean isSystem() {
    	return (flags & SYSTEM) != 0;
    }
    
    public boolean isPlayback() {
    	return (flags & PLAYBACK) != 0;
    }
}
