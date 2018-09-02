/* Copyright (C) 2005, 2009 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

/**
 * This class provides methods to simplify the handling of MIDI Time Code messages.
 * Comments from the wonderful http://home.roadrunner.com/~jgglatt/tech/mtc.htm
 */
public class TimeMsg extends UniversalSysexMsg
{
	// sub ID 2 for createMTCFull() only used interally
    public static final int MTC_FULL		= 0x01;

    // sub ID 2 types to be passed to createMTCSetup()
    
	/* 00 Special refers to the set-up information that affects a unit globally
    (as opposed to individual tracks, sounds, programs, sequences, etc.).
    In this case, the Special Type takes the place of the Event Number.
    Five are defined. Note that types 01 00 through 04 00 ignore the event
    time field. */
    public static final int MTC_SETUP_SPECIAL			= 0x0;

    /* 01/02 Punch In and Punch Out refer to the enabling and disabling of
    record mode on a unit. The Event Number refers to the track to be
    recorded. Multiple punch in/punch out points (and any of the other
    event types below) may be specified by sending multiple Set-Up messages
    with different times. */
    public static final int MTC_SETUP_PUNCH_IN			= 0x1;
    public static final int MTC_SETUP_PUNCH_OUT			= 0x2;

    /* 03/04 Delete Punch In or Out deletes the matching point (time and
    event number) from the Cue List. */
    public static final int MTC_SETUP_DELETE_PUNCH_IN	= 0x3;
    public static final int MTC_SETUP_DELETE_PUNCH_OUT	= 0x4;
    
	/* 05/06 Event Start and Stop refer to the running or playback of an event,
    and imply that a large sequence of events or a continuous event is to be
    started or stopped. The event number refers to which event on the
    targeted slave is to be played. A single event (ie. playback of a
    specific sample, a fader movement on an automated console, etc.) may
    occur several times throughout a given list of cues. These events will
    be represented by the same event number, with different Start and Stop
    times. */
    public static final int MTC_SETUP_EVENT_START		= 0x5;
    public static final int MTC_SETUP_EVENT_STOP		= 0x6;

	/* 07/08 Event Start and Stop with Additional Information refer to an event
    (as above) with additional parameters transmitted in the Set Up message
    between the Time and EOX. The additional parameters may take the form of
    an effects unit's internal parameters, the volume level of a sound
    effect, etc. See below for a description of additional information. */
    public static final int MTC_SETUP_EVENT_START_ADD	= 0x7;
    public static final int MTC_SETUP_EVENT_STOP_ADD	= 0x8;

	/* 09/0A Delete Event Start/Stop means to delete the matching (event
    number and time) event (with or without additional information) from the
    Cue List. */
    public static final int MTC_SETUP_DELET_EVENT_START	= 0x9;
    public static final int MTC_SETUP_DELETE_EVENT_STOP	= 0xA;

	/* 0B Cue Point refers to individual event occurences, such as marking
    "hit" points for sound effects, reference points for editing, and so on.
    Each Cue number may be assigned to a specific reaction, such as a
    specific one-shot sound event (as opposed to a continuous event, which
    is handled by Start/Stop). A single cue may occur several times
    throughout a given list of cues. These events will be represented by the
    same event number, with different Start and Stop times. */
    public static final int MTC_SETUP_CUE_POINT			= 0xB;

	/* 0C Cue Point with Additional Information is exactly like Event
    Start/Stop with Additional Information, except that the event represents
    a Cue Point rather than a Start/Stop Point. */
    public static final int MTC_SETUP_CUE_POINT_ADD		= 0xC;
	
    /* 0D Delete Cue Point means to Delete the matching (event number and time)
    Cue Event with or without additional information from the Cue List. */
    public static final int MTC_SETUP_DELETE_CUE_POINT	= 0xD;

    /* 0E Event Name in Additional Information. This merely assigns a name to a
    given event number. It is for human logging purposes. See Additional
    Information description. */
    public static final int MTC_SETUP_EVENT_NAME_ADD	= 0xE;
    
    // event codes for Setup Special Types

	/* 00 00 Time Code Offset refers to a relative Time Code offset for each
    unit. For example, a piece of video and a piece of music that are
    supposed to go together may be created at different times, and more
    than likely have different absolute time code positions - therefore,
    one must be offset from the other so that they will match up.
    Just like there is one master time code for an entire system, each unit
    only needs one offset value per unit. */
    public static final int MTC_SPECIAL_TIME_CODE_OFFSET	= 0 << 7;

    /* 01 00 Enable Event List means for a unit to enable execution of events
    in its list if the appropriate MTC or SMPTE time occurs. */
    public static final int MTC_SPECIAL_ENABLE_EVENT_LIST	= 1 << 7;

	/* 02 00 Disable Event List means for a unit to disable execution of its
    event list but not to erase it. This facilitates an MTC Event Manager
    in muting particular devices in order to concentrate on others in a
    complex system where many events occur simultaneously. */
    public static final int MTC_SPECIAL_DISABLE_EVENT_LIST	= 2 << 7;

	/* 03 00 Clear Event List means for a unit to erase its entire event list. */
    public static final int MTC_SPECIAL_CLEAR_EVENT_LIST	= 3 << 7;

    /* 04 00 System Stop refers to a time when the unit may shut down. This
    serves as a protection against Event Starts without matching Event
    Stops, tape machines running past the end of the reel, and so on. */
    public static final int MTC_SPECIAL_SYSTEM_STOP			= 4 << 7;

	/* 05 00 Event List Request is sent by a master to an MTC peripheral. If
    the device ID (Channel Number) matches that of the peripheral, the
    peripheral responds by transmitting its entire cue list as a sequence
    of Set Up Messages, starting from the SMPTE time indicated in the Event
    List Request message. */
    public static final int MTC_SPECIAL_EVENT_LIST_REQUEST	= 5 << 7;
    
    public static boolean isMTCFull(MidiMessage msg) {
        return isMTCFull(getMessage(msg));
    }

    public static boolean isMTCFull(byte[] data) {
        return (data[0] == SYSTEM_EXCLUSIVE) &&
             (data[1] == ID_UNIVERSAL_REALTIME) &&
             (data[3] == 0x01) &&
            (data[4] == MTC_FULL);
    }

    /*
	Full Message - (10 bytes)
 
          F0 7F [chan] 01 [sub-ID 2] hr mn sc fr F7    
 
          F0 7F = Real Time Universal System Exclusive Header  
          [chan] = 7F (message intended for entire system)  
          01 = [sub-ID 1], 'MIDI Time Code'  
          [sub-ID 2] = 01, Full Time Code Message  
          hr = hours and type: 0 
               yyzzzzzyy = type: 
                      00 = 24 Frames/Second 
                      01 = 25 Frames/Second 
                      10 = 30 Frames/Second (drop frame)
                      11 = 30 Frames/Second (non-drop frame) 
               zzzzz = Hours (00->23)
          mn = Minutes (00->59)
          sc = Seconds (00->59)
          fr = Frames (00->29)
        F7 = EOX
     */

    public static MidiMessage createMTCFull(int channel, MTC.Time time, MTC.FrameRate rate) 
    	throws InvalidMidiDataException {
    	byte[] data = new byte[10];
    	createUniversalHeader(data, ID_UNIVERSAL_REALTIME, channel, 0x01, MTC_FULL);
    	data[5] = (byte)((time.hours & 0x0f) | (rate.getIndex() << 4));
    	data[6] = (byte)time.minutes;
    	data[7] = (byte)time.seconds;
    	data[8] = (byte)time.frames;
    	data[9] = (byte)END_OF_EXCLUSIVE;
    	return createSysex(data, 10);
    }
    
    public static boolean isMTCSetup(MidiMessage msg) {
        return isMTCSetup(getMessage(msg));
    }

    public static boolean isMTCSetup(byte[] data) {
        return (data[0] == SYSTEM_EXCLUSIVE) &&
            (data[1] == ID_UNIVERSAL_NON_REALTIME) &&
            (data[3] == 0x04);
    }
    
    /*
	Set-Up Messages (13 bytes plus any additional information):

    	F0 7E [chan] 04 [sub-ID 2] hr mn sc fr ff sl sm [add. info] F7

     	F0 7E = Non-Real Time Universal System Exclusive Header

              [chan] = Channel number
               04 = [sub-ID 1], MIDI Time Code
              [sub-ID 2] = Set-Up Type
               00 = Special
               01 = Punch In points
               02 = Punch Out points
               03 = Delete Punch In point 
               04 = Delete Punch Out point 
               05 = Event Start points 
               06 = Event Stop points
               07 = Event Start points with additional info. 
               08 = Event Stop points with additional info. 
               09 = Delete Event Start point
               0A = Delete Event Stop point
               0B = Cue points
               0C = Cue points with additional info 
               0D = Delete Cue point
               0E = Event Name in additional info

          hr = hours and type: 0 yy zzzzz

                yy = type:
                  00 = 24 Frames/Second
                  01 = 25 Frames/Second
                  10 = 30 Frames/Second drop frame 
                  11 = 30 Frames/Second non-drop frame

                zzzzz = Hours (00-23) 
                  mn = Minutes (00-59) 
                  sc = Seconds (00-59) 
                  fr = Frames (00-29) 
                  ff = Fractional Frames (00-99) 
                  sl, sm = Event Number (LSB first) 
                  [add.info.]
          F7 = EOX

	Event Time
		This is the SMPTE/MIDI Time Code time at which the given event is
        supposed to occur. Actual time is in 1/100th frame resoultion, for those
        units capable of handling bits or some other form of sub-frame
        resolution, and should otherwise be self- explanatory.
	Event Number
		This is a fourteen-bit value, enabling 16,384 of each of the above types
        to be individually addressed. "sl" is the 7 LS bits, and "sm" is the 7
        MS bits.

	Additional Information description
	Additional information consists of a nibblized MIDI data stream, LS nibble
    first. The exception is Set-Up Type OE, where the additional information is
    nibblized ASCII, LS nibble first. An ASCII newline is accomplished by
    sending CR and LF in the ASCII. CR alone functions solely as a carriage
    return, and LF alone functions solely as a Line-Feed.

	For example, a MIDI Note On message such as 91 46 7F would be nibblized and
    sent as 01 09 06 04 0F 07. In this way, any device can decode any message
    regardless of who it was intended for. Device-specific messages should be
    sent as nibblized MIDI System Exclusive messages.
    */
    public static MidiMessage createMTCSetup(int channel, MTC.Time time, 
    		MTC.FrameRate rate, int type, int event, byte[] add) throws InvalidMidiDataException {
    	int addlen = add == null ? 0 : 2 * add.length;
		byte[] data = new byte[13+addlen];
		createUniversalHeader(data, ID_UNIVERSAL_NON_REALTIME, channel, 0x04, type);
		data[5] = (byte) ((time.hours & 0x0f) | (rate.getIndex() << 4));
		data[6] = (byte) (time.minutes & 0x3f);				// mn
		data[7] = (byte) (time.seconds & 0x3f);				// sc
		data[8] = (byte) (time.frames & 0x1f);				// fr
		data[9] = (byte) (time.fractionalFrames & 0x7f);	// ff
		data[10] = (byte) (event & 0x7f);					// sl
		data[11] = (byte) ((event >> 7) & 0x7f);			// sm
		// nibblize additional data
		if ( addlen > 0 ) {
			for ( int i = 0, j = 12; i < addlen; i++, j += 2 ) {
				data[j] = (byte)(add[i] & 0x0f); 			// ls nibble
				data[j+1] = (byte)((add[i] >> 4) & 0x0f);	// ms nibble
			}
		}
		data[data.length-1] = (byte) END_OF_EXCLUSIVE;
		return createSysex(data, 13);
    }
    
    // helper method for special case of additional data being ASCII string
    public static MidiMessage createMTCSetupEventName(int channel, MTC.Time time,
    		MTC.FrameRate rate, int event, String name)
    		throws InvalidMidiDataException {
    	byte[] data = name.replaceAll("[^\\p{ASCII}]", "").getBytes();
    	return createMTCSetup(channel, time, rate, MTC_SETUP_EVENT_NAME_ADD, event, data);
    }
}
