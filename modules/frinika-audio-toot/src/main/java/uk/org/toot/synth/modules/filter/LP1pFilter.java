// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.audio.core.FloatDenormals.zeroDenorm;

/**
 * A simple single pole low pass filter with no changing parameters.
 * @author st
 *
 */
public class LP1pFilter
{
	private float y1 = 0f;
	private float g;
	
	public LP1pFilter(float freq, int rate) {
		g = 1f - (float)Math.exp(-2.0*Math.PI*freq/rate);
	}
	
	public float filter(float sample) {
		y1 = zeroDenorm(y1 + g*(sample - y1));
	    return y1;	
	}

}
