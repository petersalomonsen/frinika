// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import java.util.List;
import java.util.Collections;
import java.util.Observable;

public abstract class AbstractMidiDevice extends Observable implements MidiDevice
{
    protected List<MidiInput> inputs;
    protected List<MidiOutput> outputs;
    private String name;

    public AbstractMidiDevice(String name) {
        this.name = name;
        inputs = new java.util.ArrayList<MidiInput>();
        outputs = new java.util.ArrayList<MidiOutput>();
    }

    public List<MidiInput> getMidiInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<MidiOutput> getMidiOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public String getName() {
        return name;
    }

    public String toString() {
    	return name;
    }
    
    protected void addMidiInput(MidiInput input) {
        inputs.add(input);
        setChanged();
        notifyObservers();
    }

    protected void removeMidiInput(MidiInput input) {
        inputs.remove(input);
        setChanged();
        notifyObservers();
    }

    protected void removeAllMidiInputs() {
   		inputs.clear();
        setChanged();
        notifyObservers();
    }
    
    protected void addMidiOutput(MidiOutput output) {
        outputs.add(output);
        setChanged();
        notifyObservers();
    }

    protected void removeMidiOutput(MidiOutput output) {
        outputs.remove(output);
        setChanged();
        notifyObservers();
    }

    protected void removeAllMidiOutputs() {
   		outputs.clear();
        setChanged();
        notifyObservers();
    }
    
}
