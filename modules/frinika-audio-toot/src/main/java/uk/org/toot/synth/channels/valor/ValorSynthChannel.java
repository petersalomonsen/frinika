package uk.org.toot.synth.channels.valor;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.midi.misc.Controller;
import uk.org.toot.synth.PolyphonicSynthChannel;
//import uk.org.toot.midi.synth.delay.SingleTapDelay;
import uk.org.toot.synth.modules.amplifier.AmplifierVariables;
import uk.org.toot.synth.modules.envelope.*;
import uk.org.toot.synth.modules.filter.*;
import uk.org.toot.synth.modules.mixer.MixerVariables;
import uk.org.toot.synth.modules.mixer.ModulationMixerVariables;
import uk.org.toot.synth.modules.oscillator.*;

/**
 * 3 Band Limited Oscillators
 * 		continously variable width between Pulse/Square or Saw/Triangle
 * 3 LFOs, Sine/Triangle, one for Vibrato, two for general modulation
 * 3 AHDSR Envelopes, one amplifier, two general modulation
 * Moog 24dB/octave Low Pass Filter
 * Oberheim SEM 12dB/octave Multimode Filter
 * 
 * @author st
 */
public class ValorSynthChannel extends PolyphonicSynthChannel
{
	private MultiWaveOscillatorVariables oscillator1Vars;
	private MultiWaveOscillatorVariables oscillator2Vars;
	private MultiWaveOscillatorVariables oscillator3Vars;
	private EnvelopeVariables envelopeAVars;
	private EnvelopeVariables envelope1Vars;
	private EnvelopeVariables envelope2Vars;
	private EnvelopeVariables envelopeVVars;
	private FilterVariables lpFilterVars;
	private StateVariableFilterVariables svFilterVars;
	private AmplifierVariables amplifierVars;
	private LFOVariables vibratoVars;
	private LFOVariables lfo1Vars;
	private LFOVariables lfo2Vars;
	private MixerVariables lpFilterMixerVars;
	private MixerVariables svFilterMixerVars;
	private ModulationMixerVariables osc1WidthModMixer;
	private ModulationMixerVariables osc2WidthModMixer;
	private ModulationMixerVariables osc3WidthModMixer;
	private ModulationMixerVariables lpfCutoffModMixer;
	private ModulationMixerVariables svfCutoffModMixer;
	private ModulationMixerVariables vibModMixer;
	
	static {
		MultiWaves.init();
	}
	
	public ValorSynthChannel(ValorSynthControls controls) {
		super(controls.getName());
		oscillator1Vars = controls.getOscillatorVariables(1-1);
		oscillator2Vars = controls.getOscillatorVariables(2-1);
		oscillator3Vars = controls.getOscillatorVariables(3-1);
		envelopeAVars = controls.getEnvelopeVariables(0);
		envelope1Vars = controls.getEnvelopeVariables(1);
		envelope2Vars = controls.getEnvelopeVariables(2);
		envelopeVVars = controls.getEnvelopeVariables(3);
		lpFilterVars = controls.getFilterVariables(0);
		svFilterVars = (StateVariableFilterVariables)controls.getFilterVariables(1);
		amplifierVars = controls.getAmplifierVariables();
		vibratoVars = controls.getLFOVariables(0);
		lfo1Vars = controls.getLFOVariables(1);
		lfo2Vars = controls.getLFOVariables(2);
		lpFilterMixerVars = controls.getMixerVariables(0);
		svFilterMixerVars = controls.getMixerVariables(1);
		osc1WidthModMixer = controls.getModulationMixerVariables(0);
		osc2WidthModMixer = controls.getModulationMixerVariables(1);
		osc3WidthModMixer = controls.getModulationMixerVariables(2);
		lpfCutoffModMixer = controls.getModulationMixerVariables(3);
		svfCutoffModMixer = controls.getModulationMixerVariables(4);
		vibModMixer = controls.getModulationMixerVariables(5);
		createVoice(42, 0, 44100);
	}

	protected void setSampleRate(int rate) {
		super.setSampleRate(rate);
		envelopeAVars.setSampleRate(rate);
		envelope1Vars.setSampleRate(rate);
		envelope2Vars.setSampleRate(rate);
		envelopeVVars.setSampleRate(rate);
		lpFilterVars.setSampleRate(rate);
		svFilterVars.setSampleRate(rate);
	}
	
	@Override
	protected Voice createVoice(int pitch, int velocity, int sampleRate) {
		return new Example2Voice(pitch, velocity, sampleRate);
	}

	public class Example2Voice extends AbstractVoice
	{
		private MultiWaveOscillator oscillator1;
		private MultiWaveOscillator oscillator2;
		private MultiWaveOscillator oscillator3;
		private MoogFilter2 lpFilter;
		private StateVariableFilter svFilter;
		private EnvelopeGenerator envelopeA;
		private EnvelopeGenerator envelope1;
		private EnvelopeGenerator envelope2;
		private EnvelopeGenerator envelopeVib;
		private LFO lfoVib;
		private LFO lfo1;
		private LFO lfo2;
		private OscillatorControl oscControl;
		private float ampT; // amp tracking factor
		private float ampLevel;
		private float lpfOsc1Level;
		private float lpfOsc2Level;
		private float lpfOsc3Level;
		private float svfOsc1Level;
		private float svfOsc2Level;
		private float svfOsc3Level;
		
		private boolean lpfEnabled;
		private boolean svfEnabled;
		private boolean osc2Enabled;
		private boolean osc3Enabled;
		
		private float[] modSamples = new float[8];
		private float[] osc1WidthModDepths;
		private float[] osc2WidthModDepths;
		private float[] osc3WidthModDepths;
		private float[] lpfCutoffModDepths;
		private float[] svfCutoffModDepths;
		private float[] vibModDepths;
		
		private float vibModPre;
		private float fsvstatic = 0f;
		private float flpstatic = 0f;
		
		public Example2Voice(int pitch, int velocity, int sampleRate) {
			super(pitch, velocity);
			lfo1 = new LFO(lfo1Vars, (float)(-Math.PI / 2)); // start at minimum
			lfo2 = new LFO(lfo2Vars, (float)(-Math.PI / 2)); // start at minimum
			oscillator1 = new MultiWaveOscillator(ValorSynthChannel.this, oscillator1Vars, frequency);
			oscillator2 = new MultiWaveOscillator(ValorSynthChannel.this, oscillator2Vars, frequency);
			oscillator3 = new MultiWaveOscillator(ValorSynthChannel.this, oscillator3Vars, frequency);
			envelopeA = new EnvelopeGenerator(envelopeAVars);
			envelopeA.trigger();
			envelope1 = new EnvelopeGenerator(envelope1Vars);
			envelope1.trigger();
			envelope2 = new EnvelopeGenerator(envelope2Vars);
			envelope2.trigger();
			lfoVib = new LFO(vibratoVars);
			envelopeVib = new EnvelopeGenerator(envelopeVVars);
			envelopeVib.trigger();
			lpFilter = new MoogFilter2(lpFilterVars);
			svFilter = new StateVariableFilter(svFilterVars);
			oscControl = new OscillatorControl();
//			delay = new SingleTapDelay(4410);
			float ampTracking = amplifierVars.getVelocityTrack();
			ampT = velocity == 0 ? 0f : (1 - ampTracking * (1 - amplitude));
			setSampleRate(sampleRate);
		}

		public void setSampleRate(int rate) {
			oscillator1.setSampleRate(rate);
			oscillator2.setSampleRate(rate);
			oscillator3.setSampleRate(rate);
			lfoVib.setSampleRate(rate);
			lfo1.setSampleRate(rate);
			lfo2.setSampleRate(rate);
		}
		
		public boolean mix(AudioBuffer buffer) {
			lfoVib.update();
			lfo1.update();
			lfo2.update();
			oscillator1.update();
			lpfOsc1Level = lpFilterMixerVars.getLevel(0);
			lpfOsc2Level = lpFilterMixerVars.getLevel(1);
			lpfOsc3Level = lpFilterMixerVars.getLevel(2);
			svfOsc1Level = svFilterMixerVars.getLevel(0);
			svfOsc2Level = svFilterMixerVars.getLevel(1);
			svfOsc3Level = svFilterMixerVars.getLevel(2);
			osc2Enabled = lpfOsc2Level + svfOsc2Level > 0.01f;
			if ( osc2Enabled ) oscillator2.update();
			osc3Enabled = lpfOsc3Level + svfOsc3Level > 0.01f;
			if ( osc3Enabled ) oscillator3.update();
			osc1WidthModDepths = osc1WidthModMixer.getDepths();
			osc2WidthModDepths = osc2WidthModMixer.getDepths();
			osc3WidthModDepths = osc3WidthModMixer.getDepths();
			lpfCutoffModDepths = lpfCutoffModMixer.getDepths();
			svfCutoffModDepths = svfCutoffModMixer.getDepths();
			vibModDepths = vibModMixer.getDepths();
			// Vel, AT and Wheel mod per buffer
			modSamples[4] = amplitude;
			modSamples[5] = getChannelPressure() / 128;
			modSamples[6] = getController(Controller.MODULATION) / 128;
			vibModPre = modSamples[5] * vibModDepths[1] +						// AT
						modSamples[6] * vibModDepths[2];						// Wheel
			lpfEnabled = lpfOsc1Level + lpfOsc2Level + lpfOsc3Level > 0.01f;   
			if ( lpfEnabled ) {
				flpstatic = modulation(4, 3, lpfCutoffModDepths);
				flpstatic += lpFilter.update();
			}
			svfEnabled = svfOsc1Level + svfOsc2Level + svfOsc3Level > 0.01f;
			if ( svfEnabled ) { 
				fsvstatic = modulation(4, 3, svfCutoffModDepths);
				fsvstatic += svFilter.update();
			}
			ampLevel = amplifierVars.getLevel() * ampT;
			return super.mix(buffer);
		}
		
		protected float getSample() {
			float sample = 0f;
			float s2 = 0f;
			float s3 = 0f;
			// modulation sources
			modSamples[0] = (1f + lfo1.getSample()) / 2;
			modSamples[1] = (1f + lfo2.getSample()) / 2;
			modSamples[2] = envelope1.getEnvelope(release);
			modSamples[3] = envelope2.getEnvelope(release);
			float vibMod = vibModPre + envelopeVib.getEnvelope(release) * vibModDepths[0];							
			vibMod *= lfoVib.getSample() / 50;  				// 2% freq change max
			vibMod += 1f;										// offset for oscillators
			// oscillators
			float s1 = oscillator1.getSample(vibMod, modulation(0, 2, osc1WidthModDepths), oscControl);
			if ( osc2Enabled ) {
				s2 = oscillator2.getSample(vibMod, modulation(0, 2, osc2WidthModDepths), oscControl);
			}
			if ( osc3Enabled ) {
				s3 = oscillator3.getSample(vibMod, modulation(0, 2, osc3WidthModDepths), oscControl);
			}
			oscControl.sync = false; // clear sync for next iteration
			// filters
			if ( lpfEnabled ) {
				sample = s1 * lpfOsc1Level + s2 * lpfOsc2Level + s3 * lpfOsc3Level;
				float f = pitch + modulation(0, 4, lpfCutoffModDepths) + flpstatic;
				sample = lpFilter.filter(sample, midiFreq(f) * inverseNyquist);
			}
			if ( svfEnabled ) {
				float 
				sample2 = s1 * svfOsc1Level + s2 * svfOsc2Level + s3 * svfOsc3Level;
				float f = pitch + modulation(0, 4, svfCutoffModDepths) + fsvstatic;
				sample += svFilter.filter(sample2, midiFreq(f) * inverseNyquist);
			}
			// amplifier
			return sample * ampLevel * envelopeA.getEnvelope(release);				   
		}

		protected float modulation(int start, int len, float[] depths) {
			float sample = 0f;
			for ( int i = start; i < start + len; i++ ) {
				sample += modSamples[i] * depths[i];
			}
			return sample;
		}
		
		protected boolean isComplete() {
			return envelopeA.isComplete();
		}
	}
}