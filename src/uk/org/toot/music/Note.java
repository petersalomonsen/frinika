// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music;

/**
 * This class provides static methods to assist in the representation of a note
 * as an int. Such a note encodes time, pitch, level and duration.
 * The time is specified as ticks relative to the start of a bar, 0..255.
 * The duration is specified as ticks relative to the time, 
 * 0..255 such that the off time can be up to several bars later than the on time.
 * The pitch (or drum) is specified as a MIDI compatible pitch, 0..127
 * The level is specified as a MIDI-compatible velocity, 0..127.
 * The most significant bits represent time so that an array of ints
 * representing notes can be time-ordered by simple sorting.
 * Such sorting orders notes by time, then level, then duration, then pitch.
 * @author st
 *
 * Format 0ttttttt tvvvvvvv dddddddd 0ppppppp  
 * always positive
 * can be sorted by time (t)
 * uses 1/2 of positive int values, unused bit 7 is zero
 * 
 */
public class Note extends TimedCoding
{
	private final static int LEVEL_SHIFT = 16;
	private final static int LEVEL_MASK = 0x7f;		// 7 bits, 0..127
	private final static int DURATION_SHIFT = 8;
	private final static int DURATION_MASK = 0xff;	// 8 bits, 0..255
	private final static int PITCH_SHIFT = 0;
	private final static int PITCH_MASK = 0x7f;		// 7 bits, 0..127
	
	public static int createNote(int timeOn, int pitch, int level) {
		int note = create(timeOn);
		note = setPitch(note, pitch);
		note = setLevel(note, level);
		note = setDuration(note, 1); // shortest note duration, 1/64th
		return note;
	}
	
	public static int createNote(int timeOn, int pitch, int level, int duration) {
		int note = createNote(timeOn, pitch, level);
		note = setDuration(note, duration);
		return note;
	}
	
	/**
	 * Return the duration, in ticks, of the specified note.
	 * @param note the int which contains the duration of the note
	 * @return the time, in ticks, of the duration.
	 */
	public static int getDuration(int note) {
		return getParameter(note, DURATION_SHIFT, DURATION_MASK);
	}
	
	public static int setDuration(int note, int time) {
		return setParameter(note, DURATION_SHIFT, DURATION_MASK, time);
	}
	
	public static int getPitch(int note) {
		return getParameter(note, PITCH_SHIFT, PITCH_MASK);
	}
	
	public static int setPitch(int note, int pitch) {
		return setParameter(note, PITCH_SHIFT, PITCH_MASK, pitch);
	}

	public static int getLevel(int note) {
		return getParameter(note, LEVEL_SHIFT, LEVEL_MASK);
	}
	
	public static int setLevel(int note, int level) {
		return setParameter(note, LEVEL_SHIFT, LEVEL_MASK, level);
	}
}
