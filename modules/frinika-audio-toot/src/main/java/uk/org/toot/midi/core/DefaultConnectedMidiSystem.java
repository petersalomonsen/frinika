// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import java.util.List;
import java.util.Collections;

/**
 * This class adds a composition of MidiConnections to the composition
 * of MidiDevices.
 */
public class DefaultConnectedMidiSystem extends DefaultMidiSystem
    implements ConnectedMidiSystem
{
    private List<MidiConnection> connections;

    public DefaultConnectedMidiSystem() {
        connections = new java.util.ArrayList<MidiConnection>();
    }

    public void notifyObservers() {
    	checkConnections();
    	super.notifyObservers();
    }
    
    // called when the system has changed
    // if a port has been removed, remove it's connections
    protected void checkConnections() {
    	List<MidiInput> inputs = getMidiInputs();
    	List<MidiOutput> outputs = getMidiOutputs();
    	List<MidiConnection> remove = new java.util.ArrayList<MidiConnection>();
    	for ( MidiConnection connection : connections ) {
    		MidiInput input = connection.getMidiInput();
    		MidiOutput output = connection.getMidiOutput();
    		if ( inputs.contains(input) && outputs.contains(output) ) continue;
    		remove.add(connection);
    	}
    	for ( MidiConnection connection : remove ) {
    		connections.remove(connection); // avoid notification reentrancy
    	}
    	
    }
    
    public List<MidiConnection> getMidiConnections() {
        return Collections.unmodifiableList(connections);
    }

    public void createMidiConnection(MidiOutput from, MidiInput to, int flags) {
    	MidiConnection connection = new MidiConnection(from, to, flags);
    	connections.add(connection);
    	setChanged();
    	notifyObservers();    	
    }
    
    public void closeMidiConnection(MidiOutput from, MidiInput to) {
    	MidiConnection connection = getConnection(from, to); 
    	connections.remove(connection);
    	connection.close();
    	setChanged();
    	notifyObservers();    	
    }
    
    public void createMidiConnection(String fromPortName, String toPortName, int flags) {
    	MidiOutput from = (MidiOutput)getPort(fromPortName, true);
    	MidiInput to = (MidiInput)getPort(toPortName, false);
    	if ( from == null ) System.err.println(fromPortName+" not found");
    	if ( to == null ) System.err.println(toPortName+" not found");
    	createMidiConnection(from, to, flags);
    }

    public void closeMidiConnection(String fromPortName, String toPortName) {
    	MidiOutput from = (MidiOutput)getPort(fromPortName, true);
    	MidiInput to = (MidiInput)getPort(toPortName, false);
    	closeMidiConnection(from, to);
    }

    protected MidiConnection getConnection(MidiOutput from, MidiInput to) {
    	for ( MidiConnection c : connections ) {
    		if ( c.getMidiOutput() == from && c.getMidiInput() == to ) {
    			return c;
    		}
    	}
    	throw new IllegalArgumentException(
    		"MidiConnection from "+from.getName()+" to "+to.getName());
    }
    
/*    protected MidiDevice getDevice(String name, boolean isOut) {
    	for ( MidiDevice device : getMidiDevices() ) {
    		String deviceName = device.getName();
    		List<? extends MidiPort> ports = 
    			isOut ? device.getMidiOutputs() : device.getMidiInputs();
    		for ( MidiPort port : ports ) {
    			if ( name.equals(deviceName+" "+port.getName()) ) {
    				return device;
    			}
    		}
    	}
    	return null;
    } */

    protected MidiPort getPort(String name, boolean isOut) {
    	for ( MidiDevice device : getMidiDevices() ) {
//    		String deviceName = device.getName();
    		List<? extends MidiPort> ports = 
    			isOut ? device.getMidiOutputs() : device.getMidiInputs();
    		for ( MidiPort port : ports ) {
    			if ( name.equals(port.getName()) ) {
    				return port;
    			}
    		}
    	}
    	String portString = isOut ? "MidiOutput" : "MidiInput";
		throw new IllegalArgumentException(name+" "+portString+" not found");
    }

}
