// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.tonality;

/**
 * An immutable Chord. No mutators.
 */
public class Chord
{
    private String symbol;		// e.g. maj7
    private String spelling;	// e.g. 1 3 5 7
    private String name;		// e.g. major seventh
    private int[]  intervals; 	// derived from spelling, includes UNISON at [0]

    public Chord(String aSymbol, String aSpelling, String aName) {
        symbol = aSymbol;
        spelling = aSpelling;
        name = aName;
        decodeIntervals(spelling);
    }

    void decodeIntervals(String aSpelling) {
		String[] intervalStrings = aSpelling.split("\\s");
        intervals = new int[intervalStrings.length];
	    for (int i = 0; i < intervals.length; i++) {
        	intervals[i] = Interval.spelt(intervalStrings[i]);
        }
    }

    /**
     * Get the symbol.
     * @return the symbol e.g. "maj7"
     */
    public String getSymbol() { return symbol; }

    /**
     * Get the spelling.
     * @return the spelling e.g. "1 3 5 7"
     */
    public String getSpelling() { return spelling; }

    /**
     * Get the name.
     * @return the name e.g. "major seventh"
     */
    public String getName() { return name; }

    /**
     * Get the intervals
     * @return the array of ints representing the intervals
     */
    public int[] getIntervals() { return intervals; }

    /**
     * Get the polyphony
     * @return the number of intervals (including UNISON)
     */
    public int getPoly() { return intervals.length; }

    /**
     * Return true if someIntervals exactly matches our intervals.
     * @param someIntervals the intervals to match to our intervals
     * @return boolean true if exact match, false otherwise
     */   
    public boolean matchesIntervals(int[] someIntervals) {
        if ( intervals.length != someIntervals.length ) return false;
        for ( int i = 0; i < intervals.length; i++ ) {
            if ( intervals[i] != someIntervals[i] ) {
            	return false; // fast false return
            }
        }
        return true; // interval arrays are identical
    }
    
    /**
     * Returns an array of missing interval indices if someIntervals
     * matches our intervals with exactly missing intervals missing.
     * missing should equal 0, 1 or 2.
     * returns null if not that exact number of missing intervals
     * @return int[] the array of missing interval indices or null if no match
     */
    public int[] missingIntervals(int[] someIntervals, int missing) {
    	// optimise for matching a single note
    	if ( someIntervals.length < 2 ) return null;
    	// optimise if no missed intervals allowed 
    	if ( missing == 0 ) {
    		return matchesIntervals(someIntervals) ? new int[0] : null;
    	}
    	// optimise for required number of misses not possible
    	if ( intervals.length != someIntervals.length + missing ) {
    		return null;
    	}
        int misses = 0; // actual missing count
        int[] missingIndices = new int[missing];
        int j = 0; // index into someIntervals
        for ( int i = 0; i < intervals.length; i++ ) {
            if ( j == someIntervals.length || intervals[i] != someIntervals[j] ) {
            	if ( i == 0 ) {
            		return null; // missed root 
            	}            	
            	if ( misses == missingIndices.length ) {
            		return null; // too many misses
            	}
            	missingIndices[misses++] = i;
            } else {
            	j += 1; // interval matched
            }
        }
        if ( j != someIntervals.length ) {
        	return null; // not enough intervals
        }
        
        if ( misses != missing ) {
        	return null; // wrong number of misses (shouldn't occur?)
        }
/*        System.out.print(getSymbol()+", "+Interval.spell(intervals)+" = "+
        	Interval.spell(someIntervals)+" missing "+misses+"/"+missing+": ");
        for ( int i = 0; i < missingIndices.length; i++ ) {
        	int mi = missingIndices[i];
        	System.out.print(Interval.spell(intervals[mi])+"["+mi+"], ");
        }
        System.out.println(); */
        return missingIndices;
    }
    
    /**
     * Return whether every interval of this chord is contained within
     * the chordMode
     * @param chordMode the chord mode to match
     * @return true if every interval of this Chord matches the chord mode
     */
    public boolean matchesChordMode(int[] chordMode) {
    	for ( int i = 0; i < intervals.length; i++ ) {
    		if ( !ChordMode.hasInterval(chordMode, intervals[i])) {
    			return false; // fast failure
    		}
    	}
    	return true;
    }
    
    public String toString() {
    	return getSymbol();
    }
   
    /**
     * A Chord.Voicing is an aggregation of a theoretical root position 
     * (i.e uninverted) Chord with all voicing information such as missing 
     * intervals and (one day) octave transpositions of present intervals.
     * It isn't rooted to a pitch though, it's still just in terms of intervals.
     * @author st
     *
     */
    public static class Voicing
    {
    	private Chord chord;
    	private int[] missingIndices = null;

    	/**
    	 * Construct a new Voicing with no missing intervals.
    	 * @param chord the Chord for this Voicing
    	 */
    	public Voicing(Chord chord) {
    		this.chord = chord;
    	}
    	
    	/**
    	 * Construct a new Voicing with missing intervals.
    	 * @param chord the Chord for this Voicing
    	 * @param missingIndices the array of the indices of the missing intervals
    	 */
    	public Voicing(Chord chord, int[] missingIndices) {
    		this.chord = chord;
    		this.missingIndices = missingIndices;
    	}
    	
    	/**
    	 * Get the Chord that this Voicing uses.
    	 * @return this Chord
    	 */
    	public Chord getChord() {
    		return chord;
    	}
    	
    	/**
    	 * Return an array of the indices of the intervals which are missing.
    	 * @return int[] the array of missing interval indices
    	 */
    	public int[] getMissingIndices() {
    		return missingIndices;
    	}
    	
    	/**
    	 * Return the array of intervals that exist after missing intervals have been
    	 * removed.
    	 * @return the array of intervals excluding missing intervals
    	 */
    	public int[] getIntervals() {
    		int[] allIntervals = getChord().getIntervals();
    		if ( missingIndices == null || missingIndices.length == 0 ) {
    			return allIntervals;
    		}
    		int[] intervals = new int[allIntervals.length - missingIndices.length];
    		for ( int i = 0, j = 0, k = 0; i < allIntervals.length; i++ ) {
    			if ( missingIndices[k] == i ) {
    				k += 1;
    				continue;
    			}
    			intervals[j++] = allIntervals[i];
    		}
    		return intervals;
    	}
    	
    	/**
    	 * Get the string representation of the missing intervals.
    	 * @return the missing notation, e.g. " no 5"
    	 */
    	public String getMissingString() {
    		String missingString = "";
    		if ( missingIndices != null ) {
    			int[] intervals = getChord().getIntervals();
    			for ( int i = 0; i < missingIndices.length; i++) {
    				missingString += " no "+Interval.spell(intervals[missingIndices[i]]);
    			}
    		}
    		return missingString;
    	}
    	
    	public String toString() {
    		return getChord().getSymbol()+getMissingString(); 
    	}    	
    }
    
    public static class RelativeVoicing extends Voicing
    {
    	private int offset;
    	
		public RelativeVoicing(int offset, Chord chord) {
			super(chord);
			this.offset = offset;
		}
    	
		public RelativeVoicing(int offset, Chord chord, int[] missingIndices) {
			super(chord, missingIndices);
			this.offset = offset;
		}

		/**
		 * @return the offset
		 */
		public int getOffset() {
			return offset;
		}
    	
    }
    /**
     * A PitchedVoicing is an aggregation of a Voicing and a root pitch.
     * @author st
     *
     */
    public static class PitchedVoicing 
    {
    	private Voicing voicing;
    	private int root;
    	private int slashBass = -1;
    	
    	/**
    	 * Construct a new PitchedVoicing of the specified Voicing with the
    	 * specified root pitch.
    	 * @param voicing the Voicing
    	 * @param root the root pitch
    	 */
    	public PitchedVoicing(Voicing voicing, int root) {
    		this.voicing = voicing;
    		this.root = root;
    	}
    	
    	/**
    	 * Construct a new PitchedVoicing of the specified Voicing with the
    	 * specified root pitch and the specified bass pitch as applicable to
    	 * slash chord notation.
    	 * @param voicing the Voicing
    	 * @param root the root pitch
    	 * @param slashBass
    	 */
    	public PitchedVoicing(Voicing voicing, int root, int slashBass) {
    		this(voicing, root);
    		this.slashBass = slashBass;
    	}
    	
    	/**
    	 * Get the Voicing
    	 * @return the Voicing
    	 */
    	public Voicing getVoicing() {
    		return voicing;
    	}
    	
    	/**
    	 * Get the root pitch for the chord voicing
    	 * @return the root pitch
    	 */
    	public int getRoot() {
    		return root;
    	}
    	
    	/**
    	 * Get the Chord for the chord voicing.
    	 * @return the Chord
    	 */
    	public Chord getChord() {
    		return getVoicing().getChord();
    	}
    	
    	/**
    	 * Get the individual pitches for the pitched chord voicing.
    	 * @return an array of ints for the note pitches.
    	 */
    	public int[] getPitches() {
    		int[] intervals = getVoicing().getIntervals(); 
    		int[] notes = new int[intervals.length];
    		for ( int i = 0; i < notes.length; i++ ) {
    			notes[i] = root + intervals[i];
    		}
    		return notes;
    		
    	}
    	
    	/**
    	 * Get the string representation of the slash chord notation
    	 * @return the slash notation, e.g. " / C"
    	 */
    	public String getSlashString() {
    		if ( slashBass < 0 || slashBass == root ) return "";
    		return " / "+Pitch.className(slashBass);
    	}
    	
    	public String toString() {
    		return Pitch.className(root)+getVoicing().toString()+getSlashString();
    	}    	
    }   
    
    /**
     * A Progression is a list of chords, each with their offset to a nominal root.
     * Typically relative to the tonic chord of a key center.
     */
    public static interface Progression
    {
    	int getBarCount();
    	int getStepCount();
    	
    	/**
    	 * @param bar
    	 * @param chord
    	 * @return a RelativeVoicing or null
    	 */
    	RelativeVoicing getRelativeVoicing(int bar, int step);
    	
    	/**
    	 * @param bar - the bar within the progression
    	 * @param step - the step within the bar
    	 * @param root - the root of the relative voicing
    	 * @return int[] of pitches
    	 */
    	int[] getPitches(int bar, int step, int root);
    }
    
    public static abstract class AbstractProgression implements Progression
    {
    	private int barCount;
    	private int stepCount;
    	private RelativeVoicing[][] relativeVoicings;
    	    	
    	public AbstractProgression(int barCount, int stepCount) {
    		this.barCount = barCount;
    		this.stepCount = stepCount;
			relativeVoicings = new RelativeVoicing[barCount][stepCount];
    	}

		public int getBarCount() {
			return barCount;
		}

		public int getStepCount() {
			return stepCount;
		}

		public RelativeVoicing getRelativeVoicing(int bar, int step) {
			return relativeVoicings[bar][step];
		}
		
		public int[] getPitches(int bar, int step, int root) {
			RelativeVoicing voicing = getRelativeVoicing(bar, step);
			int offset = voicing.getOffset();
			int[] pitches = voicing.getChord().getIntervals();
			for ( int i = 0; i < pitches.length; i++) {
				pitches[i] += root + offset;
			}
			return pitches;
		}

		/**
		 * @param bar - the bar
		 * @param step - the step within the bar
		 * @param voicing - the RelativeVoicing to be added at this position
		 */
		protected void add(int bar, int step, int offset, String symbol) {
			Chord chord = Chords.withSymbol(symbol);
			relativeVoicings[bar][step] = new RelativeVoicing(offset, chord);
		}
    }
    
    /*
     * String symbol would typically be "maj", "maj7" or "7"
     */
    public static class I_bIII_bVI_bII_TurnAround extends AbstractProgression 
    {
		public I_bIII_bVI_bII_TurnAround(String symbol) {
			super(2, 2); // 2 bars, 2 chords per bar
			add(0, 0, 0, symbol); // I
			add(0, 1, 3, symbol); // bIII
			add(1, 0, 8, symbol); // bVI
			add(1, 1, 1, symbol); // bII
		}
    }
    
    public static class ii_V7_I_I_Progression extends AbstractProgression
    {
     	public ii_V7_I_I_Progression() {
    		super(4, 1); // 4 bars, 1 chord per bar
			add(0, 0, 2, "min"); 		// ii
			add(1, 0, 7, "7"); 	// V7
			add(2, 0, 0, "maj"); 		// I
			add(3, 0, 0, "maj"); 		// I
    	}
    }

    /*
     * from http://www.lucaspickford.com/transsubs.htm
     * Extending the Coltrane Changes by David Baker
     * TODO substitutions and alterations
     */
    public static class CountdownProgression extends AbstractProgression
    {
    	private float expansionProbability;
    	private float transpositionProbability;
    	
		public CountdownProgression(float expansionProbability, 
									float transpositionProbability) {
			super(4, 4); // 4 bars, 4 chords per bar
			this.expansionProbability = expansionProbability;
			this.transpositionProbability = transpositionProbability;
			
			// all except first and last chords may be transposed by 3, 6 or 9 semitones
			
			// first bar, first chord isn't transposed
			add(0, 0, 2, "min");	// ii, sub dim7
			// chance of V7 on step 1
			if ( expand() ) {
				add(0, 1, transpose(7), "7");
			}
			// steps 2 and 3 either bIII7/null or bVII/bIII7
			if ( expand() ) {
				add(0, 2, transpose(10), "maj");
				add(0, 3, transpose(3), "7"); // sub 7+9+5
			} else {
				add(0, 2, transpose(3), "7"); // sub 7+9+5
			}
			
			// second bar
			add(1, 0, transpose(8), "maj");	// bVI
			// steps 2 and 3 either VII7/null or bV/VII7
			if ( expand() ) {
				add(0, 2, transpose(6), "maj");
				add(0, 3, transpose(11), "7"); // sub 7+11
			} else {
				add(0, 2, transpose(11), "7"); // sub 7+11
			}
			
			// third bar
			add(2, 0, transpose(4), "maj");	// III
			// steps 2 and 3 either V7/null or ii/V7
			if ( expand() ) {
				add(0, 2, transpose(2), "min");
				add(0, 3, transpose(7), "7"); // sub 7-5
			} else {
				add(0, 2, transpose(7), "7"); // sub 7-5
			}
			
			// fourth bar, last chord isn't transposed
			add(3, 0, 0, "maj"); 	// I, sub #11
		}   	
		
		protected boolean expand() {
			return Math.random() < expansionProbability;
		}
		
		protected int transpose(int degree) {
			if ( Math.random() > transpositionProbability ) return degree;
			int offset = 3 * (int)(Math.random() * 4);
			return (degree+offset) % 12;
		}
    }
}
