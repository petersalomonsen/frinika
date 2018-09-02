// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

/**
 * This class represents the wave for MultiWaveOscillator.
 * The data shiuld have the first sample repeated at the end to
 * allow for efficient linear interpolation.
 * @author st
 *
 */
public class SingleWave implements Wave
{
	private float[] data;
	private float period;

	public SingleWave(float[] data, float period) {
		this.data = data;
		this.period = period;
	}
	
	/**
	 * @return the data
	 */
	public float[] getData() {
		return data;
	}

	/**
	 * @return the period of the wave signal in samples, which may be less
	 * than the wave length for some waves.
	 */
	public float getPeriod() {
		return period;
	}

	/**
	 * @param index the floating point index
	 * @return a linearly interpolated sample of the wave
	 */
	public float get(float index) {
		int ix = (int)index;
		float frac = index - ix;
		return (1f - frac) * data[ix] + frac * data[ix+1];
	}
}
