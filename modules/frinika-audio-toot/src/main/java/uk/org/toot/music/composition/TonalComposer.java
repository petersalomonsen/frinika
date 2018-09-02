//Copyright (C) 2007 Steve Taylor.
//Distributed under the Toot Software License, Version 1.0. (See
//accompanying file LICENSE_1_0.txt or copy at
//http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.composition;

import java.util.BitSet;

import uk.org.toot.music.Note;
import uk.org.toot.music.tonality.*;

/**
 * This class composes melodies, one bar at a time.
 * @author st
 *
 */
public class TonalComposer extends AbstractComposer
{
	private int currentPitch;
	private Key key = null;
	private BitSet timing = null;
	private String name;

	public TonalComposer(String name) {
		this.name = name;
	}
	
	public String getName() { return name; }
	
	public int[] composeBar(BarContext barContext) {
		Key[] keys = barContext.getKeys();
		int[] times = barContext.getKeyTimes();
		assert keys.length == times.length;
		int keyCount = keys.length;
		int nkey = 0;
		if ( key == null ) key = keys[0];
		if ( timing == null || getContext().getRepeatTimingProbability() < Math.random() ) {
			timing = getContext().getTimingStrategy().createTiming(barContext.getMeter());
		}
		// jam key change times into timing
		// to force existing notes to end
		for ( int i = 0; i < times.length; i++ ) {
			timing.set(times[i]);
		}
		int n = timing.cardinality();
		int[] polys = new int[n];
		int m = 0;
		for ( int i = 0; i < n; i++) {
			if ( Math.random() >= getContext().getMelodyProbability() ) {
				polys[i] = getContext().getMinPoly() + 
					(int)(Math.random() * 
							(1 + getContext().getMaxPoly() - getContext().getMinPoly()));
			} else {
				polys[i] = 1; // a melody
			}
			m += polys[i];
		}
		int[] notes = new int[m];
		n = 0;
		m = 0;
		int[] chordNotes;
		int offset;
		for ( int i = 0; i < timing.size(); i++) {
			if ( !timing.get(i) ) continue;
			if ( nkey < keyCount && times[nkey] == i ) {
				key = keys[nkey];
				nkey += 1;
			}
			// calculate duration
			int iOff = timing.nextSetBit(i+1);
			if ( iOff < 0 ) iOff = barContext.getMeter();
			int duration = Math.max(1, (int)(getContext().getLegato() * (iOff - i)));
			// calculate pitches
			currentPitch = getContext().nextPitch(currentPitch, key);
			int mm = m;
			do {
				if ( mm != m ) {
					currentPitch -= 1; // shouldn't be an avoid note by definition
//					System.out.println(getName()+" trying "+Pitch.className(currentPitch));
					m = mm;
				}
				if ( polys[n] > 1 ) {
					int chordInterval = ChordMode.TERTIAN;
					if ( Math.random() > getContext().getTertianProbability() ) {
						chordInterval = ChordMode.QUARTAL;
					}
					chordNotes = key.getChordNotes(key.index(currentPitch), polys[n], chordInterval);
					offset = currentPitch - chordNotes[0];
					for ( int p = 0; p < polys[n]; p++ ) {
						notes[m++] = Note.createNote(i, chordNotes[p] + offset, getContext().getLevel(i), duration);
					}
				} else {
					notes[m++] = Note.createNote(i, currentPitch, getContext().getLevel(i), duration);
				}
			} while ( avoid(notes[mm], barContext.getAvoidNotes()));
			n += 1;
		}
		// create avoid notes if first chord composer !!!
		if ( barContext.getAvoidNotes() == null && getContext().getMelodyProbability() < 0.5f ) {
			barContext.setAvoidNotes(createAvoidNotes(notes, key));
		}
		return notes;
	}

	// do we avoid any avoid note
	protected boolean avoid(int note, int[] avoidNotes) {
		if ( avoidNotes == null ) return false;
		for ( int i = 0; i < avoidNotes.length; i++ ) {
			if ( avoid(note, avoidNotes[i]) ) return true;
		}
		return false;
	}
	
	// do we avoid this avoid note
	// i.e. do times overlap and same pitch class
	protected boolean avoid(int note, int avoidNote) {
		int notePC = Pitch.classValue(Note.getPitch(note));
		int avoidPC = Pitch.classValue(Note.getPitch(avoidNote));
		if ( notePC != avoidPC ) return false; // different pitch class
		int noteOn = Note.getTime(note);
		int noteOff = noteOn + Note.getDuration(note);
		int avoidOn = Note.getTime(avoidNote);
		int avoidOff = avoidOn + Note.getDuration(avoidNote);
		if ( noteOff <= avoidOn ) return false; // note is before avoid note
		if ( noteOn >= avoidOff ) return false; // note is after avoid note
		// TODO possibly accept short duration passing notes
//		System.out.println(getName()+" avoiding "+Pitch.className(notePC)+" at "+noteOn);
		return true; // note overlaps avoid note
	}
	
	// we create all avoid notes, whether diatonic or not
	// we keep as pitches, relative register may affect avoid probability
	protected int[] createAvoidNotes(int[] notes, Key key) {
		int[] avoidNotes = new int[notes.length];
		for ( int i = 0; i < notes.length; i++) {
			int note = notes[i];
//			System.out.print(getName()+" "+Pitch.className(Note.getPitch(note)));
//			System.out.print(" from "+Note.getTime(note)+" to "+(Note.getDuration(note)+Note.getTime(note)));
			note = Note.setPitch(note, Note.getPitch(note)+1); // one semitone above
			avoidNotes[i] = note;
//			System.out.println(" so avoid "+Pitch.className(Note.getPitch(note)));
		}
/*		System.out.print("Avoid notes: ");
		for ( int i = 0; i < avoidNotes.length; i++ ) {
			System.out.print(Pitch.className(Note.getPitch(avoidNotes[i]))+' ');
		}
		System.out.println(); */
		return avoidNotes;
	}
	
	public Context getContext() {
		return (Context)super.getContext();
	}

	public static class Context extends AbstractComposer.Context
	{
		private int minPitch;
		private int maxPitch;
		private int maxPitchChange = 3;
		private int minPoly = 3;
		private int maxPoly = 5;
		private float legato = 1.0f;
		private float melodyProbability = 0f; // probability of melody (single notes)
		private float tertianProbability = 1f;
		private float leapProbability = 0.5f;
		
		public int nextPitch(int pitch, Key key) {
//			if ( Math.random() > getRepeatPitchProbability() ) {
				int maxPitchChange = getMaxPitchChange();
				if ( maxPitchChange > 2 && Math.random() > getLeapProbability() ) {
					maxPitchChange = 2; // force a step or skip rather than a leap
				}
				int offset = (int)((2 * maxPitchChange + 1) * Math.random() - maxPitchChange);
				// don't get stuck at min or max pitches
				if ( pitch == getMinPitch() && offset < 0 || 
						pitch == getMaxPitch() && offset > 0 ) {
					offset = -offset;
				}
				pitch = key.getRelativePitch(pitch, offset);
//			}
			if ( pitch < getMinPitch() || pitch > getMaxPitch() ) {
				pitch = getMinPitch() + (int)(Math.random() * (getMaxPitch() - getMinPitch()));
			}
			return key.diatonicPitch(pitch);
		}

		/**
		 * @return the minPitch
		 */
		public int getMinPitch() {
			return minPitch;
		}

		/**
		 * @param minPitch the minPitch to set
		 */
		public void setMinPitch(int minPitch) {
			this.minPitch = minPitch;
		}

		/**
		 * @return the maxPitch
		 */
		public int getMaxPitch() {
			return maxPitch;
		}

		/**
		 * @param maxPitch the maxPitch to set
		 */
		public void setMaxPitch(int maxPitch) {
			this.maxPitch = maxPitch;
		}

		/**
		 * @return the maxPitchChange
		 */
		public int getMaxPitchChange() {
			return maxPitchChange;
		}

		/**
		 * @param maxPitchChange the maxPitchChange to set
		 */
		public void setMaxPitchChange(int maxPitchChange) {
			this.maxPitchChange = maxPitchChange;
		}

		/**
		 * @return the proportion of full legato that a note sustains
		 */
		public float getLegato() {
			return legato;
		}

		/**
		 * Set the proportion of full legato that a note should sustain.
		 * @param legato the proportion of full legato, 0..1f
		 */
		public void setLegato(float legato) {
			this.legato = legato;
		}

		/**
		 * @return the maxPoly
		 */
		public int getMaxPoly() {
			return maxPoly;
		}

		/**
		 * @param maxPoly the maxPoly to set
		 */
		public void setMaxPoly(int maxPoly) {
			this.maxPoly = maxPoly;
		}

		/**
		 * @return the minPoly
		 */
		public int getMinPoly() {
			return minPoly;
		}

		/**
		 * @param minPoly the minPoly to set
		 */
		public void setMinPoly(int minPoly) {
			this.minPoly = minPoly;
		}

		/**
		 * @return the melody
		 */
		public float getMelodyProbability() {
			return melodyProbability;
		}

		/**
		 * @param melody the melody to set
		 */
		public void setMelodyProbability(float melody) {
			this.melodyProbability = melody;
		}

		/**
		 * @return the tertianProbability
		 */
		public float getTertianProbability() {
			return tertianProbability;
		}

		/**
		 * @param tertianProbability the tertianProbability to set
		 */
		public void setTertianProbability(float tertianProbability) {
			this.tertianProbability = tertianProbability;
		}

		/**
		 * @return the stepProbability
		 */
		public float getLeapProbability() {
			return leapProbability;
		}

		/**
		 * @param stepProbability the stepProbability to set
		 */
		public void setLeapProbability(float stepProbability) {
			this.leapProbability = stepProbability;
		}
	}
}
