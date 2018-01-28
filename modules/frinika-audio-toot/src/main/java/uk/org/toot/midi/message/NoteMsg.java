/* Copyright (C) 2005 Steve Taylor (toot.org.uk) */

package uk.org.toot.midi.message;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.InvalidMidiDataException;

/**
 * This class provides method to simplify client handling of NOTE_ON and
 * NOTE_OFF messages.
 * 
 * The methods isOn() and isOff() encapsulate the knowledge of when a NOTE_ON
 * is really a NOTE_OFF (when velocity equals 0) so that client code need not
 * be concerned about this matter.
 * 
 * Accessors and mutators are provided for the Velocity properties.
 */
public class NoteMsg extends PitchMsg
{
    private static int noteOff = NOTE_ON;

    /**
     * Determine whether the specified MidiMessage can be handled by this class. 
     */
    static public boolean isNote(MidiMessage msg) {
        return isNote(getStatus(msg));
    }

    static public boolean isNote(int status) {
        int cmd = getCommand(status);
        return (cmd == NOTE_ON) || (cmd == NOTE_OFF);
    }

    /**
     * Create a MidiMessage representing a Note On message. 
     */
    static public MidiMessage on(int chan, int note, int vel)
    	throws InvalidMidiDataException {
        return createChannel(NOTE_ON, chan, note, vel);
    }

    /**
     * Create a MidiMessage representing a Note Off message. 
     */
    static public MidiMessage off(int chan, int note, int vel)
    	throws InvalidMidiDataException {
        return createChannel(NOTE_OFF, chan, note, vel);
    }

    /**
     * Create a MidiMessage representing a Note Off message.
     * Which may be a zero-velocty Note On.
     */
    static public MidiMessage off(int chan, int note)
    	throws InvalidMidiDataException {
        return createChannel(noteOff, chan, note, 0);
    }

    /**
     * Determine whether this MidiMessage is effectively a Note On.
     * This definition excludes a Note On with a velocity of zero which
     * is considered to effectively be a Note Off. 
     */
    static public boolean isOn(MidiMessage msg) {
        return isOn(getStatus(msg), getVelocity(msg));
    }

    static public boolean isOn(int status, int data2) {
        return getCommand(status) == NOTE_ON && data2 != 0;
    }

    /**
     * Determine whether the specified MidiMessage is effectively a Note Off message.
     */
    static public boolean isOff(MidiMessage msg) {
        return isOff(getStatus(msg), getVelocity(msg));
    }

    static public boolean isOff(int status, int data2) {
        return !isOn(status, data2);
    }

    /**
     * Get the velocity byte for the specified MidiMessage. 
     */
    static public int getVelocity(MidiMessage msg) {
        return getData2(msg);
    }

    /**
     * Set the velocity byte for the specified MidiMessage. 
     */
    static public MidiMessage setVelocity(MidiMessage msg, int vel)
    	throws InvalidMidiDataException {
        return setData2(msg, vel);
    }

    /**
     * Louden the specified MidiMessage by adding the specified velocityDelta
     * to the velocity byte. A negatibe delta makes the message quieter.
     * Value is clamped. 
     */
    static public MidiMessage louden(MidiMessage msg, int velocityDelta)
    	throws InvalidMidiDataException {
        int velocity = getData2(msg)+velocityDelta;
        if ( velocity > 127 ) velocity = 127;
        else if ( velocity < 0 ) velocity = 0;
        return setData2(msg, velocity);
    }
}
