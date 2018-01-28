// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.synths.multi;

import uk.org.toot.synth.MidiSynth;
import uk.org.toot.synth.SynthControls;
import uk.org.toot.synth.spi.SynthServiceProvider;

import static uk.org.toot.control.id.ProviderId.TOOT_PROVIDER_ID;

public class MultiSynthServiceProvider extends SynthServiceProvider
{
	public MultiSynthServiceProvider() {
		super(TOOT_PROVIDER_ID, "Toot Software", MultiSynthControls.NAME, "0.1");
		String name = MultiSynthControls.NAME;
		addControls(MultiSynthControls.class, MultiSynthControls.ID, name, "", "0.1");
		add(MultiMidiSynth.class, name, "", "0.1");
	}

	public MidiSynth createSynth(SynthControls c) {
		if ( c instanceof MultiSynthControls ) {
			return new MultiMidiSynth((MultiSynthControls)c);
		}
		return null;
	}

}
