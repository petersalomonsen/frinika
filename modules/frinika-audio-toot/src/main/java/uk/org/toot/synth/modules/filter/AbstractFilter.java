// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

public abstract class AbstractFilter
{
	protected FilterVariables vars;
	protected float fs = 44100;
	
	public AbstractFilter(FilterVariables filterVariables) {
		vars = filterVariables;
	}
	
	public void setSampleRate(int rate) {
		vars.setSampleRate(rate);
		fs = rate;
	}
}
