// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.tonality;

import java.util.Observable;

/**
 * A Key has a root pitch class and a Scale
 * It does not have a register.
 * It is Observable to simplify modulation
 */
public class Key extends Observable
{
    protected int root;

    /**
     * @link aggregation 
     */
    protected Scale scale;

	protected String[] names;

	/**
     * Constructor
     */
    public Key(int root, Scale scale) {
        this.root = Pitch.classValue(root) ;
        this.scale = scale ;
        decideNoteNames();
    }

    public Key(String root, Scale scale) {
        this(Pitch.classValue(root), scale);
    }

    public Key(int root) {
        this(root, Scales.getInitialScale());
    }

    public Key() {
        this("C", Scales.getInitialScale());
    }

    /**
     * Return whether this Key contains all the specified pitches.
     * @param pitches the array of int values of the pitches
     * @return true if this Key contains all the pitches, false otherwise
     */
	public boolean contains(int[] pitches) {
        // if any note isn't diatonic return false
        for ( int i = 0 ; i < pitches.length ; i++ ) {
            if ( !contains(pitches[i]) )
                return false ;
        }
        return true ;
    }

    /**
     * Return whether this Key contains the specified pitch.
     * @param pitch the int value of the pitch
     * @return true if this Key contains the pitch, false otherwise
     */
	public boolean contains(int pitch) {
        return index(pitch) >= 0 ;
    }

	/**
	 * Return the index of a pitch into this Key's Scale, effectively a linear measure
	 * of the degree.
	 * @param pitch the int value of the pitch
	 * @return the index into the Key's Scale or -1 if not a diatonic pitch
	 */
    public int index(int pitch) {
        for ( int i = 0 ; i < scale.length() ; i++ ) {
            if ( Pitch.classValue(pitch) == Pitch.classValue(getNote(i)) )
                return i ; // diatonic match
        }
        return -1;
    }

    /**
     * Get the note names for this Key
     * @return the array of names or null if names have not been explicitly set
     */
    public String[] getNames(){ return names; }

    /**
     * Set the not names for this Key.
     * permits correction of enharmonic spelling!
     */
    public void setNames(String[] names){ this.names = names; }

    /**
     * Get the root pitch class of this Key.
     * @return the int value of the root pitch class
     */
    public int getRoot(){ return root; }

    /**
     * Set the root pitch class for this Key and notify Observers.
     * @param root the new root pitch class for this key
     */
    public void setRoot(int root) {
        this.root = Pitch.classValue(root);
        decideNoteNames();
        setChanged();
        notifyObservers();
//        System.out.println("Key of "+PitchClass.name(root));
    }

    /**
     * Get the Scale for this Key.
     * @return the Scale
     */
    public Scale getScale() { return scale; }

    /**
     * Set the Scale for this Key and notify Observers.
     * @param scale the new Scale for this Key.
     */
    public void setScale(Scale scale) {
        this.scale = scale;
        decideNoteNames();
        setChanged();
        notifyObservers();
    }

    /**
     * Returns the nearest diatonic pitch, preference to lower
     * @param pitch the possibly non-diatonic pitch
     * @return the int value of the nearest diatonic pitch
     */
    public int diatonicPitch(int pitch) {
        if ( contains(pitch) ) return pitch;
        for ( int disp = 1; disp < 3; disp++ ) {
            if ( pitch - disp > 0 && contains(pitch-disp) ) return pitch-disp;
            if ( pitch + disp < 128 && contains(pitch+disp) ) return pitch+disp;
        }
        return pitch; // oh well, it's still accidental after all!
    }

    public int getRelativePitch(int pitch, int offset) {
    	pitch = diatonicPitch(pitch);
    	if ( offset == 0 ) return pitch;
    	// absent a better algorithm we just move by chromatic semitones
    	// counting diatonic pitches as we go
    	// efficiency proportional to the magnitude of offset 
    	if ( offset < 0 ) {
    		while ( offset < 0 && pitch > 0 ) {
    			if ( contains(--pitch) ) offset += 1;
    		}
    		
    	} else if ( offset > 0 ) {
    		while ( offset > 0 && pitch < 127 ) {
    			if ( contains(++pitch) ) offset -= 1;
    		}
    	}
    	return pitch;
    }
    
    /**
     * Return the note derived from the index into the Key.
     * A returned note may no longer be a pitch class, it may extend
     * into the second octave of pitches.
     * @param index the index into the Key's Scale
     * @return the note
     */
    public int getNote(int index) {
    	return getRoot() + getScale().interval(index);
    }
    
    /**
     * Return the notes of a chord with specified polyphony and interval.
     * The notes may no longer all be pitch classes since they may extend
     * into the second octave of pitches.
     * @param index the index into the Key's Scale
     * @param poly the number of notes, 2 .. 7
     * @param lowInterval ChordMode.SECUNDAL, ChordMode.TERTIARY or ChordMode.QUARTAL
     * @return the notes of the chord
     */
    public int[] getChordNotes(int index, int poly, int lowInterval) {
        int[] notes = new int[poly];
        int[] chordMode = getScale().getChordMode(index);
        int[] intervals = ChordMode.getIntervals(chordMode, poly, lowInterval);
        int base = getNote(index);
        for ( int n = 0; n < poly; n++ ) {
            notes[n] = base + intervals[n];
        }
        return notes;
    }

    /**
     * Return the name of this Key as root note (pitch class) and Scale.
     */
    public String name() {
        return name(0)+" "+scale.getName() ;
    }

    /**
     * Return the name of a note indexed into the Key
     * @param index
     */
    public String name(int index) {
        if ( names == null )
            return Pitch.className(getNote(index)) ;

        return names[index % names.length] ;
    }

    // TODO PROPERLY
    public boolean equals(Object o) {
        if ( o instanceof Key ) {
            return toString().equals(((Key)o).toString());
        }
        return super.equals(o);
    }
    
    /**
     * Return the name of this Key, e.g. "C Major" and it's notes
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(80);
        sb.append(name());
        sb.append(":  ");
        for ( int i = 0; i < scale.length(); i++ ) {
            sb.append(names[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    protected void decideNoteNames() {
        String[] flatNames = new String[scale.length()];
        String[] sharpNames = new String[scale.length()];
        int flatRepeats = 0, sharpRepeats = 0;
        for ( int i = 0; i < scale.length(); i++ ) {
            int n = getNote(i);
            flatNames[i] = Pitch.classFlatName(n);
            sharpNames[i] = Pitch.classSharpName(n);
            if ( i > 1 ) {
                if ( flatNames[i].charAt(0) == flatNames[i-1].charAt(0) ) flatRepeats += 1;
                if ( sharpNames[i].charAt(0) == sharpNames[i-1].charAt(0) ) sharpRepeats += 1;
            }
        }
        int last = scale.length()-1;
        if ( flatNames[0].charAt(0) == flatNames[last].charAt(0) ) flatRepeats += 1;
        if ( sharpNames[0].charAt(0) == sharpNames[last].charAt(0) ) sharpRepeats += 1;
        names = sharpRepeats < flatRepeats ? sharpNames : flatNames;
    }
    
    /**
     * A Provider provides a mutable Key for Observers to observe.
     * Typically at any given time an application will use a single Key.Provider, 
     * perhaps a key track from a sequencer or perhaps a virtual musician.
     * @author st
     *
     */
    public interface Provider
    {
    	/**
    	 * Get the provided Key which may then be observed for changes.
    	 * @return the mutable Key
    	 */
        Key getKey();
    }

}
