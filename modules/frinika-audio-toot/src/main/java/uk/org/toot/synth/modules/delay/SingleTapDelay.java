// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.delay;

import java.util.Arrays;

public class SingleTapDelay 
{
	private float[] delayLine;
	
	private int ntaps;
	private int wrpos;
	private int rdpos = 0;
	private int delaySamples;
	
	public SingleTapDelay(int ntaps) {
		delayLine = new float[ntaps];
		Arrays.fill(delayLine, 0);
		this.ntaps = ntaps;
		delaySamples = ntaps / 2;
		wrpos = delaySamples;
	}
	
	public float getSample(float in) {
		delayLine[wrpos] = in;
		wrpos += 1;
		wrpos %= ntaps;
		rdpos += 1;
		rdpos %= ntaps;
		return delayLine[rdpos];
	}
}
