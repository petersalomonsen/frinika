// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import uk.org.toot.synth.SynthChannel;

public class MultiWaveOscillator
{
	private SynthChannel channel;
	private MultiWaveOscillatorVariables vars;
	private boolean master;
	private MultiWave multiWave;
	private Wave wave;
	private int waveSize;
	private int waveIndex;			// index of the Wave in the MultiWave
	private float k;				// product of period in samples * frequency in Hz
	private float k2;				// frequency / increment
	private float increment = 1f;	// wave increment for the nominal pitch
	private float currentIncrement;
	private boolean sync;
	private float index = 0f;		// index of the sample within the Wave
	private float scalar = 1f;
	private float offset = 0f;
	private float frequency;		// nominal (start) frequency
	private float width;
	
	public MultiWaveOscillator(
			SynthChannel channel, 
			MultiWaveOscillatorVariables oscillatorVariables, 
			float frequency) {
		this.channel = channel;
		vars = oscillatorVariables;
		master = vars.isMaster();
		multiWave = vars.getMultiWave();
		int octave = vars.getOctave();
		switch ( octave ) {
		case -2: frequency /= 4; break;
		case -1: frequency /= 2; break;
		case +1: frequency *= 2; break;
		case +2: frequency *= 4; break;
		}
		this.frequency = frequency;
		waveIndex = multiWave.getIndex(frequency);
		wave = multiWave.getWave(waveIndex);
		waveSize = wave.getData().length - 1;
		k = wave.getPeriod() * frequency;
		index = waveSize * multiWave.getWidthStartFactor(vars.getWidth());
	}
	
	public void setSampleRate(int sampleRate) {
		increment = k / sampleRate;
		k2 = frequency / increment;
	}
	
	public void update() {
		currentIncrement = increment * channel.getBendFactor() * vars.getDetuneFactor();
		sync = !master;
		width = vars.getWidth();
		scalar = multiWave.getWidthScalar(width);
		offset = multiWave.getWidthOffset(width);
	}
	
	public float getSample(float mod, float wmod, OscillatorControl control) {
		float inc = currentIncrement * mod; 	// !!! 0 .. 2 instead of 0.5 .. 2 !!!
		if ( sync ) {
			if ( control.sync ) index = 0; 		// hard sync - aliases
		}
		float sample = wave.get(index);
		float w = width + wmod;
		if ( w > 0.99f ) w = 0.99f;
		else if ( w < 0.01f ) w = 0.01f;		// TODO wrap instead of clamp?
		float ixShift = index + waveSize * w;
		if ( ixShift >= waveSize ) ixShift -= waveSize;
		sample -= wave.get(ixShift);  			// inverted phase shifted for PWM etc.
		index += inc;
		if ( index >= waveSize ) {				// once per wave cycle
			index -= waveSize;					// glitches shifted sample!
			int wi = multiWave.getIndex(k2 * inc);
			if ( wi != waveIndex ) {
				wave = multiWave.getWave(wi);
				waveIndex = wi;
			}
			if ( master ) control.sync = true;
		} 
		return sample * scalar + offset;
	}
}
