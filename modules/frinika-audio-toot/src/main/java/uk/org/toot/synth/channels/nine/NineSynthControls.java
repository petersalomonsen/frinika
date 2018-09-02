// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.channels.nine;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.id.TootSynthControlsId.NINE_CHANNEL_ID;
import uk.org.toot.synth.SynthChannelControls;
import uk.org.toot.synth.modules.amplifier.AmplifierControls;
import uk.org.toot.synth.modules.amplifier.AmplifierVariables;
import uk.org.toot.synth.modules.oscillator.HammondOscillatorControls;
import uk.org.toot.synth.modules.oscillator.HammondOscillatorVariables;

/**
 * @author st
 *
 */
public class NineSynthControls extends SynthChannelControls
{
	public static String NAME = "Nine";
	
	// OFFSETS MUST NOT BE CHANGED TO PRESERVE PERSISTENCE PORTABILITY
	// OFFSETS ARE SLIGHTLY SPARSE TO ALLOW EXTENSION OF EXISTING MODULES
	private final static int OSC_OFFSET = 0x00;
	private final static int AMP_OFFSET = 0x38;

	private HammondOscillatorControls hammondControls;
	private AmplifierControls ampControls;
	
	public NineSynthControls() {
		super(NINE_CHANNEL_ID, NAME);
		ControlRow row = new ControlRow();
		row.add(hammondControls = new HammondOscillatorControls(0, getString("Level"), OSC_OFFSET));
		row.add(ampControls = new AmplifierControls(0, getString("Amplifier"), AMP_OFFSET, ""));
		add(row);
	}
	
	public HammondOscillatorVariables getHammondVariables() {
		return hammondControls;
	}
	
	public AmplifierVariables getAmplifierVariables() {
		return ampControls;
	}
}
