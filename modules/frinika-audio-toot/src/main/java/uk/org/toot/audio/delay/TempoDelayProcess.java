// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.dsp.FastMath;
import uk.org.toot.misc.Tempo;

/**
 * A Tempo linked Delay Process
 * Basically delegating to DelayBuffer
 */
public class TempoDelayProcess implements AudioProcess
{
    /**
     * @link aggregationByValue
     * @supplierCardinality 1 
     */
    private DelayBuffer delayBuffer;

    /**
     * @link aggregationByValue
     * @supplierCardinality 1 
     */
    private DelayBuffer tappedBuffer; // just for conform()

	private Tempo.Listener tempoListener;

	/**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private final Variables vars;

    private boolean wasBypassed;

    private float bpm = 120f;
    
    private float meansquare = 0f;
    private float meanK = 0.2f;
    private float smoothedMix;
    
    public TempoDelayProcess(Variables vars) {
        this.vars = vars;
        wasBypassed = !vars.isBypassed(); // force update
		tempoListener = new Tempo.Listener() {
			public void tempoChanged(float newTempo) {
				bpm = newTempo;				
			}			
		};
		smoothedMix = vars.getMix();
    }

    public void open() {
        Tempo.addTempoListener(tempoListener);
        // defer delay buffer allocation until sample rate known
    }

    /*
   	If all taps delays are > buffer time
    the delayed output is independent of input
	but need 3 buffers: buffer (in/out), delayBuffer, tappedBuffer
   	*/
    public int processAudio(AudioBuffer buffer) {
        boolean bypassed = vars.isBypassed();
        if ( bypassed ) {
            if ( !wasBypassed ) {
                if ( delayBuffer != null ) {
                    // silence delay buffer on transition to bypassed
                    delayBuffer.makeSilence();
                }
                wasBypassed = true;
            }
            return AUDIO_OK;
        }

        float sampleRate = buffer.getSampleRate();
        int ns = buffer.getSampleCount();
        int nc = buffer.getChannelCount();

        float feedback = vars.getFeedback();
		float mix = vars.getMix();

        if ( delayBuffer == null ) {
	        delayBuffer = new DelayBuffer(nc,
                msToSamples(vars.getMaxDelayMilliseconds(), sampleRate),
                sampleRate);
        } else {
            delayBuffer.conform(buffer);
        }

        if ( tappedBuffer == null ) {
	        tappedBuffer = new DelayBuffer(nc, ns, sampleRate);
        } else {
            tappedBuffer.conform(buffer);
            // conform only changes number of channels and sample rate
            if ( tappedBuffer.getSampleCount() != ns ) {
                tappedBuffer.changeSampleCount(ns, false);
            }
        }

        float ducking = vars.getDucking();
        if ( ducking < 1f ) {
        	float square = buffer.square();
        	//meansquare = meansquare * meanK + square * (1 - meanK); // !!!
        	meansquare += meanK * (square - meansquare);
        	// * 10 to normalise to ducking
        	float rms = 10f * (float)FastMath.sqrt(meansquare);
        	// calculate mix duck factor 0..1
        	if ( rms < ducking ) {
        		// intentionally empty, * 1
        	} else if ( rms > 1f ) {
        		mix *= ducking; // full ducking
        	} else {
        		// as rms [ducking .. 1], factor [1 .. ducking]
        		mix *= ducking / rms; //? straight in linear, but not in log
        		// also rms + ducking = 1 ?
        	}
        }
        smoothedMix += 0.05f * (mix - smoothedMix);
    	// tapped from delayed
    	tappedBuffer.makeSilence();
		int delay = (int)msToSamples(60000*vars.getDelayFactor()/bpm, sampleRate);
        for ( int c = 0; c < nc; c++ ) {
            if ( delay < ns ) continue; // can't evaluate. push down to called method?
    		delayBuffer.tap(c, tappedBuffer, delay, 1f); // optimised mix
		}
    	// append buffer + filtered tapped feedback to delayed
    	delayBuffer.appendFiltered(buffer, tappedBuffer, feedback * 1.1f, vars.getLowpassCoefficient());
    	// tapped mixed to buffer
        for ( int c = 0; c < nc; c++ ) {
            float[] samples = buffer.getChannel(c);
            float[] tapped = tappedBuffer.getChannel(c);
            for ( int i = 0; i < ns; i++ ) {
                samples[i] += smoothedMix * tapped[i];
            }
        }

        wasBypassed = bypassed;
        return AUDIO_OK;
    }

    public void close() {
        delayBuffer = null;
        tappedBuffer = null;
        Tempo.removeTempoListener(tempoListener);
    }

    protected int msToSamples(float ms, float sr) {
        return (int)((ms * sr) / 1000); // !!! !!! move elsewhere
    }
    
    public interface Variables extends DelayVariables
    {
    	float getDelayFactor(); // quarter note = 1, half note = 2 etc.
    	
        float getFeedback();

        float getMix();
        
        float getDucking(); // >0 .. 1
        
        float getLowpassCoefficient();
    }

}
