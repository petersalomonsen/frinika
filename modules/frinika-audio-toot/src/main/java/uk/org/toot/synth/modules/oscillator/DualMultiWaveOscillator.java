// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import uk.org.toot.synth.SynthChannel;

/**
 * A different take on MultiWaveOScillator
 * Derive sawtooth and PWM square at the same time from just 2 lookups
 * No envelopes or lfos internally
 * 
 * @author st
 *
 */
public class DualMultiWaveOscillator
{
	private SynthChannel channel;
	private DualMultiWaveOscillatorVariables vars;
	private MultiWave sqrMultiWave;
	private Wave sqrWave;
	private int waveSize;
	private int waveIndex = -1;		// index of the Wave in the MultiWave
//	private float k2;				// frequency / increment
	private float currentIncrement;
	private boolean master;
	private float index = 0;		// index of the sample within the Wave
	private float sqrScalar;
	private float sqrOffset;
	private float width;
	private float sawLevel;
	private float sqrLevel;
	private int sampleRate = 44100;
	
	public DualMultiWaveOscillator(
			SynthChannel channel, 
			DualMultiWaveOscillatorVariables oscillatorVariables) {
		this.channel = channel;
		vars = oscillatorVariables;
		master = vars.isMaster();
		sqrMultiWave = MultiWaves.get("Square");
		sqrWave = sqrMultiWave.getWave(42);
		waveSize = sqrWave.getData().length - 1;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}
	
	public void update(float frequency) {	
		int wi = sqrMultiWave.getIndex(frequency);
		if ( wi != waveIndex ) {
			sqrWave = sqrMultiWave.getWave(wi);
			waveIndex = wi;
		}
//		index = waveSize * sqrMultiWave.getWidthStartFactor(vars.getWidth());		
		float k = sqrWave.getPeriod() * frequency;
		float increment = k / sampleRate;
//		k2 = frequency / increment;
		currentIncrement = increment * channel.getBendFactor();
		if ( !master ) currentIncrement *= vars.getTuningFactor();
		width = vars.getWidth();
		sqrScalar = sqrMultiWave.getWidthScalar(width);
		sqrOffset = sqrMultiWave.getWidthOffset(width);
		sqrLevel = vars.getSquareLevel();
		sawLevel = vars.getSawLevel();
	}
	
	public float getSample(float mod, float wmod, OscillatorControl control) {
		float inc = currentIncrement * mod; 	// !!! 0 .. 2 instead of 0.5 .. 2 !!!
		if ( !master ) {
			if ( control.sync ) index = 0; 		// hard sync - aliases?
		}
		float sawSample = sqrWave.get(index);
		float w = width + wmod;
		if ( w > 0.99f ) w = 0.99f;
		else if ( w < 0.01f ) w = 0.01f;
		float ixShift = index + waveSize * w;
		if ( ixShift >= waveSize ) ixShift -= waveSize;
		float sqrSample = sawSample - sqrWave.get(ixShift);
		index += inc;
		if ( index >= waveSize ) {				// once per wave cycle
			index -= waveSize;
/*			int wi = sqrMultiWave.getIndex(k2 * inc);
			if ( wi != waveIndex ) {
				sqrWave = sqrMultiWave.getWave(wi);
				waveIndex = wi;
			} */
			if ( master ) control.sync = true;
		} 
		return sawSample * sawLevel + 
			   (sqrSample * sqrScalar + sqrOffset) * sqrLevel;
	}
}
