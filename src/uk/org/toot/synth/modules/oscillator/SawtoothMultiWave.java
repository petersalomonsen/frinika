// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

/**
 * Somewhat paradoxically a sawtooth wave is used to derive a rectangular wave
 * variable between pulse and square, depending on the variable width parameter.
 * Also paradoxically, a variable width sawtooth/triangle is not derived from a sawtooth.
 * @author st
 *
 */
public class SawtoothMultiWave extends MultiWave
{
	public SawtoothMultiWave(int size, float fNyquist) {
		super(size, fNyquist);
	}

	@Override
	public float getWidthOffset(float width) {
		return 1f - 2 * width;
	}

	@Override
	protected int partial(float[] data, int length, int partial, int sign, float comp) {
		float amp = comp / partial;
		for ( int i = 0; i < length; i++ ) {
			data[i] += amp * sinetable[(i * partial) % length];			
		}
		return sign;
	}
}
