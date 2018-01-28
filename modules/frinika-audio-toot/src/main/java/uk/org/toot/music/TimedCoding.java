// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music;

/**
 * A TimedCoding is the abstract base class for those musical events which
 * have a time relative to a bar.
 * The time is encoded in the 8 most significant bits of a positive int
 * so time-ordering of such ints is simply achieved by sorting.
 * The time is relative to the start of a bar.
 * The resolution is 255 ticks.
 * @author st
 *
 * Format 0ttttttt txxxxxxx xxxxxxxx xxxxxxxx
 * always positive
 * can be sorted by time (t)
 * leaves lower 23 bits for subclass use
 */
public abstract class TimedCoding {

	private static final int TIME_SHIFT = 23;
	private static final int TIME_MASK = 0xff;

	protected static int create(int time) {
		return setTime(0, time);
	}
		
	protected static int getParameter(int coded, int shift, int mask) {
		return (coded >> shift) & mask;		
	}

	protected static int setParameter(int coded, int shift, int mask, int value) {
		coded &= ~(mask << shift);
		coded |= (value & mask) << shift;
		return coded;		
	}
	
	/**
	 * Return the time, in ticks, of the specified event.
	 * @param note the int which contains the time of the event
	 * @return the time, in ticks, of the event
	 */
	public static int getTime(int coded) {
		return getParameter(coded, TIME_SHIFT, TIME_MASK);
	}

	/**
	 * Encode the time into a coded event.
	 * @param coded the coded event to have the time set
	 * @param time the time to encode into the coded event
	 * @return int - the modified coded event
	 */
	public static int setTime(int coded, int time) {
		return setParameter(coded, TIME_SHIFT, TIME_MASK, time);
	}
}
