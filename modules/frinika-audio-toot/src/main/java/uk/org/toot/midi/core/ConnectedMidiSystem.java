// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core;

import java.util.List;

public interface ConnectedMidiSystem extends MidiSystem
{
    void createMidiConnection(MidiOutput from, MidiInput to, int flags);

    void closeMidiConnection(MidiOutput from, MidiInput to);

    void createMidiConnection(String fromPortName, String toPortName, int flags);
 
    void closeMidiConnection(String fromPortName, String toPortName);

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..* 
     */
    /*#MidiConnection lnkMidiConnection;*/
    List<MidiConnection> getMidiConnections();
}
