// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

/**
 * This interface declares the contract for multiple oscillator unison.
 * @author st
 */
public interface UnisonVariables
{
	int getOscillatorCount(); 	// 1..
	float getPitchSpread();   	// 0..1
	float getPhaseSpread();		// 0..1
}
