// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.channels.nine;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.synth.PolyphonicSynthChannel;
import uk.org.toot.synth.modules.amplifier.AmplifierVariables;
import uk.org.toot.synth.modules.envelope.ASREnvelopeGenerator;
import uk.org.toot.synth.modules.envelope.ASREnvelopeVariables;
import uk.org.toot.synth.modules.oscillator.HammondOscillator;
import uk.org.toot.synth.modules.oscillator.HammondOscillatorVariables;

/**
 * A model of a Hammond Drawbar organ.
 * Called nine because, well, draw is slang for cannabis and a bar is 9 oz.
 * And typically there just happen to be 9 drawbars :)
 * @author st
 */
public class NineSynthChannel extends PolyphonicSynthChannel
{
	private HammondOscillatorVariables oscVars;
	private ASREnvelopeVariables envVars;
	private AmplifierVariables ampVars;
	
	public NineSynthChannel(NineSynthControls controls) {
		super(controls.getName());
		oscVars = controls.getHammondVariables();
		envVars = new ASREnvelopeVariables() {
			public float getAttackCoeff() {	return 0.05f; }
			public float getReleaseCoeff() { return 0.03f; }
			public boolean getSustain() { return true; }
			public void setSampleRate(int rate) {}			
		};
		ampVars = controls.getAmplifierVariables();
	}

	@Override
	protected Voice createVoice(int pitch, int velocity, int sampleRate) {
		return new NineVoice(pitch, velocity);
	}

	public class NineVoice extends AbstractVoice
	{
		private HammondOscillator osc;
		private ASREnvelopeGenerator env;
		private float ampLevel;

		public NineVoice(int pitch, int velocity) {
			super(pitch, velocity);
//			System.out.println(frequency);
			float wn = frequency * 2 * (float)Math.PI / sampleRate;
			float wmax = 5925 * 2 * (float)Math.PI / sampleRate;
			osc = new HammondOscillator(wn, wmax, oscVars.getLevels());
			env = new ASREnvelopeGenerator(envVars);
		}

		public void setSampleRate(int sr) {
			stop(); // can't change sample rate dynamically !!!
		}

		@Override
		public boolean mix(AudioBuffer buffer) {
			ampLevel = ampVars.getLevel();
			return super.mix(buffer);
		}
		
		@Override
		protected float getSample() {
			return osc.getSample() * ampLevel * env.getEnvelope(release);
		}

		@Override
		protected boolean isComplete() {
			return env.isComplete();
		}
	}
}
