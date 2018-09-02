/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;

/**
 * The class for creating accessing and mutating 1, 2 and 3 bytes MidiMessages
 * representing Channel Voice and Channel Mode messages without knowledge of
 * the implementation class.
 * 
 * Channel Voice and Mode messages are not distinguished.
 */
public class ChannelMsg extends ShortMsg
{
    /**
     * Determine whether the specified MidiMessage can be handled by this class. 
     */
    static public boolean isChannel(MidiMessage msg) {
        return isChannel(getStatus(msg));
    }

    static public boolean isChannel(int status) {
        return status < 0xF0;
    }

    /**
     * Create a 3 byte MidiMessage with the specifed channel. 
     */
    static public MidiMessage createChannel(int status, int chan, int data1, int data2)
		throws InvalidMidiDataException {
	    ShortMessage msg = (ShortMessage)fastShortPrototype.clone();
        msg.setMessage(status, chan, data1, data2);
    	return msg;
	}

    /**
     * Create a 2 byte MidiMessage with the specifed channel. 
     */
    static public MidiMessage createChannel(int status, int chan, int data1)
		throws InvalidMidiDataException {
    	return createChannel(status, chan, data1, 0);
    }
    
    /**
     * Set all 3 bytes of a 3 byte MidiMessage 
     */
/*    static public MidiMessage setMessage(MidiMessage msg, int status, int chan, int data1, int data2)
    	throws InvalidMidiDataException {
        ((ShortMessage)msg).setMessage(status, chan, data1, data2);
        return msg;
    } */

    /**
     * Get the Command nybble of the status byte for the specified MidiMessage. 
     */
    static public int getCommand(MidiMessage msg) {
        return ((ShortMessage)msg).getCommand();
    }

    static public int getCommand(int status) {
        return (status & 0xF0);
    }

    /**
     * Get the Channel nybble from the status byte for this MidiMessage. 
     */
    static public int getChannel(MidiMessage msg) {
        return ((ShortMessage)msg).getChannel();
    }

    static public int getChannel(int status) {
        return status & 0x0f;
    }

    /**
     * Set the Channel nybble in the status byte for the specified MidiMessage. 
     */
    static public MidiMessage setChannel(MidiMessage msg, int chan)
    	throws InvalidMidiDataException {
        ((ShortMessage)msg).setMessage(getCommand(msg), chan, getData1(msg), getData2(msg));
        return msg;
    }

    static public int setChannel(int status, int chan) {
        return (status & 0xF0) | (chan & 0x0F);
    }

    // Channel voice message upper nibble defines

    /**
     * Command value for Note Off message (0x80, or 128)
     */
    public static final int NOTE_OFF					= 0x80;  // 128

    /**
     * Command value for Note On message (0x90, or 144)
     */
    public static final int NOTE_ON						= 0x90;  // 144

    /**
     * Command value for Polyphonic Key Pressure (Aftertouch) message (0xA0, or 128)
     */
    public static final int POLY_PRESSURE				= 0xA0;  // 160

    /**
     * Command value for Control Change message (0xB0, or 176)
     */
    public static final int CONTROL_CHANGE				= 0xB0;  // 176

    /**
     * Command value for Program Change message (0xC0, or 192)
     */
    public static final int PROGRAM_CHANGE				= 0xC0;  // 192

    /**
     * Command value for Channel Pressure (Aftertouch) message (0xD0, or 208)
     */
    public static final int CHANNEL_PRESSURE			= 0xD0;  // 208

    /**
     * Command value for Pitch Bend message (0xE0, or 224)
     */
    public static final int PITCH_BEND					= 0xE0;  // 224

}


