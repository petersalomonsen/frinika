/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import java.io.File;
import java.io.IOException;

/**
 * The base class for creating accessing and mutating 1, 2 and 3 byte
 * MidiMessages without knowledge of the implementation class.
 * 
 * This class is not normally used directly, rather the specialised classes
 * NoteMsg, ChannelMsg, CommonMsg and RealTimeMsg should probably be used.
 *
 * Accessors and mutators are provided for the Data1 and Data2 bytes and the
 * combined Data1and2 integer.
 */
public class ShortMsg extends MidiMsg 
{
    static MidiMessage fastShortPrototype;

    static { createFastPrototypes(); }

    /**
     * Determine whether the MidiMessage can be handled by this class. 
     */
    static public boolean isShort(MidiMessage msg) {
        return msg instanceof ShortMessage;
    }

    static public boolean isShort(int status) {
        return false; // !!!
    }

    /**
     * Create a 1 byte MidiMessage with the specified Status byte. 
     */
    static public MidiMessage createShort(int status)
		throws InvalidMidiDataException {
    	ShortMessage msg = (ShortMessage)fastShortPrototype.clone();
        msg.setMessage(status);
	    return msg;
    }

    /**
     * Create a 2 byte MidiMessage with the specified Status byte and Data1 byte.
     */
	static public MidiMessage createShort(int status, int data1)
		throws InvalidMidiDataException {
    	return createShort(status, data1, 0);
	}

    /**
     * Create a 3 byte MidiMessage with the specified Status byte and Data1 and
     * Data2 bytes.
     */
	static public MidiMessage createShort(int status, int data1, int data2)
		throws InvalidMidiDataException {
        ShortMessage msg = (ShortMessage)fastShortPrototype.clone();
	    msg.setMessage(status, data1, data2);
    	return msg;
	}

    /**
     * Set the Status byte of a 1 byte MidiMessage. 
     */
    static public MidiMessage setMessage(MidiMessage msg, int status)
    	throws InvalidMidiDataException {
        ((ShortMessage)msg).setMessage(status);
        return msg;
    }

    /**
     * Set the Status byte and Data1 and Data2 bytes of a 2 byte MidiMessage.
     */
    static public MidiMessage setMessage(MidiMessage msg, int status, int data1, int data2)
    	throws InvalidMidiDataException {
        ((ShortMessage)msg).setMessage(status, data1, data2);
        return msg;
    }

    /**
     * A hack to get a real FastShortMessage that can be cloned, e.g. Prototype
     */
    private static void createFastPrototypes() {
        try {
	        // create a sequence with a ShortMessage
    	    Sequence sequence = new Sequence(Sequence.PPQ, 10);
			Track track = sequence.createTrack();
            track.add(new MidiEvent(new ShortMessage(), 0l));
        	// save the sequence
            try {
	            File file = File.createTempFile("FSMhack", "mid");
    	        MidiSystem.write(sequence, 0, file);
		        // load the sequence and get back the FastShortMessage
                sequence = MidiSystem.getSequence(file);
                track = sequence.getTracks()[0];
		        MidiMessage msg = null;
                for ( int i = 0; i < track.size(); i++ ) {
                    msg = track.get(i).getMessage();
                    Class clazz = msg.getClass();
                    if ( fastShortPrototype == null &&
                         	clazz.getCanonicalName().equals("com.sun.media.sound.FastShortMessage") ) {
						fastShortPrototype = msg;
                    }
                }
                file.delete();
            } catch ( IOException ioe ) {
            }

		} catch ( InvalidMidiDataException imde ) {
        }
    }

    /**
     * Get the first data byte of the specified MidiMessage. 
     */
    static public int getData1(MidiMessage msg) {
        return ((ShortMessage)msg).getData1();
    }

    /**
     * Set the first data byte of a 2 or 3 byte MidiMessage. 
     */
    static public MidiMessage setData1(MidiMessage msg, int data1)
    	throws InvalidMidiDataException {
        if ( msg.getLength() == 1 )
            throw new InvalidMidiDataException("Can't setData1 on 1 byte message");
	    ((ShortMessage)msg).setMessage(getStatus(msg), data1, getData2(msg));
        return msg;
    }

    /**
     * Get the second data byte of the specified MidiMessage. 
     */
    static public int getData2(MidiMessage msg) {
        return ((ShortMessage)msg).getData2();
    }

    /**
     * Set the second data byte of a 3 byte MidiMessage. 
     */
    static public MidiMessage setData2(MidiMessage msg, int data2)
    	throws InvalidMidiDataException {
		if ( msg.getLength() != 3 )
            throw new InvalidMidiDataException("Can't setData2 on "+msg.getLength()+" byte message");
        ((ShortMessage)msg).setMessage(getStatus(msg), getData1(msg), data2);
        return msg;
    }

    /**
     * Get the combined value of the first and second data bytes. The first data byte is the least significant byte. 
     */
    static public int getData1and2(MidiMessage msg) {
       	return getData1and2(getData1(msg), getData2(msg));
    }

    static public int getData1and2(int data1, int data2) {
        return (data1 & 0x3F) | (data2 << 7);
    }
}


