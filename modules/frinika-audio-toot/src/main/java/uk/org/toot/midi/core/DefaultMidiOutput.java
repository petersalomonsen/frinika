// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import java.util.List;
import javax.sound.midi.MidiMessage;

public class DefaultMidiOutput implements MidiOutput, MidiTransport
{
    private String name;

    /**
     * @supplierCardinality 0..n 
     */
    private List<MidiInput> tos;

    public DefaultMidiOutput(String name) {
        this.name = name;
        tos = new java.util.ArrayList<MidiInput>();
    }

    public void addConnectionTo(MidiInput input) {
        tos.add(input);
    }

    public void removeConnectionTo(MidiInput input) {
        tos.remove(input);
    }

    public int getConnectionCount() {
    	return tos.size();
    }
    
    public void transport(MidiMessage msg, long timestamp) {
        for ( MidiInput to : tos ) {
        	to.transport(msg, timestamp);
        }
    }

    public void setName(String name) {
    	this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public String toString() {
    	return name;
    }
}
