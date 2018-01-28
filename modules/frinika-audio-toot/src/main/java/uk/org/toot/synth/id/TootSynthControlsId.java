// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.id;

public interface TootSynthControlsId
{
	final static int MULTI_SYNTH_ID = 1;
	final static int SIX_STRING_GUITAR_PLUCKED_SYNTH = 2;
	final static int TWELVE_STRING_GUITAR_PLUCKED_SYNTH = 3;
	final static int FOUR_STRING_BASS_PLUCKED_SYNTH = 4;

	// a special case for wrapping VST instruments
	final static int VSTI_SYNTH_ID = 127;
	
	// ids of the plugin synth channels
	final static int VALOR_CHANNEL_ID = 2;
	final static int PLUCK_CHANNEL_ID = 3;
	final static int COPAL_CHANNEL_ID = 4;
	final static int WHIRL_CHANNEL_ID = 5;
	final static int TOTAL_CHANNEL_ID = 6;
	final static int NINE_CHANNEL_ID = 7;
}
