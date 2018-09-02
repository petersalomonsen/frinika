// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import uk.org.toot.dsp.Phasor;
import uk.org.toot.dsp.Sine;

/**
 * A model of an oscillator for a Hammond drawbar organ, even though a
 * real Hammond drawbar organ has 91 continuously running tonewheels.
 * @author st
 */
public class HammondOscillator
{
	private int nsines = 0;
	
	private Phasor[] sines;
	private float[] levels;
	
	/*
	 * The drawbars are named according to organ stops
	 * 16'		  	0.5		1 octave below
	 *  5 1/3'	    1.5		a fifth above (3rd harmonic of octave below)
	 *  8'			1		fundamental
	 *  4'			2		1 octave above
	 *  2 2/3'		3		1 octaves and a fifth above
	 *  2'			4		2 octaves above
	 *  1 3/5'		5		2 octaves and a major third above
	 *  1 1/3'		6		2 octaves and a fifth above
	 *  1'			8		3 octaves above
	 * but see http://www.electricdruid.net/index.php?page=info.hammond
	 * for exact ratios which are sort of equal temperament.
	 * Ideally each Phasor would start at different times, high frequencies first,
	 * over a 1 to 40ms period depending on key velocity. 
	 */
	public HammondOscillator(float wn, float wmax, float[] levels) {
		this.levels = levels;
		nsines = levels.length;
		sines = new Phasor[nsines];
		sines[0] = createPhasor(wmax, wn * 0.5);
		sines[1] = createPhasor(wmax, wn * 1.498823530); 	
		sines[2] = createPhasor(wmax, wn);
		sines[3] = createPhasor(wmax, wn * 2);
		sines[4] = createPhasor(wmax, wn * 2.997647060);
		sines[5] = createPhasor(wmax, wn * 4);
		sines[6] = createPhasor(wmax, wn * 5.040941178);
		sines[7] = createPhasor(wmax, wn * 5.995294120);
		sines[8] = createPhasor(wmax, wn * 8);
	}
	
	protected Phasor createPhasor(float wmax, double w) {
		while ( w > wmax ) w *= 0.5f; // foldback, may loop once or twice
		return new Sine(w);
	}
	
	public float getSample() {
		float sample = 0f;
		for ( int i = 0; i < nsines; i++ ) {
			sample += sines[i].out() * levels[i];
		}
		return sample;
	}
}
