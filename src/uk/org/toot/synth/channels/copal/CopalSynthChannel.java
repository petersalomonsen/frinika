package uk.org.toot.synth.channels.copal;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.synth.ParaphonicSynthChannel;
import uk.org.toot.synth.modules.amplifier.AmplifierVariables;
import uk.org.toot.synth.modules.envelope.ASREnvelopeGenerator;
import uk.org.toot.synth.modules.envelope.ASREnvelopeVariables;
import uk.org.toot.synth.modules.filter.FormantFilter;
import uk.org.toot.synth.modules.filter.FormantFilterVariables;
import uk.org.toot.synth.modules.filter.HP1pFilter;
import uk.org.toot.synth.modules.filter.LP1pFilter;
import uk.org.toot.synth.modules.filter.LP1pHP1pVariables;
import uk.org.toot.synth.modules.mixer.MixerVariables;
import uk.org.toot.synth.modules.oscillator.MultiWaves;
import uk.org.toot.synth.modules.oscillator.SawtoothOscillator;

//import uk.org.toot.audio.core.FloatDenormals;

/*
 * A string synth.
 * 8', 4' and 2' sawtooths are filtered with key tracking high pass and low pass filters 
 * before being mixed together.
 * 
 * Traditionally chorus would be applied to the output.
 * In the toot model, synths do not have effects, effects may be applied at the mixer
 * and are more reusable as a result.
 */
public class CopalSynthChannel extends ParaphonicSynthChannel
{
	private LP1pHP1pVariables lphpVars;
	private MixerVariables mixVars;
	private ASREnvelopeVariables envVars;
	private AmplifierVariables ampVars;
	private FormantFilterVariables formantVars;
	
	private FormantFilter formantFilter;
	
	public CopalSynthChannel(CopalSynthControls controls) {
		super(controls.getName());
		lphpVars = controls.getLPHPVariables();
		mixVars = controls.getMixerVariables();
		envVars = controls.getEnvelopeVariables();
		ampVars = controls.getAmplifierVariables();
		formantVars = controls.getFormantFilterVariables();
		formantFilter = new FormantFilter(formantVars);
		MultiWaves.get("Square");
	}

	@Override
	protected int postProcessAudio(AudioBuffer buffer, int ret) {
		if ( formantVars.isBypassed() ) return 0;
		if ( ret == AudioProcess.AUDIO_SILENCE ) return 0; // !!!
		formantFilter.update();
		float[] samples = buffer.getChannel(0);
		// to help prevent the formant filter descending into denormals internally
//		samples[0] += FloatDenormals.THRESHOLD;
		int nsamples = buffer.getSampleCount();
		for ( int i = 0; i < nsamples; i++ ) {
			samples[i] = formantFilter.filter(samples[i]);
		}
		// to avoid 100% output denormals :(
//		FloatDenormals.zeroDenorms(samples, nsamples);
		return 0;
		
	}

	@Override
	protected void setSampleRate(int rate) {
		super.setSampleRate(rate);
		envVars.setSampleRate(rate);
		formantVars.setSampleRate(rate);
	}

	@Override
	protected Voice createVoice(int pitch, int velocity, int sampleRate) {
		return new CepalVoice(pitch, velocity, sampleRate);
	}

	protected class CepalVoice extends AbstractVoice
	{
		private SawtoothOscillator osc8, osc4, osc2;
		private LP1pFilter lp8, lp4, lp2;
		private HP1pFilter hp8, hp4, hp2;
		private ASREnvelopeGenerator envelope;
		private float osc8Level, osc4Level, osc2Level;
		private float ampT; // amp tracking factor
		private float ampLevel;
		
		public CepalVoice(int pitch, int velocity, int sampleRate) {
			super(pitch, velocity);
			osc8 = new SawtoothOscillator(CopalSynthChannel.this, frequency);   // 8'
			osc4 = new SawtoothOscillator(CopalSynthChannel.this, frequency*2);	// 4'
			osc2 = new SawtoothOscillator(CopalSynthChannel.this, frequency*4);	// 2'
			float lpRatio = lphpVars.getLowPassRatio();
			float hpRatio = lphpVars.getHighPassRatio();
			lp8 = new LP1pFilter(frequency * lpRatio, sampleRate);
			lp4 = new LP1pFilter(frequency * 2 * lpRatio, sampleRate);
			lp2 = new LP1pFilter(frequency * 4 * lpRatio, sampleRate);
			hp8 = new HP1pFilter(frequency * hpRatio, sampleRate);
			hp4 = new HP1pFilter(frequency * 2 * hpRatio, sampleRate);
			hp2 = new HP1pFilter(frequency * 4 * hpRatio, sampleRate);
			envelope = new ASREnvelopeGenerator(envVars);
			float ampTracking = ampVars.getVelocityTrack();
			ampT = velocity == 0 ? 0f : (1 - ampTracking * (1 - amplitude));
			setSampleRate(sampleRate);
		}

		@Override
		public boolean mix(AudioBuffer buffer) {
			ampLevel = ampVars.getLevel() * ampT;
			osc8.update();
			osc4.update();
			osc2.update();
			osc8Level = mixVars.getLevel(2);
			osc4Level = mixVars.getLevel(1);
			osc2Level = mixVars.getLevel(0);
			return super.mix(buffer);
		}
		
		protected float getSample() {
			float vibrato = 1f; // + vibratoLFO.getSample() / 50; // 2% freq change max
			float sample = 
				hp8.filter(lp8.filter(osc8.getSample(vibrato))) * osc8Level +
				hp4.filter(lp4.filter(osc4.getSample(vibrato))) * osc4Level +
				hp2.filter(lp2.filter(osc2.getSample(vibrato))) * osc2Level;
			return sample * ampLevel * envelope.getEnvelope(release);
		}

		protected boolean isComplete() {
			return envelope.isComplete();
		}

		public void setSampleRate(int sr) {
			osc8.setSampleRate(sr);
			osc4.setSampleRate(sr);
			osc2.setSampleRate(sr);
		}
		
	}
}
