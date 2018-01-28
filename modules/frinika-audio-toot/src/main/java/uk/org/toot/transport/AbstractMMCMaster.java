// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.transport;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.InvalidMidiDataException;

import static uk.org.toot.midi.message.MachineControlMsg.*;

/**
 * An abstract MIDI Machine Control master.
 * Extend this class with a MIDI output port and implement send.
 * The Transport is then the master of the external MIDI port being sent
 * the MIDI Machine Control messages.
 */
abstract public class AbstractMMCMaster implements TransportListener
{
    abstract protected void send(MidiMessage msg);

    protected void send(int cmd) {
        try {
	        send(createMachineControl(cmd));
        } catch ( InvalidMidiDataException imde ) {
        }
    }

    public void stop() {
        send(STOP);
    }

    public void play() {
        send(PLAY);
    }

    public void playDeferred() { // !!! Unknown purpose, thanks MMA
        send(PLAY_DEFERRED);
    }

    public void record(boolean rec) {
        if ( rec ) {
            punchIn();
        } else {
            punchOut();
        }
    }

    public void fastForward() {
        send(FAST_FORWARD);
    }

    public void rewind() {
        send(REWIND);
    }

    public void punchIn() {
        send(PUNCH_IN);
    }

    public void punchOut() {
        send(PUNCH_OUT);
    }

    public void locate(long microseconds) {
        // !!! !!!
    }
}
