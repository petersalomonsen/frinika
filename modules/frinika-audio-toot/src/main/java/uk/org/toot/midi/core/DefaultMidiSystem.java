// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import java.util.List;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

public class DefaultMidiSystem extends Observable implements MidiSystem
{
    private List<MidiDevice> devices;
    private Observer observer;

    public DefaultMidiSystem() {
        devices = new java.util.ArrayList<MidiDevice>();
        observer = new Observer() {
			public void update(Observable arg0, Object arg1) {
				setChanged();
				notifyObservers();
			}        	
        };
    }

    public void addMidiDevice(MidiDevice device) {
        devices.add(device);
        setChanged();
        notifyObservers();
        device.addObserver(observer);
    }

    public void removeMidiDevice(MidiDevice device) {
        device.deleteObserver(observer);
    	devices.remove(device);
        setChanged();
        notifyObservers();
    }

    public List<MidiDevice> getMidiDevices() {
        return Collections.unmodifiableList(devices);
    }
    
    public List<MidiInput> getMidiInputs() {
    	List<MidiInput> inputs = new java.util.ArrayList<MidiInput>();
    	for ( MidiDevice device : devices ) {
    		inputs.addAll(device.getMidiInputs());
    	}
    	return inputs;
    }
    
    public List<MidiOutput> getMidiOutputs() {
    	List<MidiOutput> outputs = new java.util.ArrayList<MidiOutput>();
    	for ( MidiDevice device : devices ) {
    		outputs.addAll(device.getMidiOutputs());
    	}
    	return outputs;
    }
    
    public void close() {
//		System.out.println("Closing Midi Devices");
    	for ( MidiDevice device : devices ) {
    		device.closeMidi();
    	}
//		System.out.println("All Midi Devices Closed");
    }
}
