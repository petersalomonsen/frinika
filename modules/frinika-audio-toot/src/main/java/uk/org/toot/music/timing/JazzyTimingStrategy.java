// Copyright (C) 2008 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.timing;

import java.util.BitSet;
import java.util.HashMap;
import uk.org.toot.music.MeterCoding;

import static uk.org.toot.music.timing.Timing.*;

/**
 * Generate somewhat jazzy timing especially suited to lead lines.
 * Each note length has a probability of occuring so unlike ConventionalTimingStrategy
 * shorter note lengths can occur without longer note lengths.
 * e.g. upbeats can occur without downbeats, potentially resulting in syncopation
 * relative to conventional timing.
 * Triplets are possible but all three notes are always present.
 * @author st
 *
 */
public class JazzyTimingStrategy extends AbstractTimingStrategy
{
	private Context context;
	private int nTicks;
	private int startTick;
	private int endTick;
	
	public JazzyTimingStrategy(Context context) {
		this.context = context;
	}
	
	public BitSet createTiming(int nTicks) {
		this.nTicks = nTicks;
		startTick = endTick = 0;
		BitSet bitSet = new BitSet(nTicks);
		divideBar(bitSet);
		return bitSet;
	}

	protected void divideBar(BitSet bitset) {
		while ( endTick < nTicks ) {
			startTick = endTick;
			if ( remaining(QUARTER_NOTE) && MeterCoding.asString(nTicks).endsWith("4")) {
				if ( chance(EIGHTH_NOTE_TRIPLET) ) {
					// 3 8th note triplets
					bitset.set(startTick);
					bitset.set(startTick+EIGHTH_NOTE_TRIPLET);
					bitset.set(startTick+EIGHTH_NOTE_TRIPLET+EIGHTH_NOTE_TRIPLET);
				} else {
					if ( chance(QUARTER_NOTE) ) {
						// 1 quarter note
						bitset.set(startTick);
					}
					if ( chance(SIXTEENTH_NOTE) ) {
						// 1 16th note between quarter and 8th
						bitset.set(startTick+SIXTEENTH_NOTE);
					}
					if ( chance(EIGHTH_NOTE) ) {
						// 1 8th note (between quarter notes)
						bitset.set(startTick+EIGHTH_NOTE);
					}
					if ( chance(SIXTEENTH_NOTE) ) {
						// 1 16th note between 8th and quarter
						bitset.set(startTick+EIGHTH_NOTE+SIXTEENTH_NOTE);
					}
				}
				endTick += QUARTER_NOTE;
			} else if ( remaining(EIGHTH_NOTE) ) {
				if ( chance(SIXTEENTH_NOTE_TRIPLET) ) {
					// 3 16th note triplets
					bitset.set(startTick);
					bitset.set(startTick+SIXTEENTH_NOTE_TRIPLET);
					bitset.set(startTick+SIXTEENTH_NOTE_TRIPLET+SIXTEENTH_NOTE_TRIPLET);
				} else  {
					if ( chance(EIGHTH_NOTE) ) {
						// 1 8th note
						bitset.set(startTick);
					}
					if ( chance(SIXTEENTH_NOTE) ) {
						// 1 16th note between 8ths
						bitset.set(startTick+SIXTEENTH_NOTE);
					}
				}
				endTick += EIGHTH_NOTE;
			} else { 
				// x/16 meter, final 16th
				bitset.set(startTick);
				endTick += SIXTEENTH_NOTE;
			}
		}
	}
	
	protected boolean remaining(int ticks) {
		return startTick + ticks <= nTicks;
	}
	
	protected boolean chance(int noteLength) {
		return Math.random() < probability(noteLength);
	}
	
	protected float probability(int noteLength) {
		return context.getProbability(noteLength);
	}
	
	public static class Context
	{
		private HashMap<Integer, Float> map = new HashMap<Integer, Float>();
		private float defaultProbability = 0.25f;
		
		public Context() {	
		}
		
		public Context(float defaultProbability) {
			this.defaultProbability = defaultProbability;
		}
		
		public float getProbability(int noteLength) {
			Float p = map.get(noteLength);
			return p == null ? defaultProbability : p;
		}
		
		public void setProbability(int noteLength, float probability) {
			map.put(noteLength, probability);
		}
		
		/**
		 * Set the default probability, which is used when a note length has
		 * not had a probability explicitly set.
		 * @param probability
		 */
		public void setDefaultProbability(float probability) {
			defaultProbability = probability;
		}		
	}
}
