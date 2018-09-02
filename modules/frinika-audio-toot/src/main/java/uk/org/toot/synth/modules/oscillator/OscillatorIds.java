// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

public class OscillatorIds 
{
	final static int OSCILLATOR_BASE_ID = 0x00; // 32 oscillators

    final static int WAVE_OSCILLATOR_ID = OSCILLATOR_BASE_ID + 1;
    final static int MULTI_WAVE_OSCILLATOR_ID = OSCILLATOR_BASE_ID + 2;
    final static int LFO_ID = OSCILLATOR_BASE_ID + 3;
    final static int ENHANCED_LFO_ID = OSCILLATOR_BASE_ID + 4;
    final static int DUAL_MULTI_WAVE_OSCILLATOR_ID = OSCILLATOR_BASE_ID + 5;
    final static int DSF_OSCILLATOR_ID = OSCILLATOR_BASE_ID + 6;
    final static int HAMMOND_OSCILLATOR_ID = OSCILLATOR_BASE_ID + 7;
    
    final static int UNISON_ID = 31;
}
