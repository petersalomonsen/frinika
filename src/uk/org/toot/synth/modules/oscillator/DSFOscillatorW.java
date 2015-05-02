// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import uk.org.toot.dsp.Cosine;
import uk.org.toot.dsp.Sine;

/**
 * This class implements an Oscillator using the Discrete Summation Formula 
 * as detailed by Wolfram. Not that Wolfram discovered it but they were kind
 * enough to detail it on the web so that's where I found it.
 * http://functions.wolfram.com/ElementaryFunctions/Sin/23/01/0008/
 * @author st
 */
public class DSFOscillatorW implements DSFOscillator
{
	private double a;
	private Sine sine1, sine2, sine3;
	private Cosine cosine;
	private double aN;
	private int np;
	
	/**
	 * Create a Discrete Summation Formula Oscillator with a spectrum of
	 * a fundamental frequency and N partial frequencies with amplitude rolling 
	 * off with respect to the fundamental.
	 * 
	 * @param wn - fundamental normalised frequency
	 * @param wp - unused
	 * @param a - partial rolloff weight 0..1
	 * @param np - number of partials, 1..
	 */
	public DSFOscillatorW(double wn, double wp, int np, float a) {
		assert ( wn > 0f && wn < Math.PI ) : "wn="+wn;
		assert np > 0 : "np="+np;
		assert ( a >= 0 && a < 1f ) : "a="+a;
		this.np = np; 
		// ensure the highest partial is below nyquist
		if ( wn * np >= Math.PI ) np = (int)(Math.PI / wn);
		sine1 = new Sine(wn * (np+1));
		sine2 = new Sine(wn * np); 	
		sine3 = new Sine(wn); 			
		cosine = new Cosine(wn); 
		update(a);
	}
	
	public void update(float a) {
		this.a = a;
		aN = Math.pow(a, np); // !!! EXPENSIVE
	}
	
	public float getSample() {
		double denom = (1 - 2*a*cosine.out() + a*a);
		if ( denom == 0 ) return 0f; // ??
		return (float)((((a * sine2.out() - sine1.out()) * aN + sine3.out()) * a) / denom);
	}
}
