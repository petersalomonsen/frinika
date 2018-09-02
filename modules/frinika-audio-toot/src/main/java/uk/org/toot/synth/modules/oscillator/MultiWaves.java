// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import java.util.List;
import java.util.Collections;

/**
 * Wavetable implementations of classic waves.
 * These waves are band-limited so they do NOT alias.
 * @author st
 *
 */
public class MultiWaves 
{
	private static String SQUARE = "Square";
	private static String SAW = "Saw";

	private static MultiWave square;
	private static MultiWave saw;
	
	private static int size = 2048; // !!!
	
	private static List<String> names = new java.util.ArrayList<String>();
	
	static {
		names.add(SQUARE);		// no even harmonics, -6dB/Octave odd harmonic rolloff
		names.add(SAW);			// even and odd harmonics, -6dB/Octave rolloff
	}
	
	public static List<String> getNames() {
		return Collections.unmodifiableList(names);
	}

	public static void init() {
		square = createSquareWave(size);				
		saw = createSawtoothWave(size);		
	}
	
	public static MultiWave get(String name) {
		if ( name.equals(SQUARE) ) {
			if ( square == null ) {
				square = createSquareWave(size);				
			}
			return square;
		} else if ( name.equals(SAW) ) {
			if ( saw == null ) {
				saw = createSawtoothWave(size);
			}
			return saw;
		}
		return null;
	}
	
	/**
	 * The actual size is likely to be slightly larger than the requested size
	 * to allow for efficient linear interpolation. The requested size is actually
	 * the size of the loopable data.
	 * @param aSize
	 */
	public static void setSize(int aSize) {
		size = aSize;
	}
	
	public static MultiWave createSquareWave(int nsamples) {
		return new SawtoothMultiWave(nsamples, 20000); // TODO nyquist?
	}

	public static MultiWave createSawtoothWave(int nsamples) {
		return new ParabolaMultiWave(nsamples, 20000); // TODO nyquits?
	}

}
