// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

import javax.sound.midi.MidiMessage;

import static uk.org.toot.midi.message.MachineControlMsg.*;

/**
 * An abstract MIDI Machine Control slave.
 * Extend this class with a MIDI input and call receive with each
 * received MidiMessage.
 * The associated Transport is then slaved to the external MIDI source providing
 * the MIDI Machine Control messages.
 */
abstract public class AbstractMMCSlave
{
    /**
     * @supplierCardinality 1
     * @link aggregation 
     */
	private Transport transport;

    public AbstractMMCSlave(Transport t) {
        transport = t;
    }

    protected void receive(int cmd) {
        switch ( cmd ) {
        case STOP: transport.stop(); break;
        case PLAY: transport.play(); break;
        case PLAY_DEFERRED: transport.play(); break; // ???
        case FAST_FORWARD: break;
        case REWIND: break;
        case PUNCH_IN: transport.record(true); break;
        case PUNCH_OUT: transport.record(false); break;
        default: break;
        }
    }

    protected void receive(MidiMessage msg) {
        if ( !isMachineControl(msg) ) return;
        int cmd = getCommand(msg);
        // if locate, do something else !!! !!!
        receive(cmd);
    }
}
