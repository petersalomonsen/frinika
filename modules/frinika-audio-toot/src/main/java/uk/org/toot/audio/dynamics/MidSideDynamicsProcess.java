// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

import static uk.org.toot.dsp.FastMath.*;

abstract public class MidSideDynamicsProcess extends SimpleAudioProcess
{
    protected float[] envelope = new float[2];

    protected boolean isPeak = false;
    protected float[] threshold;
    protected float[] attack, release;
    protected float[] makeupGain;

    protected Variables vars;

    private boolean wasBypassed;

    private int sampleRate = 0;

    private int NSQUARESUMS = 10;
    private float[] squaresumsM = new float[NSQUARESUMS];
    private float[] squaresumsS = new float[NSQUARESUMS];
    private int nsqsum = 0;

    private float[] samplesM, samplesS;

    public MidSideDynamicsProcess(Variables vars) {
        this(vars, false);
    }

    public MidSideDynamicsProcess(Variables vars, boolean peak) {
        this.vars = vars;
        this.isPeak = peak;
        wasBypassed = !vars.isBypassed(); // force update
    }

    public void clear() {
        envelope[0] = envelope[1] = 1f; // envelope of gain
       	vars.setDynamicGain(1f, 1f);
    }

    /**
     * Called once per AudioBuffer
     */
    protected void cacheProcessVariables() {
        // update local variables
        threshold = vars.getThreshold();
        attack = vars.getAttack();
        release = vars.getRelease();
        makeupGain = vars.getGain();
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
        float targetGainM = 1f; // unity
        float targetGainS = 1f;
        float gainM = 0f; // keeps compiler happy
        float gainS = 0f;

        int len = buffer.getSampleCount();
        int mslen = (int)(buffer.getSampleRate() * 0.001f);
        float sumdiv = 1f / (mslen + mslen);
        
        if ( !buffer.encodeMidSide() ) return AUDIO_OK; // mono abort, TODO sumfin better
		samplesM = buffer.getChannel(0);
        samplesS = buffer.getChannel(1);

        for ( int i = 0; i < len; i++ ) {
        	float keyM = 0;
            float keyS = 0;
        	if ( isPeak ) {
                keyM = max(keyM, abs(samplesM[i]));
                keyS = max(keyS, abs(samplesS[i]));
        		targetGainM = function(0, keyM);
                targetGainS = function(1, keyS);
        	} else if ( (i % mslen) == 0 && (i + mslen) < len ) {
        		// the rms side chain calculations, every millisecond
        		float sumOfSquaresM = 0f;
                float sumOfSquaresS = 0f;
                for ( int j = 0, k = i; j < mslen; j++, k++ ) {
                    sumOfSquaresM += samplesM[k] * samplesM[k];
                    sumOfSquaresS += samplesS[k] * samplesS[k];
                }
        		squaresumsM[nsqsum] = sumOfSquaresM * sumdiv;
                squaresumsS[nsqsum] = sumOfSquaresS * sumdiv;
        		float meanM = 0;
                float meanS = 0;
        		for ( int s = 0; s < NSQUARESUMS; s++ ) {
        			meanM += squaresumsM[s];
                    meanS += squaresumsS[s];
        		}
        		if ( ++nsqsum >= NSQUARESUMS ) nsqsum = 0;
                targetGainM = function(0, (float)sqrt(meanM/NSQUARESUMS));
                targetGainS = function(1, (float)sqrt(meanS/NSQUARESUMS));
        	}

        	gainM = dynamics(0, targetGainM);
            gainS = dynamics(1, targetGainS);
        	// affect all channels identically to preserve positional image
       		samplesM[i] *= gainM * makeupGain[0];
            samplesS[i] *= gainS * makeupGain[1];
        }
        buffer.decodeMidSide();
        // we only announce the final value at the end of the buffer
        // this effectively subsamples the dynamic gain
        // but typically attack and release will provide sufficient smoothing
        // for the avoidance of aliasing
		vars.setDynamicGain(gainM, gainS);
        wasBypassed = bypassed;
        return AUDIO_OK;
    }

    // effect of comparison of detected against threshold - subclass issue
    protected abstract float function(int i, float value);

    // hold is a gate subclass issue
    protected float dynamics(int i, float target) {
        // anti-denormal not needed, decays to unity
        // seems back to front because (>)0 is max and 1 is min gain reduction
        float factor = target < envelope[i] ?  attack[i] : release[i];
		envelope[i] = factor * (envelope[i] - target) + target;
        return envelope[i];
    }

    /**
     * Specifies parameters in implementation terms
     */
    public interface Variables {
        void update(float sampleRate);
        boolean isBypassed();
        float[] getThreshold();     //  NOT dB, the actual level
        float[] getInverseThreshold();     //  NOT dB, the actual level
        float[] getInverseRatio();  // 1/ratio avoids real-time divides
        float[] getKnee();		    //	NOT dB, the actual level
        float[] getAttack();		//	NOT ms, the exponential coefficient
        int[] getHold();			//	NOT ms, samples
        float[] getRelease();		//	NOT ms, the exponential coefficient
        float[] getDepth();	        //	NOT dB, the actual level
        float[] getGain();		    // 	NOT dB, the actual static makeup gain
        void setDynamicGain(float gainM, float gainS); // NOT dB, the actual (sub sampled) dynamic gain
    }
}
