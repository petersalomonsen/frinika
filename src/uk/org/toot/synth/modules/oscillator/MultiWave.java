// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import uk.org.toot.synth.SynthChannel;

/**
 * This class represents the waves for MultiWaveOScillator.
 * @author st
 *
 */
public abstract class MultiWave
{
	protected Wave[] wave = new Wave[128];
	private static byte[] index; 
	protected float[] sinetable;

	public MultiWave(int size, float fNyquist) {
		if ( index == null ) createIndex();
		createWaves(size, fNyquist);
	}
	
	protected void createIndex() {
		index = new byte[25088];
		int prevIx = 0;
		for ( int n = 0; n < 128; n++ ) { // each midi note
			float f = SynthChannel.midiFreq(n); // !!! could use a better home TODO
			int ix = (int)(2 * f);
			for ( int j = prevIx; j <= ix; j++) {
				index[j] = (byte)n;
			}
			prevIx = ix + 1;
		}
	}
	
	protected void createWaves(int size, float fNyquist) {
		sinetable = new float[size];
		for ( int i = 0; i < size; i++) {
			sinetable[i] = (float)Math.sin(2 * Math.PI * i / size);
		}
		float max = 0;
		for ( int n = 0; n < 128; n++ ) { // each midi note
			float[] data = new float[size+1];
			// 8.175799Hz to 12543.854Hz
			float f = SynthChannel.midiFreq(n); // !!! could use a better home TODO
			int npartials = (int)(fNyquist / f);
			int sign = 1;
			float comp = 0;
			for ( int p = 0; p < npartials; p++ ) { // each partial
				// compensation for Gibbs Phenomemon
				comp = (float)Math.cos(p * Math.PI / 2 / npartials);
				comp *= comp;
				// add compensated partial
				sign = partial(data, data.length-1, p+1, sign, comp);
				data[data.length-1] = data[0]; // for simple linear interpolation
			}
			if ( n == 0 ) max = getMax(data);
			normalise(data, max);
			wave[n] = new SingleWave(data, data.length-1);
		}
		sinetable = null;
	}

	protected abstract int partial(float[] data, int length, int partial, int sign, float comp);
	
	protected float getMax(float[] data) {
		float max = 0f;
		for ( int i = 0; i < data.length; i++ ) {
			float abs = Math.abs(data[i]); 
			if ( abs > max ) max = abs; 
		}
		return max;
	}
	
	protected void normalise(float[] data, float max) {
		for ( int i = 0; i < data.length; i++ ) {
			data[i] /= max;
		}
	}
	
	// 8.175799Hz to 12543.854Hz
	public int getIndex(float freq) {
		if ( freq > 12544 ) return 127;
		return index[(int)(freq+freq)];
	}
	
	public Wave getWave(int n) {
		return wave[n];
	}
	
	public float getWidthStartFactor(float width) {
		return 0;
	}
	
	public float getWidthScalar(float width) {
		return 1f;
	}
	
	public float getWidthOffset(float width) {
		return 0f;
	}
}
