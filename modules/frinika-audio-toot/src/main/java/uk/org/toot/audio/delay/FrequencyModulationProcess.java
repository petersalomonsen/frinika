// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;

/**
 * A Modulated Delay Process for Frequency Modulation effects such
 * as Vibrato and Wow & Flutter.
 */
public abstract class FrequencyModulationProcess implements AudioProcess
{
    /**
     * @link aggregationByValue
     * @supplierCardinality 1 
     */
    private DelayBuffer delayBuffer;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private final DelayVariables vars;
    private float[][] samples = new float[6][];

    protected ChannelFormat format;
    protected int sampleRate = 0;
    
    private boolean wasBypassed;
    private float maxDelayMillis;
    private int staticDelay;

    public FrequencyModulationProcess(DelayVariables vars) {
        this.vars = vars;
        wasBypassed = !vars.isBypassed(); // force update
        maxDelayMillis = vars.getMaxDelayMilliseconds();
    }

    public void open() {
        // buffer allocation is deferred until sample rate is known
    }

    protected void cacheProcessVariables() {        
    }
    
    protected void sampleRateChanged() {
    }
    
    public int processAudio(AudioBuffer buffer) {
        boolean bypassed = vars.isBypassed();
        if ( bypassed ) {
            if ( !wasBypassed ) {
                // silence buffer on transition to bypassed
                if ( delayBuffer != null ) delayBuffer.makeSilence();
                wasBypassed = true;
            }
            return AUDIO_OK;
        }
        wasBypassed = bypassed;

        int sr = (int)buffer.getSampleRate();
        if ( sampleRate != sr ) {
            sampleRate = sr;
            delayBuffer = new DelayBuffer(buffer.getChannelCount(),
                    msToSamples(maxDelayMillis, sampleRate), sampleRate);        
            staticDelay = (int)(delayBuffer.msToSamples(maxDelayMillis/2f));
            sampleRateChanged();
        }
        delayBuffer.conform(buffer);

        cacheProcessVariables();
        
        int ns = buffer.getSampleCount();
        int nc = buffer.getChannelCount();

        for ( int c = 0; c < nc; c++ ) {
        	samples[c] = buffer.getChannel(c); 
        }
        for ( int s = 0; s < ns; s++ ) {
            float delay = staticDelay * (1f + modulation());
	        for ( int ch = 0; ch < nc; ch++ ) {
                delayBuffer.append(ch, samples[ch][s]); 
                samples[ch][s] = delayBuffer.outA(ch, delay);
            }
            delayBuffer.nudge(1);
        }

        return AUDIO_OK;
    }

    public void close() {
        delayBuffer = null;
    }

    protected int msToSamples(float ms, float sr) {
        return Math.round((ms * sr) / 1000);
    }
    
    /**
     * Return the per sample normalised modulation
     * @return -1f .. +1f
     */
    abstract protected float modulation();
    
}
