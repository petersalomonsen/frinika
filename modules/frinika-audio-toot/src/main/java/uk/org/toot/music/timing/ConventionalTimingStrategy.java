// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.timing;

import java.util.BitSet;

import static uk.org.toot.music.timing.Timing.*;

/**
 * Generate conventional timing using recursive binary subdivision.
 * Always has a beat on ONE.
 * If the meter is 4/4, 2/4 or 8/4 the whole bar is subdivided in one action.
 * Otherwise the first part of the bar with the longest binary note length
 * is subdivided and the remaining part is iteratively subdivided into binary related
 * note lengths. e.g. 7/4 is divided into a whole note followed by half note
 * followed by an eighth note.
 * 
 * @author st
 *
 */
public class ConventionalTimingStrategy extends AbstractTimingStrategy
{
	private float density;
	private float serialDensity;
	private int minNoteLength;
	
	/**
	 * @param density the probability of subdividing a bar
	 * @param minnotelen the minimum note length to subdivide to
	 * @param serialDensity the probability of serial subdivisions
	 */
	public ConventionalTimingStrategy(float density, int minnotelen, float serialDensity) {
		this.density = density;
		this.serialDensity = serialDensity;
		minNoteLength = minnotelen;
		if ( minNoteLength > WHOLE_NOTE ) minNoteLength = WHOLE_NOTE; // prevent zero resolution
		else if ( minNoteLength < 1 ) minNoteLength = THIRTYSECOND_NOTE; // !!! <1 ???
	}

	/**
	 * Stochastic Binary Subdivision
	 * from Six Techniques for Algorithmic Music Composition, 1985
	 * by Peter S. Langston, Bellcore, Morristown, New Jersey
	 *
	 * Doesn't use triplets or dotted notes.
	 * Always sounds correct but is only a subset of possible timings.
	 */
	protected void subdivide(BitSet bitSet, int lo, int hi) {
		bitSet.set(lo); 				// mark this division start
		int mid = (lo + hi) >> 1; 		// the point of next subdivision
		if ( Math.random() < density && hi - lo > minNoteLength) {
			subdivide(bitSet, lo, mid); // lower subdivision
			subdivide(bitSet, mid, hi); // higher subdivision
		}		
	}
	
	public BitSet createTiming(int nTicks) {
		BitSet bitSet = new BitSet(nTicks);
		int startTick;
		int endTick = 0;
		while ( endTick < nTicks ) {
			startTick = endTick;
			float d = serialDensity;
			for ( int len = 2 * WHOLE_NOTE; len >= SIXTEENTH_NOTE; len /= 2) {
				if ( startTick + len <= nTicks ) {
					endTick += len;
					break;
				}
//				d *= d; // each smaller division is less likely, since d < 1
			}
			// a heuristic to prevent rigid timing of serial subdivisions
			// e.g. won't always get 5 in 5/4
			// e.g. won't always get 5 and 7 in 7/4
			// but will always get 1 in any meter
			if ( startTick == 0 || Math.random() < d ) {
				subdivide(bitSet, startTick, endTick);
			}
		}
		return bitSet;
	}
}
