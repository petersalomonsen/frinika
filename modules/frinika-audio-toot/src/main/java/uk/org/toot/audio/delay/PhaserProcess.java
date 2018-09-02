// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;
import uk.org.toot.dsp.FastMath;

import static uk.org.toot.audio.core.FloatDenormals.*;

/*
 * A Mono Phaser built with 6 all pass filter stages.
 * http://musicdsp.org/files/phaser.cpp by Ross Bencina
 */
public class PhaserProcess extends SimpleAudioProcess
{
	private final static int N = 12;
	
	private AllPass[] allpass = new AllPass[N];
	private float zm1 = 0f;			// previous output
	private float lfoPhase = 0f;
	private float dmin, dmax;
	private int sampleRate = -1;
	private Variables vars;
	private float a1;				// shared allpass coeff
	
	public PhaserProcess(Variables vars) {
		this.vars = vars;
		for ( int i = 0; i < N; i++ ) {
			allpass[i] = new AllPass();
		}
	}
	
	public int processAudio(AudioBuffer buffer) {
		if ( vars.isBypassed() ) return AUDIO_OK;
        int sr = (int)buffer.getSampleRate();
        int ns = buffer.getSampleCount();
        float[] samples = buffer.getChannel(0);
        
        if ( sr != sampleRate ) {
        	dmin = 440f / (sr/2f);  // !! actually min norm freq
            dmax = 1600f / (sr/2f); // !! actually max norm freq
            sampleRate = sr;
        }
        
        int n = vars.getStages();
        float depth = vars.getDepth();
        float fb = vars.getFeedback();
        float _lfoInc = 2 * (float)Math.PI * (vars.getRate() / sampleRate);
        
        for ( int i = 0; i < ns; i++ ) {
        	// calculate and update sweep lfo...
            float d  = dmin + (dmax - dmin) * ((FastMath.sin( lfoPhase ) + 1f) * 0.5f);
            lfoPhase += _lfoInc;
            if ( lfoPhase >= Math.PI )
            	lfoPhase -= Math.PI * 2;

            // calculate allpass output
        	a1 = (1f - d) / (1f + d);
            float y = samples[i] + zm1 * fb; 
            for ( int a = 0; a < n; a++ ) {
            	y = allpass[a].update(y);
            }
            zm1 = zeroDenorm(y);
       		samples[i] += zm1 * depth;
        }
        
		return AUDIO_OK;
	}

	private class AllPass
	{
		private float zm1 = 0f;
		
		public float update(float in) {
        	float y = in * -a1 + zm1;
        	zm1 = zeroDenorm(y * a1 + in);
            return y;
		}
	}
	
	public interface Variables
	{
		boolean isBypassed();
        int getStages();
		float getRate();		// Hz
		float getDepth();		// 0..1
		float getFeedback();	// 0..1
	}

}
