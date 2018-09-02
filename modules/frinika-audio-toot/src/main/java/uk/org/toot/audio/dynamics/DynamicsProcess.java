// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

import static uk.org.toot.dsp.FastMath.*;

abstract public class DynamicsProcess extends SimpleAudioProcess
{
    protected float envelope = 0f;

    protected boolean isPeak = false;
    protected float threshold;
    protected float attack, release;
    protected float makeupGain, dryGain;

    protected DynamicsVariables vars;

    private boolean wasBypassed;

    private int sampleRate = 0;

    private int NSQUARESUMS = 10;
    private float[] squaresums = new float[NSQUARESUMS];
    private int nsqsum = 0;

    private float[][] samples = new float[6][];
    private float[][] tapSamples = new float[6][];
	private float[][] keySamples;
	

    public DynamicsProcess(DynamicsVariables vars) {
        this(vars, false);
    }

    public DynamicsProcess(DynamicsVariables vars, boolean peak) {
        this.vars = vars;
        this.isPeak = peak;
        wasBypassed = !vars.isBypassed(); // force update
    }

    public void clear() {
        envelope = 1f; // envelope of gain
       	vars.setDynamicGain(1f);
    }

    /**
     * Called once per AudioBuffer
     */
    protected void cacheProcessVariables() {
        // update local variables
        threshold = vars.getThreshold(); // all subclasses use this
        attack = vars.getAttack();
        release = vars.getRelease();
        makeupGain = vars.getGain();
        dryGain = vars.getDryGain();
    }

    /**
     * Called once per AudioBuffer
     */
	public int processAudio(AudioBuffer buffer) {
        boolean bypassed = vars.isBypassed();
        if ( bypassed ) {
            if ( !wasBypassed ) {
                clear();
            }
            wasBypassed = true;
            return AUDIO_OK;
        }
        wasBypassed = bypassed;
        int sr = (int)buffer.getSampleRate();
        if ( sr != sampleRate ) {
        	sampleRate = sr;
        	vars.update(sr); // rederives attack, release
        }
        cacheProcessVariables();
        float targetGain = 1f; // unity
        float gain = makeupGain; // keeps compiler happy

        int len = buffer.getSampleCount();
        int mslen = (int)(buffer.getSampleRate() / 1000);
        
        int nc = buffer.getChannelCount();
		for ( int c = 0; c < nc; c++ ) {
			samples[c] = buffer.getChannel(c);
		}

		int nck;
		AudioBuffer keyBuffer = vars.getKeyBuffer();
		if ( keyBuffer == null ) {
			keySamples = samples;
			nck = nc;
		} else {
			nck = keyBuffer.getChannelCount();
			for ( int c = 0; c < nck; c++ ) {
				tapSamples[c] = keyBuffer.getChannel(c);
			}
			keySamples = tapSamples;
		}
		
        float sample;
        for ( int i = 0; i < len; i++ ) {
        	float key = 0;
        	if ( isPeak ) {
        		for ( int c = 0; c < nck; c++ ) {
                    key = max(key, abs(keySamples[c][i]));
        		}
        		targetGain = function(key);
        	} else if ( (i % mslen) == 0 ) {
                int length = (i + mslen) > len ? len - i : mslen; 
        		// the rms side chain calculations, every millisecond
        		float sumOfSquares = 0f;
        		for ( int c = 0; c < nck; c++ ) {
        			for ( int j = 0; j < length; j++ ) {
        				sample = keySamples[c][i+j];
        				sumOfSquares += sample * sample;
        			}
        		}
        		squaresums[nsqsum] = sumOfSquares / length;
        		float mean = 0;
        		for ( int s = 0; s < NSQUARESUMS; s++ ) {
        			mean += squaresums[s];
        		}
        		if ( ++nsqsum >= NSQUARESUMS ) nsqsum = 0;
        		key = (float)sqrt(mean/NSQUARESUMS);
        		targetGain = function(key);
        	}

        	gain = dynamics(targetGain);
        	// affect all channels identically to preserve positional image
        	for ( int c = 0; c < nc; c++ ) {
        		samples[c][i] *= (gain * makeupGain) + dryGain;
        	}
        }
        // we only announce the final value at the end of the buffer
        // this effectively subsamples the dynamic gain
        // but typically attack and release will provide sufficient smoothing
        // for the avoidance of aliasing
		vars.setDynamicGain(gain);
        return AUDIO_OK;
    }

    // effect of comparison of detected against threshold - subclass issue
    protected abstract float function(float value);

    // hold is a gate subclass issue
    protected float dynamics(float target) {
        // anti-denormal not needed, decays to unity
        // seems back to front because (>)0 is max and 1 is min gain reduction
        float factor = target < envelope ?  attack : release;
		envelope = factor * (envelope - target) + target;
        return envelope;
    }
}
