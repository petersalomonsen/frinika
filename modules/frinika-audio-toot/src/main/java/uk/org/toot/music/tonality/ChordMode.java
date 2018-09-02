// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.tonality;

/**
 * This class provides static methods to operate on int arrays rerpesenting
 * chord modes.
 * A chord mode is the mode derived from a particular index into a Scale.
 * So called because it 'contains' every diatonic chord from
 * that index of a Scale.
 * 
 * Constants SECUNDAL, TERTIAN and QUARTAL are provided for typical
 * chord construction.
 * @author st
 *
 */
public class ChordMode 
{
	/**
	 * Minor or major seconds.
	 */
	public static final int SECUNDAL = 1;
	
	/**
	 * Minor or major thirds (conventional chord construction).
	 */
	public static final int TERTIAN = 3;
	
	/**
	 * Diminished or perfect fourths.
	 */
	public static final int QUARTAL = 5;

	private ChordMode() {
		// prevent instantation
	}
	
	/**
	 * Return the interval of the index of the chord mode
	 * @param chordMode the intevals of the chord mode
	 * @param index
	 * @return the interval
	 */
    public static int interval(int[] chordMode, int index) { 
    	return chordMode[index % chordMode.length] ; 
    }

    /**
     * Return the interval from index1 to index2 of the chord mode
     * If index2 is less than index1, the interval for index2 is raised by an octave
     * @param chordMode the intevals of the chord mode
     * @param index1
     * @param index2
     * @return the interval
     */
    public static int interval(int[] chordMode, int index1, int index2) {
    	return interval(interval(chordMode, index1), interval(chordMode, index2));
    }


    protected static int interval(int i1, int i2) {
        if ( i2 < i1 ) i2 += Interval.OCTAVE;
        return i2 - i1;    
    }

    /**
     * 
     * @param chordMode the intervals of the chord mode
     * @param poly the requested polyphony 1..7 but less intervals may be returned
     * @param lowInterval SECUNDAL, TERTIAN or QUARTAL
     * the lower of the two allowed intervals, add 1 for higher allowed interval
     *  tertian may be two octaves of intervals
     *  so secundal will be less than 2 octaves of intervals?
     *  and quartal may be more than 2 octaves of intervals?
     * @return the intervals of the chord with the specified polyphony
     */
    public static int[] getIntervals(int[] chordMode, int poly, int lowInterval) {
    	if ( poly < 1 || poly > 7 ) {
    		throw new IllegalArgumentException("invalid polyphony = "+poly);
    	}
        int[] intervals = new int[poly];
        int interval;
        int i = 0;
        int j = 0;
        intervals[j] = 0; // explicit
        j += 1;
        while ( j < poly ) {
        	interval = interval(intervals[j-1], chordMode[i]);
        	if ( interval == lowInterval || interval == lowInterval + 1 ) {
        		intervals[j] = intervals[j-1] + interval;
        		j += 1;
        	}
        	i += 1;
        	i %= chordMode.length;
        }
        return intervals;
    }
   
    /**
     * Return whether the chord mode contains the specified interval.
     * @param chordMode the intervals of the chord mode
     * @param interval
     * @return true if chordMode contains interval, false otherwise.
     */
    public static boolean hasInterval(int[] chordMode, int interval) {
    	interval %= 12;
    	for ( int i = 0; i < chordMode.length; i++) {
    		if ( chordMode[i] == interval ) {
    			return true; // fast match
    		}
    	}
    	return false;
    }
}
