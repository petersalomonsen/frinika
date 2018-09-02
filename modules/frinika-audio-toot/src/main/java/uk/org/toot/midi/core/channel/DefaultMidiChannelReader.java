/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.core.channel;

import static uk.org.toot.midi.misc.Controller.*;
import static uk.org.toot.midi.message.ChannelMsg.*;

/**
 * This class decodes state and provides a channel=based read API.
 * It has state.
 */
public class DefaultMidiChannelReader implements MidiChannelReader
{
    private int index;

    public DefaultMidiChannelReader(int index) {
        this.index = index;
//        this.port = port;
    }

    public void decode(int command, int data1, int data2) {
        switch (command) {
            case CONTROL_CHANGE:
                decodeControlChange(data1, data2);
                break;
            case PROGRAM_CHANGE:
                decodeProgramChange(data1);
                break;
            case CHANNEL_PRESSURE:
                decodeChannelPressure(data1);
                break;
            case POLY_PRESSURE:
                decodePolyPressure(data1, data2);
                break;
            case PITCH_BEND:
                decodePitchBend(data1, data2);
                break;
        }
    }

    private int getPseudoControl(int control) {
        switch (control) {
            case PITCH_BEND_PSEUDO:
                return getPitchBend();
            case POLY_PRESSURE_PSEUDO:
                return 0; // !!! !!!
            case CHANNEL_PRESSURE_PSEUDO:
                return getChannelPressure();
            case PROGRAM_PSEUDO:
                return getProgram();
        }
        return 0;
    }

    public int getControl(int control) {
        if (control < 0) {
            return getPseudoControl(control);
        }
        if (is7bit(control)) {
            return getController(control);
        } else {
            control &= 0x1F; // force to the msb control
            int msb = getController(control);
            int lsb = 0; //getController(control+0x20); !!!
            return 128 * msb + lsb;
        }
    }

    /**
     * Obtains the pressure with which the specified key is being depressed.
     * @param noteNumber the MIDI note number, from 0 to 127 (60 = Middle C)
     * @return the amount of pressure for that note, from 0 to 127 (127 = maximum pressure)
     * @see #setPolyPressure(int, int)
     */
    public int getPolyPressure(int noteNumber) {
        return polyPressure[noteNumber];
    }

    protected void decodePolyPressure(int note, int pressure) {
//        int oldPressure = polyPressure[note];
        polyPressure[note] = pressure;
        //		port.firePropertyChange("Poly Pressure", getIndex(), oldPressure, polyPressure[note]);
    }

    protected void decodeChannelPressure(int pressure) {
//        int oldPressure = channelPressure;
        channelPressure = pressure;
        //        System.out.println("Firing Channel Pressure "+pressure);
//        port.firePropertyChange("Channel Pressure", getIndex(), oldPressure, channelPressure);
    }

    /**
     * Obtains the channel's keyboard pressure.
     * @return the pressure with which the keyboard is being depressed, from 0 to 127 (127 = maximum pressure)
     * @see #setChannelPressure(int)
     */
    public int getChannelPressure() {
        return channelPressure;
    }

    protected void decodeControlChange(int controller, int value) {
        if (controller < 0 || controller > 127) return; // !!!
        // we never have to decode what we encode (when timestamp == NO_INTERCEPT)
        // 14 bit: msb [lsb] i.e. lsb may never arrive so event firing is tricky
        // we could always fire but that would create a glitch when lsb is used
        // but that's OK because that's the spec's fault
        // we could just fire on lsb but we would miss 14 bits used as 7 bits, i.e. msb only
        // so fire on msb! nope, 7bits gets lost, so always fire
//        int mscontroller = controller < 0x40 ? controller & 0x1f : controller;
//        int oldval = getControl(mscontroller);
        control[controller] = value;
        if (controller < 0x20) { // if msb
            control[controller + 0x20] = 0; // reset lsb to meet MIDI spec.
        }
//        int newval = getControl(mscontroller);
        //        Log.debug("decodeCC "+Controller.propertyName(controller)+", old "+oldval+", now "+newval);
//        port.firePropertyChange(propertyName(mscontroller), getIndex(), oldval, newval);
    }

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
    public int getController(int controller) {
        return control[controller];
    }

    protected void decodeProgramChange(int program) {
//        int oldProgram = this.program;
        this.program = program;
//        port.firePropertyChange("Program", getIndex(), oldProgram, program);
        //        String name = GM.melodicName(program);
        //        Log.debug("Program Change "+(1+program)+" on Channel "+(1+getIndex()));
    }

    /**
     * Obtains the current program number for this channel.
     * @return the program number of the currently selected patch
     * @see javax.sound.midi.Patch#getProgram
     * @see javax.sound.midi.Synthesizer#loadInstrument
     * @see #programChange(int)
     */
    public int getProgram() {
        return program;
    }

    protected void decodePitchBend(int data1, int data2) {
        int bend = (data2 << 7) | (data1 & 0x3f);
//        int oldBend = pitchBend;
        pitchBend = bend;
//        port.firePropertyChange("Pitch Bend", getIndex(), oldBend, pitchBend);
    }

    /**
     * Obtains the upward or downward pitch offset for this channel.
     * @return bend amount, as a nonnegative 14-bit value (8192 = no bend)
     * @see #setPitchBend(int)
     */
    public int getPitchBend() {
        return pitchBend;
    }

    /*
     * Implement higher level support for Controllers ---------------------
     */

    public int getVolume() {
        return getControl(VOLUME);
    }

    public int getPan() {
        return getControl(PAN);
    }

    /**
     * Obtains the current mono/poly mode.
     * @return <code>true</code> if mono mode is on, otherwise <code>false</code> (meaning poly mode is on).
     * @see #setMono(boolean)
     */
    public boolean getMono() {
        return mono;
    }

    /**
     * Obtains the current omni mode status.
     * @return <code>true</code> if omni mode is on, otherwise <code>false</code>.
     * @see #setOmni(boolean)
     */
    public boolean getOmni() {
        return omni;
    }

    /**
     * Obtains the current mute state for this channel.
     * @return <code>true</code> the channel is muted, <code>false</code> if not
     * @see #setMute(boolean)
     */
    public boolean getMute() {
        return mute;
    }

    /**
     * Obtains the current solo state for this channel.
     * @return <code>true</code> if soloed, <code>false</code> if not
     * @see #setSolo(boolean)
     */
    public boolean getSolo() {
        return solo;
    }

    public int getIndex() { return index; }

    public void setIndex(int index) {
        this.index = index;
    }

    private boolean solo = false;
    private boolean mute = false;
    private boolean omni = false;
    private boolean mono = false;
//    private boolean localControl = false;
    private int pitchBend = 0;
    private int program = 0;
//    private int bank = 0;
    private int channelPressure = 0;
    private int[] polyPressure = new int[128]; // one per midi note
    private int[] control = new int[128]; // as per MIDI spec
}
