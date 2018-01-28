/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.core.channel;

import uk.org.toot.midi.core.*;
import javax.sound.midi.MidiMessage;

import javax.sound.midi.InvalidMidiDataException;
import static uk.org.toot.midi.misc.Controller.*;
import static uk.org.toot.midi.message.ChannelMsg.*;

/**
 * This class provides a channel-based write API.
 * Requires no state.
 */
public class DefaultMidiChannelWriter implements MidiChannelWriter
{
    private MidiTransport port;
    private int index;

    public DefaultMidiChannelWriter(MidiTransport port, int index) {
        this.index = index;
        this.port = port;
    }

    public int getIndex() { return index & 0x0F; }

    public void encode(int command, int data1, int data2  ) {
	        try {
        	    MidiMessage msg = createChannel(command, index, data1, data2);
	            port.transport(msg, -1);
	        } catch (InvalidMidiDataException imde) {
    	        imde.printStackTrace();
        	}
    }

    public void encode(int command, int data ) {
        encode(command, data, 0);
    }

    private void setPseudoControl(int control, int value) {
        switch (control) {
            case PITCH_BEND_PSEUDO:
                setPitchBend(value);
                break;
            case POLY_PRESSURE_PSEUDO:
                // !!! !!! !!!
                break;
            case CHANNEL_PRESSURE_PSEUDO:
                setChannelPressure(value);
                break;
            case PROGRAM_PSEUDO:
                programChange(value);
                break;
        }
    }

    /**
     * Changes the pitch offset for all notes on this channel. This affects all currently sounding notes as well as subsequent
     * ones. (For pitch bend to cease, the value needs to be reset to the center position.) <p> The MIDI specification
     * stipulates that pitch bend be a 14-bit value, where zero is maximum downward bend, 16383 is maximum upward bend, and
     * 8192 is the center (no pitch bend).  The actual amount of pitch change is not specified; it can be changed by
     * a pitch-bend sensitivity setting.  However, the General MIDI
     * specification says that the default range should be two semitones up and down from center.
     * @param bend the amount of pitch change, as a nonnegative 14-bit value (8192 = no bend)
     * @see #getPitchBend
     */
    public void setPitchBend(int bend) {
//        pitchBend = bend;
        int msb = bend >> 7;
        int lsb = bend & 0x3f;
        encode(PITCH_BEND, lsb, msb);
    }

    public void setControl(int control, int value) {
        if (control < 0) {
            setPseudoControl(control, value);
            return;
        }
        if (is7bit(control)) {
            controlChange(control, value);
        } else {
            control &= 0x1F; // force to the msb control
            int msb = value >> 7; // the high 7 bits
//            int lsb = value & 0x3F; // the low 7 bits
            // only relays change if ?sb is changed
            controlChange(control, msb); // resets low 7 bits!
            // lsb must be sent after msb to meet MIDI spec.
            //            controlChange(control+0x20, lsb); !!!
        }
    }

    /**
     * Starts the specified note sounding.  The key-down velocity usually controls the note's volume and/or brightness.  If
     * <code>velocity</code> is zero, this method instead acts like {@link #noteOff(int)}, terminating the note.
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     * @param velocity the speed with which the key was depressed
     * @see #noteOff(int, int)
     */
    public void noteOn(int noteNumber, int velocity) {
        encode(NOTE_ON, noteNumber, velocity);
    }

    /**
     * Turns the specified note off.  The key-up velocity, if not ignored, can be used to affect how quickly the note decays.
     * In any case, the note might not die away instantaneously; its decay rate is determined by the internals of the
     * <code>Instrument</code>. If the Hold Pedal (a controller; see {@link #controlChange(int, int) controlChange}) is down,
     * the effect of this method is deferred until the pedal is released.
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     * @param velocity the speed with which the key was released
     * @see #noteOff(int)
     * @see #noteOn
     * @see #allNotesOff
     * @see #allSoundOff
     */
    public void noteOff(int noteNumber, int velocity) {
        encode(NOTE_OFF, noteNumber, velocity);
    }

    /**
     * Turns the specified note off.
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     * @see #noteOff(int, int)
     */
    public void noteOff(int noteNumber) {
        encode(NOTE_OFF, noteNumber, 0);
    }

    /**
     * Reacts to a change in the specified note's key pressure.  Polyphonic key pressure allows a keyboard player to press
     * multiple keys simultaneously, each with a different amount of pressure.  The pressure, if not ignored, is typically
     * used to vary such features as the volume, brightness, or vibrato of the note.
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     * @param pressure value for the specified key, from 0 to 127 (127 = maximum pressure)
     * @see #getPolyPressure(int)
     */
    public void setPolyPressure(int noteNumber, int pressure) {
//        polyPressure[noteNumber] = pressure;
        encode(POLY_PRESSURE, noteNumber, pressure);
    }

    /**
     * Reacts to a change in the keyboard pressure.  Channel pressure indicates how hard the keyboard player is depressing the
     * entire keyboard.  This can be the maximum or average of the per-key pressure-sensor values, as set by
     * <code>setPolyPressure</code>.  More commonly, it is a measurement of
     * a single sensor on a device that doesn't implement polyphonic key pressure.  Pressure can be used to control various
     * aspects of the sound, as described under {@link #setPolyPressure(int, int) setPolyPressure}.
     * @param pressure the pressure with which the keyboard is being depressed, from 0 to 127 (127 = maximum pressure)
     * @see #setPolyPressure(int, int)
     * @see #getChannelPressure
     */
    public void setChannelPressure(int pressure) {
//        channelPressure = pressure;
        //        System.out.println("Relaying Channel Pressure "+pressure);
        encode(CHANNEL_PRESSURE, pressure);
    }

    /**
     * Reacts to a change in the specified controller's value.  A controller
     * is some control other than a keyboard key, such as a switch, slider, pedal, wheel, or breath-pressure sensor.  The MIDI
     * 1.0 Specification provides standard numbers for typical controllers on MIDI devices, and describes the intended effect
     * for some of the controllers.  The way in which an <code>Instrument</code> reacts to a controller change may be
     * specific to the <code>Instrument</code>. <p> The MIDI 1.0 Specification defines both 7-bit controllers
     * and 14-bit controllers.  Continuous controllers, such as wheels and sliders, typically have 14 bits (two MIDI bytes),
     * while discrete controllers, such as switches, typically have 7 bits
     * (one MIDI byte).  Refer to the specification to see the expected resolution for each type of control. <p>
     * Controllers 64 through 95 (0x40 - 0x5F) allow 7-bit precision.
     * The value of a 7-bit controller is set completely by the <code>value</code> argument.  An additional set of controllers
     * provide 14-bit precision by using two controller numbers, one for the most significant 7 bits and another for the least
     * significant 7 bits.  Controller numbers 0 through 31 (0x00 - 0x1F) control the
     * most significant 7 bits of 14-bit controllers; controller numbers
     * 32 through 63 (0x20 - 0x3F) control the least significant 7 bits of
     * these controllers.  For example, controller number 7 (0x07) controls
     * the upper 7 bits of the channel volume controller, and controller number 39 (0x27) controls the lower 7 bits.
     * The value of a 14-bit controller is determined by the interaction of the two halves.  When the most significant 7 bits
     * of a controller are set (using controller numbers 0 through 31), the
     * lower 7 bits are automatically set to 0.  The corresponding controller
     * number for the lower 7 bits may then be used to further modulate the controller value.
     * @param controller the controller number (0 to 127; see the MIDI 1.0 Specification for the interpretation)
     * @param value the value to which the specified controller is changed (0 to 127)
     * @see #getController(int)
     */
    public void controlChange(int controller, int value) {
        encode(CONTROL_CHANGE, controller, value);
    }

    /**
     * Changes a program (patch).  This selects a specific instrument from the currently selected bank of instruments. <p>
     * The MIDI specification does not dictate whether notes that are already sounding should switch
     * to the new instrument (timbre) or continue with their original timbre until terminated by a note-off. <p>
     * The program number is zero-based (expressed from 0 to 127).
     * Note that MIDI hardware displays and literature about MIDI typically use the range 1 to 128 instead.
     * @param program the program number to switch to (0 to 127)
     * @see #programChange(int, int)
     * @see #getProgram()
     */
    public void programChange(int program) {
//        if (program == this.program) return; // no change
        //        Log.debug("Relaying Program Change "+(1+program));
        encode(PROGRAM_CHANGE, program);
    }

    /**
     * Changes the program using bank and program (patch) numbers.
     * @param bank the bank number to switch to (0 to 16383)
     * @param program the program (patch) to use in the specified bank (0 to 127)
     * @see #programChange(int)
     * @see #getProgram()
     */
    public void programChange(int bank, int program) {
        // !! set the bank here;
//        this.bank = bank;
//        programChange(program);
    }

    /**
     * Resets all the implemented controllers to their default values.
     * @see #controlChange(int, int)
     */
    public void resetAllControllers() {
        encode(CONTROL_CHANGE, ALL_CONTROLLERS_OFF, 0);
    }

    /**
     * Turns off all notes that are currently sounding on this channel.
     * The notes might not die away instantaneously; their decay rate is determined by the internals of the <code>Instrument</code>.
     * If the Hold Pedal controller (see {@link #controlChange(int, int) controlChange}) is down, the effect of this method is
     * deferred until the pedal is released.
     * @see #allSoundOff
     * @see #noteOff(int)
     */
    public void allNotesOff() {
        encode(CONTROL_CHANGE, ALL_NOTES_OFF, 0);
    }

    /**
     * Immediately turns off all sounding notes on this channel, ignoring the state of the Hold Pedal and the internal decay
     * rate of the current <code>Instrument</code>.
     * @see #allNotesOff
     */
    public void allSoundOff() {
        encode(CONTROL_CHANGE, ALL_SOUND_OFF, 0);
    }

    /**
     * Turns local control on or off.  The default is for local control to be on.  The "on" setting means that if a device is
     * capable of both synthesizing sound and transmitting MIDI messages,
     * it will synthesize sound in response to the note-on and note-off messages that it itself transmits.  It will also respond
     * to messages received from other transmitting devices. The "off" setting means that the synthesizer will ignore its
     * own transmitted MIDI messages, but not those received from other devices.
     * @param local <code>true</code> to turn local control on, <code>false</code> to turn local control off
     * @return the new local-control value
     */
    public boolean localControl(boolean local) {
//        localControl = local;
        encode(CONTROL_CHANGE, LOCAL_CONTROL, local ? 127 : 0);
        return local;
    }

    /*
     * Implement higher level support for Controllers ---------------------
     */

    public void setVolume(int volume) {
        setControl(VOLUME, volume);
    }

    public void setPan(int pan) {
        setControl(PAN, pan);
    }

    /**
     * Turns mono mode on or off.  In mono mode, the channel synthesizes
     * only one note at a time.  In poly mode (identical to mono mode off),
     * the channel can synthesize multiple notes simultaneously. The default is mono off (poly mode on). <p>
     * "Mono" is short for the word "monophonic," which in this context
     * is opposed to the word "polyphonic" and refers to a single synthesizer voice per MIDI channel.  It has nothing to do
     * with how many audio channels there might be (as in "monophonic" versus "stereophonic" recordings).
     * @param on <code>true</code> to turn mono mode on, <code>false</code> to turn it off (which means turning poly mode on).
     * @see #getMono
     */
    public void setMono(boolean on) {
        encode(CONTROL_CHANGE, MONO_MODE, on ? 127 : 0);
    }

    /**
     * Turns omni mode on or off.  In omni mode, the channel responds
     * to messages sent on all channels.  When omni is off, the channel responds only to messages sent on its channel number.
     * The default is omni off.
     * @param on <code>true</code> to turn omni mode on, <code>false</code> to turn it off.
     * @see #getOmni
     */
    public void setOmni(boolean on) {
        encode(CONTROL_CHANGE, OMNI_MODE_ON, on ? 127 : 0);
    }

    /**
     * Sets the mute state for this channel. A value of <code>true</code> means the channel is to be muted, <code>false</code>
     * means the channel can sound (if other channels are not soloed). <p>  Unlike {@link #allSoundOff()}, this method
     * applies to only a specific channel, not to all channels.  Further, it
     * silences not only currently sounding notes, but also subsequently received notes.
     * @param mute the new mute state
     * @see #getMute
     * @see #setSolo(boolean)
     */
    public void setMute(boolean mute) {
		// !!! !!! !!!
    }

    /**
     * Sets the solo state for this channel. If <code>solo</code> is <code>true</code> only this channel
     * and other soloed channels will sound. If <code>solo</code> is <code>false</code> then only other soloed channels will
     * sound, unless no channels are soloed, in which case all unmuted channels will sound.
     * @param soloState new solo state for the channel
     * @see #getSolo
     * @see #setMute(boolean)
     */
    public void setSolo(boolean soloState) {
		// !!! !!! !!!
    }
}
