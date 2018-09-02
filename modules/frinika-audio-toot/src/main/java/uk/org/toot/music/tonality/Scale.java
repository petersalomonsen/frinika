// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.tonality;

/**
 * A Scale is a list of intervals.
 * Melodies are derived from a scale.
 * Chords are derived from the modes of a scale, aka chord modes.
 * @author st
 *
 */
public class Scale 
{
    private String name ;
    private int[] intervals ;
    private int intIntervals;

    /**
     * Constructor
     */
    public Scale(String name, int[] intervals) {
        this.name = name ;
        this.intervals = intervals ;
        // encode intervals as int bitmask, UNISON = 0x01 etc.
        for ( int i = 0; i < intervals.length; i++) {
        	intIntervals |= 1 << intervals[i];
        }
    }

    /**
     * @return the length of this Scale.
     */
    public int length() { return intervals.length ; }

    /**
     * @return the name of this Scale.
     */
    public String getName() { return name; }

    /**
     * @return the intervals of this Scale.
     */
    public int[] getIntervals() { return intervals; }
    
    /**
     * @return the intervals of this Scale as a bitmask.
     */
    public int getIntervalsAsInt() { return intIntervals; }
    
    /**
     * Return the interval of the specified index into this Scale.
     * @param index the index into this Scale.
     * @return the indexed interval
     */
    public int interval(int index) {
    	// the Scale intervals are effectively the chord mode of the first
    	// degree of the Scale so we use the general ChordMode logic.
    	return ChordMode.interval(intervals, index) ; 
    }

    /**
     * Return the interval between the specified indices into this Scale.
     * If index2 is less than index1, the interval for index2 is raised by an octave
     * so that modes of the scale (rotations) can be derived easily by getChordMode().
     * @param index1 the index of the lower interval
     * @param index2 the index of the higher interval
     * @return the interval between the specified indices
     */
    public int interval(int index1, int index2) {
    	// the Scale intervals are effectively the chord mode of the first
    	// degree of the Scale so we use the general ChordMode logic.
    	return ChordMode.interval(intervals, index1, index2);
    }
    
    /**
     * Derive the Chord Mode for the specified index into this Scale.
     * @param index the index into this Scale
     * @return the array of intervals representing the chord mode
     */
    public int[] getChordMode(int index) {
    	int[] modeIntervals = new int[length()];
    	for ( int i = 1; i < length(); i++ ) {
    		modeIntervals[i] = modeIntervals[i-1] + interval(index+i-1, index+i);
    	}
    	return modeIntervals;
    }
    
    /**
     * @return true if interval is diatonic from this index, false otherwise
     */
    public boolean hasInterval(int index, int val) {
        int i2 = (interval(index)+val)%12;
        for ( int i = 0; i < length(); i++ ) {
			if ( i2 == interval(i) ) return true;
        }
        return false;
    }
}
