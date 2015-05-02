// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp.filter;

public abstract class OverSampler
{
	protected final int R;
	protected final int NC;
	
	/**
	 * @param rate oversampling rate 2..64
	 * @param nchans number of channels
	 */
	public OverSampler(int rate, int nchans) {
		assert rate > 1 && rate < 65;
		R = rate;
		NC = nchans;
	}
	
	abstract public float[] interpolate(float sample, int nchan);
	
	abstract public float decimate(float[] samples, int nchan);
    
    abstract public void clear();
}
