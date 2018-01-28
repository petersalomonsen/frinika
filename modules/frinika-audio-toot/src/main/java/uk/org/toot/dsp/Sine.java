// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp;

/**
 * @author st
 *
 */
public class Sine extends Phasor
{
	/**
	 * @param w - the normalised angular frequency, 2*PI*f/sampleRate
	 */
	public Sine(double w) {
		super(w, 0);
	}
}
