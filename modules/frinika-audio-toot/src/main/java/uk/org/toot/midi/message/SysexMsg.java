/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.InvalidMidiDataException;

/**
 * The class for creating, accessing and mutating arbitrary length MidiMessages
 * representing System Exclusive messages without knowledge of the
 * implementation class. 
 */
public class SysexMsg extends MidiMsg 
{
    static public boolean isSysex(MidiMessage msg) {
        return isSysex(getStatus(msg));
    }

    // !!! figure out how special is used !!!
    static public boolean isSysex(int status) {
        return status == END_OF_EXCLUSIVE ||
            status == SPECIAL_SYSTEM_EXCLUSIVE ||
            status == SYSTEM_EXCLUSIVE;
    }

    static public MidiMessage createSysex(byte[] data, int length)
		throws InvalidMidiDataException {
        SysexMessage sysex = new SysexMessage();
        sysex.setMessage(data, length);
        return sysex;
	}

    /**
     * Status byte for System Exclusive message (0xF0, or 240).
     */
    public static final int SYSTEM_EXCLUSIVE			= 0xF0; // 240


    /**
     * Status byte for Special System Exclusive message (0xF7, or 247), which is used
     * in MIDI files.  It has the same value as END_OF_EXCLUSIVE, which
     * is used in the real-time "MIDI wire" protocol.
     */
    public static final int SPECIAL_SYSTEM_EXCLUSIVE	= 0xF7; // 247


    /**
     * Status byte for End of System Exclusive message (0xF7, or 247).
     */
    public static final int END_OF_EXCLUSIVE			= 0xF7;

    /**
     * ID byte for Non-commercial/Educational/Research System Exclusive
     */
    public static final int ID_NON_COMMERCIAL	= 0x7D;
}


