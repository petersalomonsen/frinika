// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

/**
 * @author st
 */
public interface DSFOscillatorVariables
{
	int getRatioNumerator();
	int getRatioDenominator();
	int getPartialCount();				// np
	float getPartialRolloffFactor();	// a
	int getPartialRolloffInt();			// a as raw int to simplify change detection
	boolean canUseWolfram();
}
