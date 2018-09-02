// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.midi.core.channel;

public interface MidiChannelReader {
	/**
     * Get the channel index number.
	 * @return int - the zero-based index of the channel (0..15)
     */
	int getIndex();

    /**
	 * Get the value of the controller with the specified index. This method determines whether the specified controller is 7
     * bit or 14 bit and results in one or two getController() calls as appropriate.
	 */
    int getControl(int control);

/**
 * Obtains the channel's keyboard pressure.
 * @return the pressure with which the keyboard is being depressed, from 0 to 127 (127 = maximum pressure)
 * @see #setChannelPressure(int)
 */
int getChannelPressure();

/**
 * Obtains the current program number for this channel.
 * @return the program number of the currently selected patch
 * @see javax.sound.midi.Patch#getProgram
 * @see javax.sound.midi.Synthesizer#loadInstrument
 * @see #programChange(int)
 */
int getProgram();

/**
 * Obtains the upward or downward pitch offset for this channel.
 * @return bend amount, as a nonnegative 14-bit value (8192 = no bend)
 * @see #setPitchBend(int)
 */
int getPitchBend();

int getVolume();

int getPan();

/**
 * Obtains the current mono/poly mode.
 * @return <code>true</code> if mono mode is on, otherwise <code>false</code> (meaning poly mode is on).
 * @see #setMono(boolean)
 */
boolean getMono();

/**
 * Obtains the current omni mode status.
 * @return <code>true</code> if omni mode is on, otherwise <code>false</code>.
 * @see #setOmni(boolean)
 */
boolean getOmni();

/**
 * Obtains the current mute state for this channel.
 * @return <code>true</code> the channel is muted, <code>false</code> if not
 * @see #setMute(boolean)
 */
boolean getMute();

/**
 * Obtains the current solo state for this channel.
 * @return <code>true</code> if soloed, <code>false</code> if not
 * @see #setSolo(boolean)
 */
boolean getSolo();

/**
 * Obtains the pressure with which the specified key is being depressed.
 * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
 * @return the amount of pressure for that note, from 0 to 127 (127 = maximum pressure)
 * @see #setPolyPressure(int, int)
 */
int getPolyPressure(int noteNumber);

/**
 * Obtains the current value of the specified controller.  The return
 * value is represented with 7 bits. For 14-bit controllers, the MSB and
 * LSB controller value needs to be obtained separately. For example,
 * the 14-bit value of the volume controller can be calculated by
 * multiplying the value of controller 7 (0x07, channel volume MSB) with 128 and adding the value of controller 39
 * (0x27, channel volume LSB).
 * @param controller the number of the controller whose value is desired. The allowed range is 0-127; see the MIDI
 * 1.0 Specification for the interpretation.
 * @return the current value of the specified controller (0 to 127)
 * @see #controlChange(int, int)
 */
int getController(int controller);

void decode(int command, int data1, int data2);
}


