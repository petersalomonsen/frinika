// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

import java.util.List;
import java.util.Collections;
import java.util.Observable;

public abstract class AbstractAudioDevice extends Observable implements AudioDevice
{
    protected List<AudioInput> inputs;
    protected List<AudioOutput> outputs;
    private String name;

    public AbstractAudioDevice(String name) {
        this.name = name;
        inputs = new java.util.ArrayList<AudioInput>();
        outputs = new java.util.ArrayList<AudioOutput>();
    }

    public List<AudioInput> getAudioInputs() {
        return Collections.unmodifiableList(inputs);
    }

    public List<AudioOutput> getAudioOutputs() {
        return Collections.unmodifiableList(outputs);
    }

    public String getName() {
        return name;
    }

    public String toString() {
    	return name;
    }
    
    protected void addAudioInput(AudioInput input) {
        inputs.add(input);
        setChanged();
        notifyObservers(input);
    }

    protected void removeAudioInput(AudioInput input) {
        inputs.remove(input);
        setChanged();
        notifyObservers(input);
    }

/*    protected void removeAllAudioInputs() {
   		inputs.clear();
        setChanged();
        notifyObservers();
    } */
    
    protected void addAudioOutput(AudioOutput output) {
        outputs.add(output);
        setChanged();
        notifyObservers(output);
    }

    protected void removeAudioOutput(AudioOutput output) {
        outputs.remove(output);
        setChanged();
        notifyObservers(output);
    }

/*    protected void removeAllAudioOutputs() {
   		outputs.clear();
        setChanged();
        notifyObservers();
    } */
    
}
