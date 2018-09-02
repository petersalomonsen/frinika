// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

public interface Wave 
{
	/**
	 * @return the data
	 */
	float[] getData();

	/**
	 * @return the period of the wave signal in samples, which may be less
	 * than the wave length for some waves.
	 */
	float getPeriod();

	/**
	 * @param index the floating point index
	 * @return a linearly interpolated sample of the wave
	 */
	float get(float index);
}
