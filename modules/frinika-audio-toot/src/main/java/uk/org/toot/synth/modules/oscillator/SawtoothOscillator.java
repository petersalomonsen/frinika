// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import uk.org.toot.synth.SynthChannel;

/**
 * A bandlimited sawtooth oscillator without PWM, sync
 * @author st
 *
 */
public class SawtoothOscillator
{
	private static MultiWave multiWave;
	
	private SynthChannel channel;
	private Wave wave;
	private int waveSize;
	private int waveIndex;			// index of the Wave in the MultiWave
	private float k;				// product of period in samples * frequency in Hz
	private float k2;				// frequency / increment
	private float increment = 1f;	// wave increment for the nominal pitch
	private float currentIncrement;
	private float index = 0f;		// index of the sample within the Wave
	private float frequency;		// nominal (start) frequency
	
	static {
		multiWave = MultiWaves.get("Square"); // yes, sawttoths are derived from squares
	}
	
	public SawtoothOscillator(
			SynthChannel channel, 
			float frequency) {
		this.channel = channel;
		this.frequency = frequency;
		waveIndex = multiWave.getIndex(frequency);
		wave = multiWave.getWave(waveIndex);
		waveSize = wave.getData().length - 1;
		k = wave.getPeriod() * frequency;
	}
	
	public void setSampleRate(int sampleRate) {
		increment = k / sampleRate;
		k2 = frequency / increment;
	}
	
	public void update() {
		currentIncrement = increment * channel.getBendFactor();
	}
	
	public float getSample(float mod) {
		float inc = currentIncrement * mod; 	// !!! 0 .. 2 instead of 0.5 .. 2 !!!
		float sample = wave.get(index);
		index += inc;
		if ( index >= waveSize ) {				// once per wave cycle
			index -= waveSize;					// glitches shifted sample!
			int wi = multiWave.getIndex(k2 * inc);
			if ( wi != waveIndex ) {
				wave = multiWave.getWave(wi);
				waveIndex = wi;
			}
		} 
		return sample;
	}
}
