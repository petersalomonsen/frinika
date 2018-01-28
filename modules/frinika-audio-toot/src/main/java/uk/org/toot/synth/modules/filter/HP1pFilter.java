// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

public class HP1pFilter extends LP1pFilter
{
	public HP1pFilter(float freq, int rate) {
		super(freq, rate);
	}
	
	public float filter(float sample) {
		return sample - super.filter(sample);
	}

}
