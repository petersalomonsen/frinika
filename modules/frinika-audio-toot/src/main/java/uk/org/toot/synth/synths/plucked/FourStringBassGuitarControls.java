// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.synths.plucked;

import static uk.org.toot.synth.id.TootSynthControlsId.FOUR_STRING_BASS_PLUCKED_SYNTH;

public class FourStringBassGuitarControls extends PluckedSynthControls
{
	public final static int ID = FOUR_STRING_BASS_PLUCKED_SYNTH;
	public final static String NAME = "4 String Bass";
	
	public FourStringBassGuitarControls() {
		super(ID, NAME, 4);
		// TODO Auto-generated constructor stub
	}

	@Override
	public float getLowestFrequency(int string) {
		switch ( string ) {
		case 0: return 41.2f;
		case 1: return 55.0f;
		case 2: return 73.4f;
		case 3: return 98.0f;
		}
		return 0;
	}

}
