// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

public class MoogFilter2 extends AbstractFilter
{
	private MoogFilterElement element;
	private float res;

	public MoogFilter2(FilterVariables variables) {
		super(variables);
		element = new MoogFilterElement();
	}
	
	public float update() {
		res = vars.getResonance();
		return vars.getCutoff();
	}

	public float filter(float sample, float f) {
		if ( f > 1f ) f = 1f;
		return element.filter(sample, f, res);
	}
}
