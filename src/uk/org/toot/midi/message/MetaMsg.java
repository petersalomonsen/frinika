/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.InvalidMidiDataException;

/**
 * The class for creating accessing and mutating arbitrary length MidiMessages
 * representing Meta messages without knowledge of the implementation class.
 *
 * Accessors and mutators are provided for Tempo and String attributes.
 *
 * Note that getString() provides a String representation of the data even for
 * those messages where the data bytes cannot be directly converted to a String.
 * 
 * @author Steve Taylor
 */
public class MetaMsg extends MidiMsg 
{
    /**
     * Determine whether the specified MidiMessage can be handled by this class. 
     */
    public static boolean isMeta(MidiMessage msg) {
        return isMeta(getStatus(msg));
    }

    public static boolean isMeta(int status) {
        return status == META;
    }

    /**
     * Create an arbitrary length Meta message of the specified type with the specified data bytes. 
     */
	public static MidiMessage createMeta(int type, byte[] data, int length)
		throws InvalidMidiDataException {
        MetaMessage msg = new MetaMessage();
	    msg.setMessage(type, data, length);
    	return msg;
    }

    /**
     * Create an arbitrary length Meta message with the specified type and the specified String. 
     */
	public static MidiMessage createMeta(int type, String name)
		throws InvalidMidiDataException {
        byte[] bytes = name.getBytes();
    	return createMeta(type, bytes, bytes.length);
    }

    /**
     * Create a Tempo Meta message with the specified tempo in beats per minute. 
     */
    public static MidiMessage createTempo(float bpm)
    	throws InvalidMidiDataException {
        byte[] bytes = tempoBytes(bpm);
        return createMeta(TEMPO, bytes, bytes.length);
    }

    /**
     * Get the String representation of the specified MidiMessage.
     * If the data bytes of the specified MidiMessage do not directly represent
     * a String a String is nonetheless returned which is a simple
     * representation of the data bytes based of knowledge of the data format.
     */
    public static String getString(MidiMessage msg) {
        	byte[] abData = getData(msg);
            int type = getType(msg);
        	if ( type == SMPTE_OFFSET ) {
				return 	  (abData[0] & 0x1F) + ":"
						+ (abData[1] & 0xFF) + ":"
						+ (abData[2] & 0xFF) + "."
						+ (abData[3] & 0xFF) + "."
						+ (abData[4] & 0xFF) + "   ["
                        + ((abData[0] & 0x60) >> 5) + "]";
        	} else if ( type == TIME_SIGNATURE ) {
                return    (abData[0] & 0xFF) + "/"
                       	+ (1 << (abData[1] & 0xFF));
        	} else if ( type == CHANNEL_PREFIX || type == PORT_PREFIX ) {
                return String.valueOf(1+(abData[0] & 0x0f)) ;
        	} else if ( type == TEMPO ) {
               	// tempo in microseconds per beat
				float bpm = getTempo(msg);
				// truncate it to 2 digits after dot
				bpm = (float) (Math.round(bpm*100.0f)/100.0f);
                return String.valueOf(bpm) ;
        	} else if ( type == END_OF_TRACK || type == MAJOR_MINOR_KEY ) {
            	return "";
        	}
	        return new String(abData);
    }

    /**
     * Set the String representation of the specified MidiMessage. 
     */
    public static MidiMessage setString(MidiMessage msg, String str)
    	throws InvalidMidiDataException {
        byte[] bytes = str.getBytes();
        return setData(msg, bytes, bytes.length);
    }

    public static float convertTempo(float value) {
        if (value <= 0) {
            value = 0.1f;
        }
        return 60000000.0f / value;
    }

    /**
     * Get the Tempo value (in beats per minute) from the specified TEMPO MidiMessage, 
     */
    public static float getTempo(MidiMessage msg) {
        if ( getType(msg) == TEMPO ) {
            byte[] abData = getData(msg);
    	    // tempo in microseconds per beat
	        int nTempo = ((abData[0] & 0xFF) << 16) | ((abData[1] & 0xFF) << 8) | (abData[2] & 0xFF);
        	return convertTempo(nTempo);
        }
        return 0.0f;
	}

    /**
     * Set the Tempo (in beats per minute) of the specified TEMPO MidiMessage. 
     */
    public static MidiMessage setTempo(MidiMessage msg, float bpm)
    	throws InvalidMidiDataException {
        if ( getType(msg) == TEMPO ) {
	        byte[] bytes = tempoBytes(bpm);
    	    return setData(msg, bytes, bytes.length);
        }
        return null; // !!!
    }

    /**
     * Return the data bytes representing the specified Tempo. 
     */
    public static byte[] tempoBytes(float bpm) {
        int mpqn = (int)convertTempo(bpm);
        byte[] bytes = new byte[3];
        bytes[2] = (byte)(mpqn & 0xFF);
        bytes[1] = (byte)((mpqn >> 8) & 0xFF);
        bytes[0] = (byte)((mpqn >> 16) & 0xFF);
   		return bytes;
	}

    /**
     * Return the type of the Meta message. 
     */
    static public int getType(MidiMessage msg) {
        return ((MetaMessage)msg).getType();
    }

    /**
     * Return the data byte array (excluding the type byte) for the specified Meta message. 
     */
    static public byte[] getData(MidiMessage msg) {
        return ((MetaMessage)msg).getData();
    }

    /**
     * Set the data bytes for the specified Meta message. The type remains unchanged. 
     */
    static public MidiMessage setData(MidiMessage msg, byte[] data, int length)
    	throws InvalidMidiDataException {
        ((MetaMessage)msg).setMessage(getType(msg), data, length);
        return msg;
    }

    /**
     * Status byte for <code>MetaMessage</code> (0xFF, or 255), which is used
     * in MIDI files.  It has the same value as SYSTEM_RESET, which
     * is used in the real-time "MIDI wire" protocol.
     */
    public static final int META						= 0xFF; // 255

    public final static int SEQUENCE_NUMBER = 0x00;
    public final static int TEXT = 0x01;
    public final static int COPYRIGHT = 0x02;
    public final static int TRACK_NAME = 0x03;
    public final static int INSTRUMENT_NAME = 0x04;
    public final static int LYRIC = 0x05;
    public final static int MARKER = 0x06;
    public final static int CUE_POINT = 0x07;
    public final static int DEVICE_NAME = 0x09;

    public final static int CHANNEL_PREFIX = 0x20;
    public final static int PORT_PREFIX = 0x21;
    public final static int END_OF_TRACK = 0x2f;

    public final static int TEMPO = 0x51;
    public final static int SMPTE_OFFSET = 0x54;
    public final static int TIME_SIGNATURE = 0x58;
    public final static int MAJOR_MINOR_KEY = 0x59;

    public final static int SEQUENCER_SPECIFIC = 0x7f;
}


