// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.distort;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;
import uk.org.toot.dsp.DCBlocker;
import uk.org.toot.dsp.filter.*;

import static uk.org.toot.dsp.FastMath.tanh;

/*
 * A distortion effect which uses oversampling to significantly reduce aliasing.
 */
public class Distort1Process extends SimpleAudioProcess
{
	private Variables vars;
	private OverSampler overSampler;
	private DCBlocker[] blocker;
	private int sampleRate = 44100;
    private boolean wasBypassed;
	
	public Distort1Process(Variables vars) {
		this.vars = vars;
        design();
	}

	private void design() {
		final int R = 4;				// oversample Rate
		
        FIRSpecification s = new FIRSpecification();
        s.f1 = 0; s.fN = R * sampleRate / 2; s.dBatten = 60;

        s.f2 = 9000; s.ft = 20000-s.f2;	s.mod = R; s.order = -1;	
		float[] ia = FIRDesignerPM.design(s); // KW 51 taps, PM 32 taps (7000)
		
		s.f2 = 14000; s.ft = 20000-s.f2; s.mod = 0; s.order = -1;
		float[] da = FIRDesignerPM.design(s); // KW 109 taps, PM 61 taps

//        System.out.println("Distort1Process: ia["+ia.length+"], da["+da.length+"]");

		overSampler = new FIROverSampler2(R, 2, ia, da); // !!! STEREO
        blocker = new DCBlocker[2];
		blocker[0] = new DCBlocker();
        blocker[1] = new DCBlocker();
	}
	
	/**
	 * Our rms 0dB is typically 0.1 so we apply gain before applying the function,
	 * which maxes out at output 1 for input 1. 
	 * Afterwards we divide by applied gain to get back to our nominal 0dB, allowing
	 * for the fact that heavy saturation means we don't get much louder beyond a
	 * certain point. 
	 */
	public int processAudio(AudioBuffer buffer) {
        boolean bypassed = vars.isBypassed();
        if ( bypassed ) {
            if ( !wasBypassed ) {
                overSampler.clear(); // clear oversampler histories
                wasBypassed = true;
            }
            return AUDIO_OK;
        }
        wasBypassed = bypassed;
		int srate = (int)buffer.getSampleRate();
		if ( srate != sampleRate ) {
			sampleRate = srate;
			design();
		}
        int nsamples = buffer.getSampleCount();
        int nchans = buffer.getChannelCount() > 1 ? 2 : 1; // only mono or stereo
        float bias = vars.getBias();
        float gain = vars.getGain();
        float inverseGain = 1f / gain;
        // attempt to maintain contant rms level as signal saturates
        if ( inverseGain < 0.1f ) inverseGain = 0.1f;
        float[] samples;
        float[] upSamples;
        float sample;
        DCBlocker dc;
        for ( int c = 0; c < nchans; c++ ) {
        	samples = buffer.getChannel(c);
            dc = blocker[c];
        	for ( int s = 0; s < nsamples; s++ ) {
        		upSamples = overSampler.interpolate(gain * samples[s], c);
        		for ( int i = 0; i < upSamples.length; i++ ) {
        			sample = tanh(bias + upSamples[i]);
        			sample += 0.1f * sample * sample; // 10% 2nd harmonic, like valves
        			upSamples[i] = sample;
        		}
        		samples[s] = inverseGain * dc.block(overSampler.decimate(upSamples, c));
        	}
        }
		return AUDIO_OK;
	}	
    
    public interface Variables
    {
        boolean isBypassed();
        float getGain();
        float getBias();
    }
}
