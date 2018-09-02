// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.tonality;

/**
 * This class provides static methods to simplify the handling of intervals
 * (which are just ints). An interval is the number of semitones between
 * two pitches and is fundamental to the definition of Scales and Chords.
 * Symbolic constants are provided for intervals of up to one octave.
 * @author st
 *
 */
public class Interval 
{
    public final static int UNISON = 0 ;
    public final static int MINOR_SECOND = 1 ;
    public final static int MAJOR_SECOND = 2 ;
    public final static int MINOR_THIRD = 3 ;
    public final static int MAJOR_THIRD = 4 ;
    public final static int PERFECT_FOURTH = 5 ;
    public final static int AUGMENTED_FOURTH = 6 ;
    public final static int DIMINISHED_FIFTH = AUGMENTED_FOURTH ;
    public final static int PERFECT_FIFTH = 7 ;
    public final static int AUGMENTED_FIFTH = 8 ;
    public final static int MINOR_SIXTH = AUGMENTED_FIFTH ;
    public final static int MAJOR_SIXTH = 9 ;
    public final static int DIMINISHED_SEVENTH = MAJOR_SIXTH ;
    public final static int MINOR_SEVENTH = 10 ;
    public final static int MAJOR_SEVENTH = 11 ;
    public final static int OCTAVE = 12 ;

    private Interval() {
        // prevent instantiation
    }

    /**
     * Return the interval class for an interval.
     * @param interval the interval
     * @return the interval class
     */
    public static int classValue(int interval) {
    	return interval % OCTAVE;
    }
    
    /**
     * Return the traditional name for an interval.
     * @param interval
     * @return the traditional name from Unison to Octave.
     */
    public static String name(int interval) {
    	if ( interval == OCTAVE ) return "Octave";
        switch ( classValue(interval) ) {
        case UNISON: return "Unison";
        case MINOR_SECOND: return "Minor second";
        case MAJOR_SECOND: return "Major second";
        case MINOR_THIRD: return "Minor third";
        case MAJOR_THIRD: return "Major third";
        case PERFECT_FOURTH: return "Perfect fourth";
        case DIMINISHED_FIFTH: return "Diminished fifth";
        case PERFECT_FIFTH: return "Perfect fifth";
        case AUGMENTED_FIFTH: return "Augmented fifth";
//        case MINOR_SIXTH: return "Minor sixth";
        case MAJOR_SIXTH: return "Major sixth";
        case MINOR_SEVENTH: return "Minor seventh";
        case MAJOR_SEVENTH: return "Major seventh";
        }
        return "{"+String.valueOf(interval)+"}";
    }

    /**
     * Return a jazz type representation of an interval of up to two octaves.
     * This is typically used to spell chord intervals which are up to two
     * octaves using tertiary construction.
     * @param interval
     * @return the 'jazz' spelling of the interval
     */
    public static String spell(int interval) {
        switch ( interval ) {
        case UNISON: return "1";
        case MINOR_SECOND: return "b2";
        case MAJOR_SECOND: return "2";
        case MINOR_THIRD: return "b3";
        case MAJOR_THIRD: return "3";
        case PERFECT_FOURTH: return "4";
        case DIMINISHED_FIFTH: return "b5";
        case PERFECT_FIFTH: return "5";
        case AUGMENTED_FIFTH: return "#5";
        case MAJOR_SIXTH: return "6";
        case MINOR_SEVENTH: return "b7";
        case MAJOR_SEVENTH: return "7";
        case OCTAVE: return "8"; // !!! ??
        case OCTAVE+MINOR_SECOND: return "b9";
        case OCTAVE+MAJOR_SECOND: return "9";
        case OCTAVE+MINOR_THIRD: return "#9";
        case OCTAVE+MAJOR_THIRD: return "b11";
        case OCTAVE+PERFECT_FOURTH: return "11";
        case OCTAVE+DIMINISHED_FIFTH: return "#11";
        case OCTAVE+PERFECT_FIFTH: return "12";
        case OCTAVE+AUGMENTED_FIFTH: return "b13";
        case OCTAVE+MAJOR_SIXTH: return "13";
        case OCTAVE+MINOR_SEVENTH: return "#13";
        case OCTAVE+MAJOR_SEVENTH: return "14";
        }
        return "?"+String.valueOf(interval);
    }
    
    /**
     * Return a jazz type representation of an array of intervals of up to two octaves.
     * @param intervals the array of intervals
     * @return the jazz spelling of the the intervals
     */
    public static String spell(int[] intervals) {
        StringBuffer spelling = new StringBuffer();
        for ( int i = 0; i < intervals.length; i++ ) {
            spelling.append(spell(intervals[i]));
            spelling.append(" ");
        }
        return spelling.toString();
    }

    /**
     * Return an interval of up to two octaves corresponding to the jazz type spelling.
     * @param s the spelling
     * @return the interval
     */
    public static int spelt(String s) {
		if ( s.equals("1") ) return UNISON;
        else if ( s.equals("b2") ) return MINOR_SECOND;
        else if ( s.equals("2") ) return MAJOR_SECOND;
        else if ( s.equals("b3") ) return MINOR_THIRD;
        else if ( s.equals("3") ) return MAJOR_THIRD;
        else if ( s.equals("4") ) return PERFECT_FOURTH;
        else if ( s.equals("#4") ) return AUGMENTED_FOURTH;
        else if ( s.equals("b5") ) return DIMINISHED_FIFTH;
        else if ( s.equals("5") ) return PERFECT_FIFTH;
        else if ( s.equals("#5") ) return AUGMENTED_FIFTH;
        else if ( s.equals("b6") ) return MINOR_SIXTH;
        else if ( s.equals("6") ) return MAJOR_SIXTH;
        else if ( s.equals("bb7") ) return DIMINISHED_SEVENTH;
        else if ( s.equals("b7") ) return MINOR_SEVENTH;
        else if ( s.equals("7") ) return MAJOR_SEVENTH;
        else if ( s.equals("b9") ) return OCTAVE+MINOR_SECOND;
        else if ( s.equals("9") ) return OCTAVE+MAJOR_SECOND;
        else if ( s.equals("#9") ) return OCTAVE+MINOR_THIRD;
        else if ( s.equals("b11") ) return OCTAVE+MAJOR_THIRD;
        else if ( s.equals("11") ) return OCTAVE+PERFECT_FOURTH;
        else if ( s.equals("#11") ) return OCTAVE+DIMINISHED_FIFTH;
        else if ( s.equals("b13") ) return OCTAVE+MINOR_SIXTH; // !!!
        else if ( s.equals("13") ) return OCTAVE+MAJOR_SIXTH;
        else return -1; // !!!
    }
}
