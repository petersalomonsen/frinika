// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp;

/**
 * http://musicdsp.org/showArchiveComment.php?ArchiveID=9
 * A 2nd order harmonic oscillator, as described by RBJ
 * @author st
 */
public class Phasor
{
	private double y0, y1, y2;
	private double b1;
	
	/**
	 * @param w - the normalised angular frequency, 2*PI*f/sampleRate
	 * @param theta - the initial phase in radians
	 */
	public Phasor(double w, double theta) {
		b1 = 2f * Math.cos(w);
		y1 = Math.sin(theta - w);
		y2 = Math.sin(theta - 2 * w);
	}
	
	/**
	 * @return consecutive samples of the phasor projected onto the time axis
	 */
	public float out() {
		y0 = b1 * y1 - y2;
		y2 = y1;
		y1 = y0;
		return (float)y0;
	}
}
