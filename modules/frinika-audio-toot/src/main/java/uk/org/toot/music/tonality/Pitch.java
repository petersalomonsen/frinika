// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.tonality;

/**
 * This class provides static methods to support pitches and pitch classes, which
 * are just ints.
 * 
 * A pitch is a note in a particular register, where note is used as shorthand
 * for pitch class. i.e. C is a note (and pitch class), C4 is a pitch.
 * 
 * Pitch classes are represented as the lowest octave of pitches.
 * Hence pitch classes may usually be used in place of pitches.
 * 
 * C-1 = 0
 * C0 = 12
 * C1 = 24
 * C2 = 36
 * C4 (middle-C) = 60 (arbitrary MMA definition)
 */
public class Pitch
{
    private static String[] flatNames = {
        "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" } ;

    private static String[] sharpNames = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" } ;

    private Pitch() {
        // to prevent instantiation
    }

    /**
     * Return the name of the specified pitch
     * @param pitch the int value of the pitch
     * @return the name of the pitch
     */
    static public String name(int pitch) {
        return className(pitch)+((int)(pitch/12)-1);
    }

    public static int value(String name, int register) {
        return value(classValue(name), register);
    }

    public static int value(int pitchClass, int register) {
        return pitchClass+(12*(register+1));
    }
    
    /**
     * Return whether a keyboard key is white for the specified pitch.
     * @param pitch the int value of a pitch
     * @return true if the pitch represents a white key, false otherwise
     */
    public static boolean isWhite(int pitch) {
        return !isBlack(pitch) ;
    }

    /**
     * Return whether a keyboard key is black for the specified pitch.
     * @param pitch the int value of a pitch
     * @return true if the pitch represents a black key, false otherwise
     */
    public static boolean isBlack(int pitch) {
        int pc = classValue(pitch);
        return ( pc == 1 || pc == 3 || pc == 6 || pc == 8 || pc == 10 ) ;
    }
    
    /**
     * Return the name of the pitch class of the specified pitch.
     * @param pitch the int value of a pitch
     * @return the name of the pitch class for the pitch
     */
    public static String className(int pitch) {
        return flatNames[classValue(pitch)] ;
    }

    public static String classFlatName(int pitch) {
        return flatNames[classValue(pitch)] ;
    }

    public static String classSharpName(int pitch) {
        return sharpNames[classValue(pitch)] ;
    }

    /**
     * Return the names of the pitch classes of the specified pitches,
     * seperated by spaces. May result in duplicate pitch class names.
     * @param pitches the array of int values of pitches
     * @return the names of the pitch classes for the pitches
     */
    public static String classNames(int[] pitches) {
    	StringBuilder sb = new StringBuilder();
    	for ( int i = 0; i < pitches.length; i++ ) {
    		sb.append(className(pitches[i]));
    		if ( i < pitches.length-1 ) {
    			sb.append(' ');
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * Return the int value of the pitch class of the specified pitch.
     * @param pitch the int value of the pitch
     * @return the int value of the pitch class for the pitch
     */
    public static int classValue(int pitch) {
    	if ( pitch < 0 ) throw new IllegalArgumentException("pitch < 0");
    	return pitch % 12;
    }
    
    /**
     * Returns the int value of the pitch class of the specified pitch.
     * @param pitch the string representation of a pitch
     * @return the int value of the pitch class for the pitch
     */
    public static int classValue(String pitch) {
        char qual = ' ', letter = Character.toUpperCase(pitch.charAt(0)) ;
        int base = 0, offset = 0 ;

        switch ( letter ) {
        case 'C': base = 0 ;  break ;
        case 'D': base = 2 ;  break ;
        case 'E': base = 4 ;  break ;
        case 'F': base = 5 ;  break ;
        case 'G': base = 7 ;  break ;
        case 'A': base = 9 ;  break ;
        case 'B': base = 11 ; break ;
        default:
        	throw new IllegalArgumentException(pitch+" is not a valid pitch");
        }

        // should cope with double sharp/flat !!!
        if ( pitch.length() > 1 )
        	qual = pitch.charAt(1) ;  // specified quality if available

        if ( qual == '#' ) offset++ ;
        else if ( qual == 'b' ) offset-- ;

        return classValue(base+offset);
    }

    /**
     * Returns the int values of the pitch classes of the specified pitches.
     * May result in duplicate pitch classes.
     * @param pitches the string representation of the pitches
     * @return the aarray of int values of the pitch classes for the pitches
     */
    public static int[] classValues(String pitches) {
    	String[] notes = pitches.split("\\s+");
    	int[] values = new int[notes.length];
    	int j = 0;
    	for ( int i = 0; i < notes.length; i++) {
    		try {
    			values[j] = classValue(notes[i]);
    			j += 1;
    		} catch ( Exception e) {
    			// quietly miss out invalid notes
    		}
    	}
		// but have to fill values array to avoid illegal values
    	for ( ; j < values.length; j++ ) {
    		values[j] = values[j-1];
    	}
    	return distinctClasses(values);
    }
    
    /**
     * Count the number of distinct pitch classes in the specified pitches.
     * i.e. C3, C4 etc. are counted as the single pitch class C
     * @param pitches the array of int values of the pitches
     * @return the count of distinct pitch classes for the pitches
     */
    public static int classCount(int[] pitches) {
    	int count = 0;
    	int mask = 0;
    	for ( int i = 0; i < pitches.length; i++) {
    		int pc = classValue(pitches[i]);
    		int bit = 1 << pc;
    		if ( (mask & bit) != 0 ) continue;
    		mask |= bit;
    		count += 1;
    	}
    	return count;
    }
    
    /**
     * Return the distinct pitch classes of the specified pitches.
     * i.e. remove duplicate pitch classes
     * @param pitches the array of int values of the pitches
     * @return the array of int values of the distinct pitch classes of the pitches
     */
    public static int[] distinctClasses(int[] pitches) {
    	int count = classCount(pitches);
    	if ( count == pitches.length ) return pitches; // all distinct
    	int[] distinct = new int[count];
    	count = 0;
    	int mask = 0;
    	for ( int i = 0; i < pitches.length; i++) {
    		int pc = classValue(pitches[i]);
    		int bit = 1 << pc;
    		if ( (mask & bit) != 0 ) continue;
    		mask |= bit;
    		distinct[count++] = pitches[i];
    	}
    	return distinct;
    }

}
