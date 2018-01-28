// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control.automation;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.InvalidMidiDataException;
import uk.org.toot.midi.message.SysexMsg;

/**
 * A class for handling Control MidiMessages represented as
 * Non-commercial System Exclusive.
 * It is anticipated that these messages are saved into a MIDI Track
 * that only contains messages that relate to a particular automated
 * thing, initially the AudioMixer. Because of this there is no method
 * for encoding a particular thing into the message which consequently
 * is only 8 bytes long. Also, this message cannot be used in
 * a Type 0 Standard MIDI File with multiple automated things.
 * 
 *     // encode<provider-id><module-id><instance><control-id><value>
 *     // 0 range 127, 		 127,		127,	  		127,    32767
 *     //         small		 small      too big   ok      too big
 *     // actual						7
 *     // could potentially redistribute a few spare bits for extension
 *     //		   				        4
 *     // but basically see what runs out first
 *     // e.g.    1,            17,        2,        3,       242
 *     //         Toot,		 Mod Delay, #2,       Rate,    242
 * 	//						 FX Bus     #3,		  Mute,	   1
 * 
 */
public class ControlSysexMsg extends SysexMsg
{
    private static final int LENGTH = 8;

    public static boolean isControl(MidiMessage msg) {
        return isControl(getMessage(msg));
    }

    public static boolean isControl(byte[] data) {
        return (data[0] == (byte)SYSTEM_EXCLUSIVE) &&
            (data[1] == ID_NON_COMMERCIAL) &&
            data.length == LENGTH; // weak, may not actually be automation
    }

    public static MidiMessage createControl(
        			int providerId, int moduleId, int instanceIndex,
                    int controlId, int value)
        throws InvalidMidiDataException {
        if ( instanceIndex > 7 )
            throw new InvalidMidiDataException(
            	"Instance index "+instanceIndex+" MUST be < 8");
        byte[] data = new byte[LENGTH];
        data[0] = (byte)SYSTEM_EXCLUSIVE;
        data[1] = ID_NON_COMMERCIAL;
        data[2] = (byte)(providerId & 0x7f);
        data[3] = (byte)(moduleId & 0x7f);
        data[4] = (byte)(instanceIndex & 0x07); // only use 3 bits, 4 spare
        data[5] = (byte)(controlId & 0x7f);
        data[6] = (byte)(value & 0x7f);
        data[7] = (byte)((value >> 7) & 0x7f); 	// could borrow 2 bits here
        MidiMessage msg = createSysex(data, LENGTH);
        assert isControl(msg);
        return msg;
    }

    public static int getProviderId(MidiMessage msg) {
        return getMessage(msg)[2] & 0x7F;
    }

    public static int getModuleId(MidiMessage msg) {
        return getMessage(msg)[3] & 0x7F;
    }

    public static int getInstanceIndex(MidiMessage msg) {
        return getMessage(msg)[4] & 0x07;
    }

    public static int getControlId(MidiMessage msg) {
        return getMessage(msg)[5] & 0x7F;
    }

    public static int getValue(MidiMessage msg) {
        byte[] data = getMessage(msg);
        return data[6] + 128 * data[7];
    }
}
