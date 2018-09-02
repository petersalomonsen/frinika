// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

import static uk.org.toot.dsp.FastMath.*;

/**
 * A dynamics process for classic compressors/limiters
 * No constant ratio, knee is implicit
 * @author st
 *
 */
abstract public class ClassicDynamicsProcess extends SimpleAudioProcess
{
    protected float envelope = 1f;
    private float gain = 1f;
    
    protected boolean isPeak = true;
    protected float threshold;
    protected float inverseThreshold;
    protected float attack, release;
    protected float makeupGain, dryGain;

    protected DynamicsVariables vars;

    private boolean wasBypassed;

    private int sampleRate = 0;

    private float[][] samples = new float[6][];	

    public ClassicDynamicsProcess(DynamicsVariables vars) {
        this.vars = vars;
        wasBypassed = !vars.isBypassed(); // force update
    }

    protected ClassicDynamicsProcess() {} // for testing subclasses only
    
    public void clear() {
        envelope = 1f; // envelope of gain
        gain = 1f;
       	vars.setDynamicGain(1f);
    }

    /**
     * Called once per AudioBuffer
     */
    protected void cacheProcessVariables() {
        // update local variables
        threshold = vars.getThreshold();
        inverseThreshold = vars.getInverseThreshold();
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
        int sr = (int)buffer.getSampleRate();
        if ( sr != sampleRate ) {
        	sampleRate = sr;
        	vars.update(sr); // rederives attack, release
        }
        cacheProcessVariables();

        int len = buffer.getSampleCount();
        int nc = buffer.getChannelCount();
		for ( int c = 0; c < nc; c++ ) {
			samples[c] = buffer.getChannel(c);
		}

		if ( vars.isRMS() ) {
            for ( int i = 0; i < len; i++ ) {
                float key = 0;
                for ( int c = 0; c < nc; c++ ) {
                    key += samples[c][i] * samples[c][i];
                }
                gain = (float)sqrt(dynamics(function(key)));
                // affect all channels identically to preserve positional image
                for ( int c = 0; c < nc; c++ ) {
                    samples[c][i] *= (gain * makeupGain) + dryGain;
                }
            }
		} else {
            float key = 0;
		    for ( int i = 0; i < len; i++ ) {
		        for ( int c = 0; c < nc; c++ ) {
                    key = max(key, abs(samples[c][i]));
		        }
		        gain = dynamics(function(key));
                // affect all channels identically to preserve positional image
                for ( int c = 0; c < nc; c++ ) {
                    samples[c][i] *= (gain * makeupGain) + dryGain;
                }
		    }
		}
        // we only announce the final value at the end of the buffer
        // this effectively subsamples the dynamic gain
        // but typically attack and release will provide sufficient smoothing
        // for the avoidance of aliasing
		vars.setDynamicGain(gain);
        wasBypassed = bypassed;
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
