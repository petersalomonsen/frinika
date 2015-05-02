// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.synth.modules.filter.FilterType.PEAK;

/**
 * A helper class for dual state variable configuration and real-time use.
 * package scope due to its iffyness
 * @author st
 */
class DualStateVariableFilterConfig
{
	private String name;
	
	public DualStateVariableFilterConfig(String name, int t1, int t2, int ratio) {
		this.name = name;
		assert t1 >= 0 && t1 <= PEAK;
		assert t2 >= 0 && t2 <= PEAK;
		assert ratio > 0 && ratio < 10;
		type1 = t1;
		type2 = t2;
		f2ratio = ratio;
	}
	
	// config use, final to prevent mutation
	public final int type1;		// FilterType
	public final int type2;		// FilterType
	public final int f2ratio;	// relative to f1, e.g. 1 or 2
	
	// real-time use
	public float freq1;
	public float freq2;
	public float damp1;
	public float damp2;
	
	public String toString() { return name; }
}
