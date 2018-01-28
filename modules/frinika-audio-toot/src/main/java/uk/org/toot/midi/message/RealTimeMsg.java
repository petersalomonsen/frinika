/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.InvalidMidiDataException;

/**
 * The class for creating and accessing 1 byte MidiMessages representing
 * System Real Time messages without knowledge of the implementation class.
 * 
 * They ought to be possible to interrupt Sysex messages.
 */
public class RealTimeMsg extends ShortMsg
{
    static public boolean isRealTime(MidiMessage msg) {
        return isRealTime(getStatus(msg));
    }

    static public boolean isRealTime(int status) {
        return isShort(status) && (status >= BASE) && (status < (BASE+N));
    }

    /**
     * Override ShortMsg to avoid object creation
     * All our messages are single byte constants
     */
    static public MidiMessage createRealTime(int status)
    	throws InvalidMidiDataException {
        int i = status - BASE;
        if ( i >= 0 && i < messages.length )
	        return messages[i];
        throw new InvalidMidiDataException(status+" is NOT a RealTimeMsg");
    }

    // System real-time messages

    /**
     * Status byte for Timing Clock messagem (0xF8, or 248).
     */
    public static final int TIMING_CLOCK				= 0xF8; // 248

    /**
     * Status byte for Start message (0xFA, or 250).
     */
    public static final int START						= 0xFA; // 250

    /**
     * Status byte for Continue message (0xFB, or 251).
     */
    public static final int CONTINUE					= 0xFB; // 251

    /**
     * Status byte for Stop message (0xFC, or 252).
     */
    public static final int STOP						= 0xFC; //252

    /**
     * Status byte for Active Sensing message (0xFE, or 254).
     */
    public static final int ACTIVE_SENSING				= 0xFE; // 254

    /**
     * Status byte for System Reset message (0xFF, or 255).
     */
    public static final int SYSTEM_RESET				= 0xFF; // 255

    // note 0xFF is META so we reject it
    // but what about when it means SYSTEM_RESET?
    static private final int N = 7;
    static private final int BASE = 0xF8;
    static private MidiMessage[] messages = new MidiMessage[N];

    static {
        for ( int i = 0; i < N; i++ ) {
            try {
            	messages[i] = createShort(BASE+i);
            } catch ( InvalidMidiDataException imde ) {
            }
        }
    }
}
