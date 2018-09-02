// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

public interface DualMultiWaveOscillatorVariables
{
	float getSawLevel();
	float getSquareLevel();
//	int getOctave();
	float getWidth();			// 0 .. 1
	float getTuningFactor();	// 0.25 .. 4 
	boolean isMaster();
}
