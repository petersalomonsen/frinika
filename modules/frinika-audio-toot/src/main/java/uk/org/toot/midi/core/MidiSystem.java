// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import java.util.List;
import uk.org.toot.misc.IObservable;

/**
 * A composition of MidiDevices
 */
public interface MidiSystem extends IObservable
{
	void addMidiDevice(MidiDevice device);

	void removeMidiDevice(MidiDevice device);
	
    /**
     * @link aggregationByValue
     * @supplierCardinality 0..* 
     */
    /*#MidiDevice lnkMidiDevice;*/
    List<MidiDevice> getMidiDevices();
    
    List<MidiInput> getMidiInputs();

    List<MidiOutput> getMidiOutputs();
    
    /**
     * Close all MidiDevices
     */
    void close();
}
