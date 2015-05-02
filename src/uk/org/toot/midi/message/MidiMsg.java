/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;

/**
 * The class for handling abstract MIDI messages.
 * 
 * Accessors are provided for the Status byte, the Message byte array and the
 * Length of the message. 
 */
public class MidiMsg
{
    public static int getStatus(MidiMessage msg) {
        return msg.getStatus();
    }

    public static byte[] getMessage(MidiMessage msg) {
        return msg.getMessage();
    }

    public static int getLength(MidiMessage msg) {
        return msg.getLength();
    }
}
