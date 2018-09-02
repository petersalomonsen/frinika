// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import java.util.List;
import uk.org.toot.misc.IObservable;

/**
 * An arbitrary composition of MidiInput and MidiOutput instances.
 * MidiInputs and MidiOutputs may not be added by a public API, they
 * are expected to be added by an instance.
 * 
 * @author Steve Taylor
 */
public interface MidiDevice extends IObservable
{
    String getName();

   /**
     * Get the list of MidiInputs for this MidiDevice.
     * @supplierCardinality 0..*
     * @link aggregationByValue
     */
    /*#MidiInput lnkMidiInput;*/
    List<MidiInput> getMidiInputs();

    /**
     * Get the list of MidiOutputs for this MidiDevice.
     * @supplierCardinality 0..*
     * @link aggregationByValue
     */
    /*#MidiOutput lnkMidiOutput;*/
    List<MidiOutput> getMidiOutputs();
    
    void closeMidi();
}
