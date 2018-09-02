/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

/**
 * The class for creating accessing and mutating 1, 2 and 3 bytes MidiMessages
 * representing System Common messages without knowledge of the implementation
 * class. 
 */

/*
Quarter Frame Messages (2 bytes): 

   F1  [message]

   F1 = Currently unused and undefined System Common status byte

       [message] = 0nnn dddd
 
            dddd = 4 bits of binary data for this Message Type 
 
             nnn =  Message Type: 
                     0 = Frame count LS nibble 
                     1 = Frame count MS nibble
                     2 = Seconds count LS nibble 
                     3 = Seconds count MS nibble 
                     4 = Minutes count LS nibble 
                     5 = Minutes count MS nibble 
                     6 = Hours count LS nibble 
                     7 = Hours count MS nibble and SMPTE Type

	After both the MS nibble and the LS nibble of the above counts are
    assembled, their bit fields are assigned as follows:

		FRAME COUNT:  xxx yyyyy

               xxx = undefined and reserved for future use.
                     Transmitter must set these bits to 0 
                         and receiver should ignore!
                yyyyy = Frame number (0-29)

		SECONDS COUNT:  xx yyyyyy
 
               xx = undefined and reserved for future use.
                      Transmitter must set these bits to 0 
                         and receiver should ignore!
                yyyyyy = Seconds Count  (0-59)
  
		MINUTES COUNT:  xx yyyyyy
  
               xx = undefined and reserved for future use.
                      Transmitter must set these bits to 0 
                          and receiver should ignore!
                yyyyyy = Minutes Count  (0-59)

		HOURS COUNT:  x yy zzzzz
   
               x = undefined and reserved for future use.
                      Transmitter must set this bit to 0 
                          and receiver should ignore!
                      
              yy = Time Code Type:
                     0 = 24 Frames/Second
                     1 = 25 Frames/Second
                     2 = 30 Frames/Second (Drop-Frame)
                     3 = 30 Frames/Second (Non-Drop)

               zzzzz = Hours Count  (0-23)
 */
public class CommonMsg extends ShortMsg
{
    static public boolean isCommon(MidiMessage msg) {
        return isCommon(getStatus(msg));
    }

    static public boolean isCommon(int status) {
        // note 0xF7 is EOX so we reject it
        return isShort(status) && status >= 0xF1 && status < 0xF7;
    }

    static public boolean isMTCQuarterFrame(MidiMessage msg) {
        int status = getStatus(msg);
        return isMTCQuarterFrame(status);
    }

    static public boolean isMTCQuarterFrame(int status) {
        return status == MTC_QUARTER_FRAME;
    }

    static public MidiMessage createMTCQuarterFrame(int qf, int value) 
    	throws InvalidMidiDataException {
        return createShort(MTC_QUARTER_FRAME, (qf & 7) << 4 | (value & 0x0F));
    }

    static public MidiMessage createMTCQuarterFrame(int qf, MTC.Time time, MTC.FrameRate rate) 
    	throws InvalidMidiDataException {
		int value = 0;
		switch ( qf ) {
		case 0: // bits 0..3 of frames
			value = time.frames & 0x0f;
			break;
		case 1: // bit 4 of frames, bits 5..7 are zero
			value = (time.frames & 0x10) >> 4;
			break;
		case 2: // bits 0..3 of seconds
			value = time.seconds & 0x0f;
			break;
		case 3: // bits 4..5 of seconds, bits 6..7 are zero
			value = (time.seconds & 0x30) >> 4;
			break;
		case 4: // bits 0..3 of minutes
			value = time.minutes & 0x0f;
			break;
		case 5: // bits 4..5 of minutes, bits 6..7 are zero
			value = (time.minutes & 0x30) >> 4;
			break;
		case 6: // bits 0..3 of hours
			value = time.hours & 0x0f;
			break;
		case 7: // bit 4 of hours, bits 1 and 2 are rate type, bit 3 is zero (lsb to msb)
			value = rate.getIndex() << 1 | (time.hours & 0x10) >> 4;
			break;
		default:
			throw new InvalidMidiDataException("Invalid MTC Quarter Frame type: "+qf);
		}
		return createMTCQuarterFrame(qf, value);

    }
    static public int getMTCQuarterFrame(MidiMessage msg) {
        return getData1(msg);
    }

    /**
     * Return the 14 bit Song Position Pointer.
     * This is the number of beats since the start of the sequence.
     */
    static public int getSongPositionPointer(MidiMessage msg) {
        if ( getStatus(msg) == SONG_POSITION_POINTER ) {
            return getData1and2(msg);
        }
        return -1;
    }

    // System common messages

    /**
     * Status byte for MIDI Time Code Quarter Frame message (0xF1, or 241).
     */
    public static final int MTC_QUARTER_FRAME			= 0xF1; // 241

    /**
     * Status byte for Song Position Pointer message (0xF2, or 242).
     */
    public static final int SONG_POSITION_POINTER		= 0xF2;	// 242

    /**
     * Status byte for MIDI Song Select message (0xF3, or 243).
     */
    public static final int SONG_SELECT					= 0xF3; // 243

    /**
     * Status byte for Tune Request message (0xF6, or 246).
     */
    public static final int TUNE_REQUEST				= 0xF6; // 246
}
