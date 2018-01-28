// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

/**
 * A Parabola wave is used to derive a wave variable between a triangle or sawtooth, 
 * depending on the variable width parameter.
 * @author st
 *
 */
public class ParabolaMultiWave extends MultiWave
{
	public ParabolaMultiWave(int size, float fNyquist) {
		super(size, fNyquist);
	}

	@Override
	public float getWidthStartFactor(float width) {
		// 0.5 rule change found by experiment, avoids start transient
		return width < 0.5f ? (1f - (width / 2)) : ((1f - width) / 2);
	}
	
	@Override
	public float getWidthScalar(float width) {
		return 1f / (8 * (width - width*width));
	}

	@Override
	protected int partial(float[] data, int length, int partial, int sign, float comp) {
		float amp = comp * 4 * sign / (partial * partial);
		for ( int i = 0; i < length; i++ ) {
			// note, parabola uses cos			
			data[i] += amp * sinetable[((i * partial) + (length / 4)) % length]; 
		}
		return -sign;
	}

	@Override
	protected void normalise(float[] data, float max) {
		max /= 2;
		for ( int i = 0; i < data.length; i++ ) {
			data[i] /= max;
			data[i] -= 1;
		}
	}
}
