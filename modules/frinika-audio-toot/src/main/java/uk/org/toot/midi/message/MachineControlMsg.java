// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.InvalidMidiDataException;

/**
 * This class provides methods and constants to simplify client handling of
 * MIDI Machine Control messages.
 */
public class MachineControlMsg extends UniversalSysexMsg
{
    public static final int MMC = 0x06;

    public static final int STOP = 1;
    public static final int PLAY = 2;
    public static final int PLAY_DEFERRED = 3;
    public static final int FAST_FORWARD = 4;
    public static final int REWIND = 5;
    public static final int PUNCH_IN = 6;
    public static final int PUNCH_OUT = 7;
    public static final int PAUSE = 9;

    /**
     * This method decides whether a MidiMessage is a Machine Control message.
     * Generally you should call this method and receive a true response
	 * before calling accessor methods.
     */
    public static boolean isMachineControl(MidiMessage msg) {
        return isMachineControl(getMessage(msg));
    }

    /**
     * This method decides whether a MidiMessage's data is a Machine Control message.
     * Generally you should call this method and receive a true response
	 * before calling accessor methods.
     */
    public static boolean isMachineControl(byte[] data) {
        return (data[0] == SYSTEM_EXCLUSIVE) &&
             (data[1] == ID_UNIVERSAL_REALTIME) &&
            (data[3] == MMC);
    }

    /**
     * A factory method to create a global MIDI Machine Control message.
     */
    static public MidiMessage createMachineControl(int cmd)
		throws InvalidMidiDataException {
        return createMachineControl(0x7F, cmd); // global
    }

    /**
     * A factory method to create a MIDI Machine Control message with a
     * specific device id.
     */
    static public MidiMessage createMachineControl(int deviceid, int cmd)
		throws InvalidMidiDataException {
        byte[] data = new byte[6];
        createUniversalHeader(data, ID_UNIVERSAL_REALTIME, deviceid, MMC, cmd);
        data[5] = (byte)END_OF_EXCLUSIVE;
        return createSysex(data, 6);
    }

    /**
     * Returns the device id for the assumed MIDI Machine Control message.
     */
    static public int getDeviceId(MidiMessage msg) {
        return getChannel(msg);
    }

    /**
     * Returns the command for the assumed MIDI Machine Control message.
     */
    static public int getCommand(MidiMessage msg) {
        return getSubId2(msg);
    }
}
