/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.InvalidMidiDataException;

/**
 * This class provides methods to simplify client handling of pitched messages.
 * 
 * POLY_PRESSURE is accepted since it contains pitch data which also needs to
 * be e.g. transposed.
 */
public class PitchMsg extends ChannelMsg
{
    /**
     * Determine whether the specified MidiMessage can be handled by this class. 
     */
    static public boolean isPitch(MidiMessage msg) {
        return isPitch(getStatus(msg));
    }

    static public boolean isPitch(int status) {
        int cmd = getCommand(status);
        return (cmd == NOTE_ON) || (cmd == NOTE_OFF) || (cmd == POLY_PRESSURE);
    }

    /**
     * Transpose the specified MidiMessage by the specified number of semitones. 
     */
    static public MidiMessage transpose(MidiMessage msg, int semitones)
    	throws InvalidMidiDataException {
        int note = getData1(msg)+semitones;
        if ( note > 127 ) note = 127;
        else if ( note < 0 ) note = 0;
        return setData1(msg, note);
    }

    /**
     * Get the Pitch byte of the specified MidiMessage. 
     */
    static public int getPitch(MidiMessage msg) {
        return getData1(msg);
    }

    /**
     * Set the Pitch byte for the specified MidiMessage. 
     */
    static public MidiMessage setPitch(MidiMessage msg, int pitch)
    	throws InvalidMidiDataException {
        return setData1(msg, pitch);
    }
}
