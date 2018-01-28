// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.synths.plucked;

import uk.org.toot.synth.MidiSynth;
import uk.org.toot.synth.SynthControls;
import uk.org.toot.synth.spi.SynthServiceProvider;

import static uk.org.toot.control.id.ProviderId.TOOT_PROVIDER_ID;

public class PluckedSynthServiceProvider extends SynthServiceProvider
{
	private	final static String description = "Plucked Synth";

	public PluckedSynthServiceProvider() {
		super(TOOT_PROVIDER_ID, "Toot Software", description, "0.1");
		addControls(
			SixStringGuitarControls.class, 
			SixStringGuitarControls.ID, 
			SixStringGuitarControls.NAME, 
			description, 
			"0.1");
		addControls(
			FourStringBassGuitarControls.class, 
			FourStringBassGuitarControls.ID, 
			FourStringBassGuitarControls.NAME, 
			description, 
			"0.1");
		add(PluckedSynth.class, "Plucked Synth", "", "0.1");
	}

	public MidiSynth createSynth(SynthControls c) {
		if ( c instanceof PluckedSynthControls ) {
			return new PluckedSynth((PluckedSynthControls)c);
		}
		return null;
	}

}
