// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

public interface FormantFilterVariables
{
	public int size();
	public float getFrequency(int n);	// 0..1
	public float getLevel(int n);		// 0..1
	public float getFreqencyShift();	// 0.25..4
	public float getResonance();		// 0..1
	public void setSampleRate(int rate);
	public boolean isBypassed();
}
