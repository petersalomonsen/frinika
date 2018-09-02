// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.tonality;

import java.util.Arrays;
import java.util.List;

import static uk.org.toot.music.tonality.Interval.*;

/**
 * This class is effectively a database of known chords with static methods
 * to retrieve particular chords.
 * A Chord can be retrieved by symbol, spelling, name and intervals.
 * Chords can also be identified from notes.
 * @author st
 *
 */
public class Chords 
{
	/**
     * @supplierCardinality 1..*
     * @label chords 
     * @directed
     */
    /*#Chord lnkChords;*/
    private static List<Chord> chords = new java.util.ArrayList<Chord>();
    
    static { addChords(); }

    private static Identifier chordIdentifier =	new DefaultIdentifier();

    private Chords() {
    	// prevent instantiation
    }
    
    /**
     * Permit an alternate Identifier to be plugged in in case DefaultIdentifier
     * is unsuitable.
     * @param identifier the Identifier to use to implement withNotes(int[] notes)
     */
    public static void setChordIdentifer(Identifier identifier) {
    	chordIdentifier = identifier;
    }
    
    /**
     * Add a Chord to the list of known chords.
     * @param aChord the Chord to add
     */
    public static void add(Chord aChord) {
        chords.add(aChord);
    }

    /**
     * Add a new Chord to the list of known chords.
     * @param aSymbol the symbol of the Chord to be added
     * @param aSpelling the spelling of the Chord to be added
     * @param aName the name of the Cohord to be added
     */
    public static void addChord(String aSymbol, String aSpelling, String aName) {
        add(new Chord(aSymbol, aSpelling, aName));
    }

    /**
     * Return the single Chord that has the specified symbol.
     * @param aSymbol the specified symbol to find
     * @return the Chord if found, null otherwise
     */
    public static Chord withSymbol(String aSymbol) {
        for ( Chord chord : chords ) {
            if ( chord.getSymbol().equals(aSymbol) ) return chord;
        }
        return null;
    }

    /**
     * Return the single Chord that has the specified spelling.
     * @param aSpelling the specified spelling to find
     * @return the Chord if found, null otherwise
     */
    public static Chord withSpelling(String aSpelling) {
        for ( Chord chord : chords ) {
            if ( chord.getSpelling().equals(aSpelling) ) return chord;
        }
        return null;
    }

    /**
     * Return the single Chord with the specified name.
     * @param aName the specified name to find
     * @return the Chord if found, null otherwise
     */
    public static Chord withName(String aName) {
        for ( Chord chord : chords ) {
            if ( chord.getName().equals(aName) ) return chord;
        }
        return null;
    }

    /**
     * Return the single Chord with the specified intervals
     * @param someIntervals
     * @return the Chord if found, null otherwise
     */
    public static Chord withIntervals(int[] someIntervals) {
        for ( Chord chord : chords ) {
            if ( chord.matchesIntervals(someIntervals) ) return chord;
        }
        return null;
    }

    /**
     * Return the List of Chord.Voicings with the specified intervals but
     * with exactly 'missing' missing intervals.
     * @param someIntervals
     * @param missing the number of intervals that must be missing
     * @return the List of Chord.Voicings
     */
    public static List<Chord.Voicing> withIntervals(int[] someIntervals, int missing) {
    	int[] missingIndices;
    	List<Chord.Voicing> voicings = new java.util.ArrayList<Chord.Voicing>();
        for ( Chord chord : chords ) {
        	missingIndices = chord.missingIntervals(someIntervals, missing); 
            if ( missingIndices != null ) {
            	voicings.add(new Chord.Voicing(chord, missingIndices));
            }
        }
        return voicings;
    }

    /**
     * Return the List of Chord.PitchedVoicings with the specified notes
     * @param notes the notes or pitches
     * @return the List of Chord.PitchedVoicings which match the notes
     */
    public static List<Chord.PitchedVoicing> withNotes(int[] notes) {
    	return chordIdentifier.withNotes(notes);
    }
    
    /**
     * Return the List of Chords which are derived from the specifed chord mode.
     * @param chordMode
     * @return the List of Chords
     */
    public static List<Chord> fromChordMode(int[] chordMode) {
    	List<Chord> chordList = new java.util.ArrayList<Chord>();
        for ( Chord chord : chords ) {
        	if ( chord.matchesChordMode(chordMode) ) {
        		chordList.add(chord);
        	}
        }	
    	return chordList;
    }
    
    /**
     * Check all known Chords can be properly identified.
     */
    public static void checkIdentifiability() {
    	List<Chord.PitchedVoicing> matches;
    	Chord.PitchedVoicing pitchedVoicing;
    	int[] notes;
    	boolean found;
        for ( Chord chord : chords ) {
        	pitchedVoicing = new Chord.PitchedVoicing(new Chord.Voicing(chord), Pitch.classValue("C"));
        	notes = pitchedVoicing.getPitches();
        	matches = withNotes(notes);
        	found = false;
        	for ( Chord.PitchedVoicing cpv : matches ) {
        		if ( cpv.getChord() == chord ) {
//        			System.out.println(chord.getSymbol()+" identified");
        			found = true;
        			break;
        		}
        	}
        	if ( !found ) {
        		System.out.println("C"+chord.getSymbol()+" not identified");
            	for ( Chord.PitchedVoicing car : matches ) {
            		System.out.println("matched "+car);
            	}
        	}
        }
    }
    
    /**
     * Add kwown Chords.
     */
    private static void addChords() {
		// Major: major third, major seventh ---------------------------------
        // three note
		addChord("maj", 		"1 3 5",			"major");
        // four note
		addChord("maj7", 		"1 3 5 7",			"major seventh");
		addChord("maj7-5",		"1 3 b5 7",			"major seventh flat 5");
		addChord("maj7+5",		"1 3 #5 7",	 		"major seventh sharp 5");
		addChord("maj7sus4",	"1 4 5 7",			"major seventh suspended fourth");
		addChord("add+11",		"1 3 5 #11",		"added augmented eleventh");
		addChord("6",			"1 3 5 6",			"six");
        // five note
		addChord("maj9", 		"1 3 5 7 9",		"major ninth");
		addChord("maj9+5", 		"1 3 #5 7 9",		"major ninth augmented fifth");
		addChord("maj9-5", 		"1 3 b5 7 9",		"major ninth diminished fifth");
		addChord("maj7+11",		"1 3 5 7 #11",		"major seventh augmented eleventh");
		addChord("6/7",			"1 3 5 6 7",		"six seven");
		addChord("6/9",			"1 3 5 6 9",		"major sixth added ninth");
		addChord("6/7sus4",		"1 4 5 6 7",		"six seven suspended fourth");
        // six note
		addChord("maj11", 		"1 3 5 7 9 11",		"major eleventh");
		addChord("maj11+5",		"1 3 #5 7 9 11",	"major eleventh augmented fifth");
		addChord("maj11-5",		"1 3 b5 7 9 11",	"major eleventh augmented fifth");
		addChord("maj9+11",		"1 3 5 7 9 #11",	"major ninth augmented eleventh");
        // seven note
		addChord("maj13", 		"1 3 5 7 9 11 13",	"major thirteenth");
		addChord("maj13+11",	"1 3 5 7 9 #11 13",	"major thirteenth augmented eleventh");
		// Diminished: minor third, diminished fifth
		addChord("dim", 		"1 b3 b5",			"diminished");
		addChord("dim7", 		"1 b3 b5 bb7",		"diminished seventh");
		// Minor/major: minor third, major seventh ---------------------------
		addChord("min/maj7",	"1 b3 5 7",			"minor/major seventh");
		addChord("min/maj9",	"1 b3 5 7 9",		"minor/major ninth");
		addChord("min/maj11",	"1 b3 5 7 9 11",	"minor/major eleventh");
		addChord("min/maj13",	"1 b3 5 7 9 11 13",	"minor/major thirteenth");
		// Minor: minor third, minor seventh ----------------------------------
        // three note
		addChord("m", 			"1 b3 5",			"minor");
        // four note
		addChord("m7", 			"1 b3 5 b7",		"minor seventh");
		addChord("m7-5", 		"1 b3 b5 b7",		"half diminished");
		addChord("m6",			"1 b3 5 6",			"minor sixth");
        // five note
		addChord("m9", 			"1 b3 5 b7 9",		"minor ninth");
		addChord("m9-5",		"1 b3 b5 b7 9",		"minor ninth diminished fifth");
		addChord("m7-9",		"1 b3 5 b7 b9",		"minor seventh flat nine");
		addChord("m7-9-5",		"1 b3 b5 b7 b9",	"minor seventh flat nine diminished fifth");
		addChord("m7/11",		"1 b3 5 b7 11",		"minor seven eleven");
		addChord("m6/7",		"1 b3 5 6 b7",		"minor six seven");
		addChord("m6/9",		"1 b3 5 6 9",		"minor six nine");
        // six note
		addChord("m11", 		"1 b3 5 b7 9 11",	"minor eleventh");
		addChord("m11-9",		"1 b3 5 b7 b9 11",	"minor eleventh flat nine");
		addChord("m11-9-5",		"1 b3 b5 b7 b9 11",	"minor eleventh flat nine diminished fifth");
		addChord("m11-5",		"1 b3 b5 b7 9 11",	"minor eleventh diminished fifth");
		addChord("m6/7/11",		"1 b3 5 6 b7 11",	"minor six seven eleven");
		addChord("m13/11",		"1 b3 5 9 11 13",	"minor thirteen eleven");
		// seven note
		addChord("m13", 		"1 b3 5 b7 9 11 13","minor thirteenth");
		addChord("m13-9",		"1 b3 5 b7 b9 11 13","minor thirteenth flat nine");
		addChord("m13-5",		"1 b3 b5 b7 9 11 13","minor thriteenth diminished fifth");
		// note 5 and b13 are 1 semitone apart, are they valid?
		addChord("m11-13", 		"1 b3 5 b7 9 11 b13",	"minor eleventh diminished thirteenth"); 
		addChord("m11-9-13", 	"1 b3 5 b7 b9 11 b13",	"minor eleventh diminished ninth diminished thirteenth");
		addChord("m11-9-5-13", 	"1 b3 b5 b7 b9 11 b13",	"minor eleventh diminished ninth diminished fifth diminshed thirteenth");
		// Augmented
		addChord("aug", 		"1 3 #5",			"augmented");
		// Dominant: major third, minor seventh -------------------------------
        // four note
		addChord("7",			"1 3 5 b7",			"seventh");
		addChord("7-5",			"1 3 b5 b7",		"seventh flat 5");
		addChord("7+5",			"1 3 #5 b7",		"seventh sharp 5");
        // five note
		addChord("9",			"1 3 5 b7 9",		"ninth");
		addChord("9-5",			"1 3 b5 b7 9",		"ninth diminished fifth");
		addChord("9+5",			"1 3 #5 b7 9",		"ninth augmented fifth");
		addChord("7-9",			"1 3 5 b7 b9",		"seventh flat 9");
		addChord("7-9-5",		"1 3 b5 b7 b9",		"seventh flat 9 dimished fifth");
		addChord("7-9+5",		"1 3 #5 b7 b9",		"seventh flat 9 augmented fifth");
		addChord("7+9",			"1 3 5 b7 #9",		"seventh augmented ninth");
		addChord("7+9-5",		"1 3 b5 b7 #9",		"seventh augmented ninth diminished fifth");
		addChord("7+9+5",		"1 3 #5 b7 #9",		"seventh augmented ninth augmented fifth");
		addChord("7/11",		"1 3 5 b7 11",		"seven eleven");
        // six note
		addChord("11",			"1 3 5 b7 9 11",	"eleventh");
        addChord("11+9",		"1 3 5 b7 #9 11",	"eleventh augmented ninth");
        addChord("11+9+5",		"1 3 #5 b7 #9 11",	"eleventh augmented ninth augmented fifth");
        addChord("11+9-5",		"1 3 b5 b7 #9 11",	"eleventh augmented ninth diminished fifth");
		addChord("11-9",		"1 3 5 b7 b9 11",	"eleventh diminished ninth");
        addChord("11-9+5",		"1 3 #5 b7 b9 11",	"eleventh diminished ninth augmented fifth");
        addChord("11-9-5",		"1 3 b5 b7 b9 11",	"eleventh diminished ninth diminished fifth");
		addChord("7+11",		"1 3 5 b7 9 #11",	"seventh augmented eleventh"); //
        addChord("7+11+9",		"1 3 5 b7 #9 #11",	"seventh augmented eleventh augmented ninth");
        addChord("7+11+9+5",	"1 3 #5 b7 #9 #11",	"seventh augmented eleventh augmented ninth augmented fifth");
        addChord("7+11-9",		"1 3 5 b7 b9 #11",	"seventh augmented eleventh diminished ninth");
        addChord("7+11-9+5",	"1 3 #5 b7 b9 #11",	"seventh augmented eleventh diminished ninth augmented fifth");
        // seven note
		addChord("13",			"1 3 5 b7 9 11 13",	"thirteenth");
		addChord("13+9",		"1 3 5 b7 #9 11 13","thirteenth augmented ninth");
		addChord("13+9+5",		"1 3 #5 b7 #9 11 13","thirteenth augmented ninth augmented fifth");
		addChord("13+9-5",		"1 3 b5 b7 #9 11 13","thirteenth augmented ninth diminished fifth");
		addChord("13-9",		"1 3 5 b7 b9 11 13","thirteenth diminished ninth");
		addChord("13-9+5",		"1 3 #5 b7 b9 11 13","thirteenth diminished ninth");
		addChord("13-9-5",		"1 3 b5 b7 b9 11 13","thirteenth diminished ninth");
		addChord("13+11",		"1 3 5 b7 9 #11 13","thirteenth augmented eleventh");
		addChord("13+11+9",		"1 3 5 b7 #9 #11 13","thirteenth augmented eleventh augmented ninth");
		addChord("13+11+9+5",	"1 3 #5 b7 #9 #11 13","thirteenth augmented eleventh augmented ninth augmented fifth");
		addChord("13+11-9",		"1 3 5 b7 b9 #11 13","thirteenth augmented eleventh diminished ninth");
		addChord("13+11-9+5",	"1 3 #5 b7 b9 #11 13","thirteenth augmented eleventh diminished ninth augmented fifth");
		addChord("13sus4",		"1 4 5 b7 9 13",	"thirteenth suspended fourth");

		// oddities :)
		addChord("sus4",		"1 4 5",			"suspended fourth");
		addChord("sus2",		"1 2 5",			"suspended second");
		addChord("add4",		"1 3 4 5",			"added fourth");
		addChord("add2",		"1 2 3 5",			"added second");
		addChord("5",			"1 5",				"power");
	}

    /**
     * The Identifier inner interface specified the contract for the class
     * that implements withNotes(int[] notes) for Chord identification.
     * @author st
     */
    public interface Identifier 
    {
    	public List<Chord.PitchedVoicing> withNotes(int[] notes);
    }

    /**
     * The DefaultIdentifier class provides the default implementation
     * of withNotes(int[] notes) for Chord identification.
     * @author st
     *
     */
    public static class DefaultIdentifier implements Identifier
    {
    	public List<Chord.PitchedVoicing> withNotes(int[] notes) {
    		List<Chord.PitchedVoicing> matches = new java.util.ArrayList<Chord.PitchedVoicing>();
    		
    		// first remove duplicate notes (identical pitch classes)
    		notes = Pitch.distinctClasses(notes);
    		int[] intervals = new int[notes.length];
    		
    		int rootStart = 0;
    		// may be a standard chord in root position
    		root(0, notes, intervals);
    		Chord chord = Chords.withIntervals(intervals);
    		if ( chord != null ) { 
    			matches.add(new Chord.PitchedVoicing(new Chord.Voicing(chord), notes[0]));
    			rootStart += 1;
    		}

    		boolean sixth;
    		List<Chord.Voicing> voicings;
    		for ( int missing = 0; missing < 3; missing++ ) {
//    			if ( missing > 0 ) System.out.println("trying "+missing+" missing");
        		for ( int root = rootStart; root < notes.length; root++) {
        			int nmatch = 0;
            		root(root, notes, intervals);
            		compress(intervals);
            		sixth = expand(intervals); // 6 -> 13 if present            		
            		voicings = Chords.withIntervals(intervals, missing);      		
           			for ( int i = 0; i < voicings.size(); i++ ) {
           				matches.add(new Chord.PitchedVoicing(voicings.get(i), notes[root], notes[0]));
           				nmatch += 1;
           			}
            		if ( sixth ) {
            			toggle(intervals, MAJOR_SIXTH); // 13 -> 6        			
            			voicings = Chords.withIntervals(intervals, missing);
           				for ( int i = 0; i < voicings.size(); i++ ) {
           					matches.add(new Chord.PitchedVoicing(voicings.get(i), notes[root], notes[0]));
               				nmatch += 1;
           				}
            		}
            		// try slash chords that don't include the 'bass note'
            		// but only if no other matches
            		if ( nmatch == 0 ) {
            			if ( root == notes.length-1 ) continue;
            			int[] slashNotes = new int[notes.length -1];
                		int[] slashIntervals = new int[slashNotes.length];
            			int bassNote = notes[root];
            			for ( int i = 0, j = 0; i < notes.length; i++ ) {
            				int note = notes[i];
            				if ( note == bassNote ) continue;
            				slashNotes[j++] = note;
            			}
        				root(root, slashNotes, slashIntervals);
                		compress(slashIntervals);
                		sixth = expand(slashIntervals); // 6 -> 13 if present            		
                		voicings = Chords.withIntervals(slashIntervals, missing);
               			for ( int i = 0; i < voicings.size(); i++ ) {
               				matches.add(new Chord.PitchedVoicing(voicings.get(i), slashNotes[root], bassNote));
               				nmatch += 1;
               			}
                		if ( sixth ) {
                			toggle(intervals, MAJOR_SIXTH); // 13 -> 6        			
                			voicings = Chords.withIntervals(slashIntervals, missing);
               				for ( int i = 0; i < voicings.size(); i++ ) {
               					matches.add(new Chord.PitchedVoicing(voicings.get(i), slashNotes[root], bassNote));
                   				nmatch += 1;
               				}
                		}
            		}
        		}
        		// don't try more missing notes if already have matches
        		if ( matches.size() > 0 ) break;
    		}
    		return matches;
    	}

    	// side-effects
    	protected void root(int root, final int[] notes, int[] intervals) {
    		int tmp;
    		for ( int i = 0; i < intervals.length; i++) {
    			tmp = notes[i] - notes[root];
        		// ensure within range 0..23?
    			while ( tmp < 0 ) tmp += 12;
    			while ( tmp > 23 ) tmp -=12;
    			intervals[i] = tmp;
    		}    		
    		// ensure in order
     		Arrays.sort(intervals);
    	}
    	
    	// side-effects, requires sorted input
    	// compresses to one octave
    	protected void compress(int[] intervals) {
    		for ( int i = 0; i < intervals.length; i++) {
    			intervals[i] = intervals[i] % 12;
    		}
    		// ensure in order
     		Arrays.sort(intervals);    		
    	}
    	
    	// side-effects, requires sorted input
    	// expands to nearly two octaves to match Chord intervals
    	// returns true if sixth was promoted to thirteenth
    	protected boolean expand(int[] intervals) {
    		int tmp;
    		int tmp2;
    		int len = intervals.length;
    		boolean sixth = false;
    		for ( int i = 0; i < len; i++) {
    			tmp = intervals[i];
    			switch ( tmp ) {
    	        case UNISON: // "1"
    	        case MAJOR_THIRD: // "3"
    	        case PERFECT_FIFTH: // "5"
    	        case MINOR_SEVENTH: // "b7"
    	        case MAJOR_SEVENTH: // "7"
	        		continue; // these always stay in the first octave
	        		
    	        case MINOR_SECOND: // "b2", always b9, b2 never occurs
    	        	tmp += 12;
    	        	break;
    	        	
    	        case MAJOR_SECOND: // "2"
    	        	if ( i + 1 >= len ) break;
    	        	tmp2 = intervals[i+1];
    	        	if ( tmp2 == MINOR_THIRD || 
    	        		 tmp2 == MAJOR_THIRD ||
    	        		 tmp2 == PERFECT_FOURTH ) // for 13sus4
    	        		tmp += 12;
    	        	break;
    	        	
    	        case MINOR_THIRD: // "b3"
    	        	// #9 if 3 present
    	        	if ( i + 1 >= len ) break;
    	        	tmp2 = intervals[i+1];
    	        	if ( tmp2 == MAJOR_THIRD ) 
    	        		tmp += 12;
    	        	break;
    	        	
    	        case PERFECT_FOURTH: // "4"
    	        	// 11 if b3 or 3 present
    	        	if ( i - 1 < 0 ) break;
    	        	tmp2 = intervals[i-1];
    	        	if ( tmp2 == MINOR_THIRD || 
    	        		 tmp2 == MAJOR_THIRD )
    	        		tmp += 12;
    	        	break;

    	        case DIMINISHED_FIFTH: // "b5"
    	        	// #11 if 5 or #5 present
    	        	if ( i + 1 >= len ) break;
    	        	tmp2 = intervals[i+1];
	        		if ( tmp2 == PERFECT_FIFTH ||
	        			 tmp2 == AUGMENTED_FIFTH )
	        			tmp += 12;
    	        	break;
    	        	
    	        case AUGMENTED_FIFTH: // "#5" !!! !!! m11-5-9-13
	        		// b13 if b5 or 5
    	        	if ( i - 1 < 0 ) break;
    	        	tmp2 = intervals[i-1];
    	        	if ( tmp2 == DIMINISHED_FIFTH )
    	        		tmp += 12;
    	        	break;

    	        case MAJOR_SIXTH: // "6"
    	        	// try 13, no way of deciding with potentially missing notes
   	        		tmp += 12;
   	        		sixth = true; // indicate also toggle 13->6 and try to match
    	        	break;
    			}
    			intervals[i] = tmp;
    		}
    		// ensure in order
     		Arrays.sort(intervals);
     		return sixth;
    	}

    	protected void toggle(int[] intervals, int value) {
    		for ( int i = 0; i < intervals.length; i++) {
    			if ( intervals[i] % 12 == value ) {
    				// toggle between value and value + 12
    				intervals[i] = value + value + 12 - intervals[i];
    				break;
    			}
    		}
    		// ensure in order
     		Arrays.sort(intervals);
        }
    }

}
