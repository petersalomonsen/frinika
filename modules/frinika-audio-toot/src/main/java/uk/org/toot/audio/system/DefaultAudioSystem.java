// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

import java.util.List;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

public class DefaultAudioSystem extends Observable implements AudioSystem
{
    private List<AudioDevice> devices;
    private Observer observer;
    protected boolean autoConnect = true;

    public DefaultAudioSystem() {
        devices = new java.util.ArrayList<AudioDevice>();
        observer = new Observer() {
			public void update(Observable arg0, Object arg1) {
				setChanged();
				notifyObservers(arg1);
			}        	
        };
    }

    public void addAudioDevice(AudioDevice device) {
    	checkUniqueDeviceName(device);
        devices.add(device);
       	setChanged();
       	notifyObservers(device);
        device.addObserver(observer);
    }

    protected void checkUniqueDeviceName(AudioDevice device) {
    	for ( AudioDevice d : devices ) {
    		if ( d.getName().equals(device.getName()) ) {
    			throw new IllegalArgumentException(
    				"An AudioDevice named "+device.getName()+" already exists");
    		}
    	}
    }
    
    public void removeAudioDevice(AudioDevice device) {
        device.deleteObserver(observer);
    	devices.remove(device);
        setChanged();
        notifyObservers(device);
    }

    public List<AudioDevice> getAudioDevices() {
        return Collections.unmodifiableList(devices);
    }
    
    public List<AudioInput> getAudioInputs() {
    	List<AudioInput> inputs = new java.util.ArrayList<AudioInput>();
    	for ( AudioDevice device : devices ) {
    		inputs.addAll(device.getAudioInputs());
    	}
    	return inputs;
    }
    
    public List<AudioOutput> getAudioOutputs() {
    	List<AudioOutput> outputs = new java.util.ArrayList<AudioOutput>();
    	for ( AudioDevice device : devices ) {
    		outputs.addAll(device.getAudioOutputs());
    	}
    	return outputs;
    }
    
    public void setAutoConnect(boolean autoConnect) {
    	this.autoConnect = autoConnect;
    }
    
    public void close() {
//		System.out.println("Closing Audio Devices");
    	for ( AudioDevice device : devices ) {
    		device.closeAudio();
    	}
//		System.out.println("All Audio Devices Closed");
    }
}
