// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;
import uk.org.toot.dsp.FastMath;

/**
 * @author st
 */
public class TremoloProcess extends SimpleAudioProcess
{
	private Variables vars;
	private float[][] samples = new float[8][];
	private float lfoPhase = 0f;

	public TremoloProcess(Variables variables) {
		vars = variables;
	}

	public int processAudio(AudioBuffer buffer) {
		if ( vars.isBypassed() ) return AUDIO_OK;
		int ns = buffer.getSampleCount();
		int nc = buffer.getChannelCount();
		for ( int c = 0; c < nc; c++ ) {
			samples[c] = buffer.getChannel(c);
		}
        float _lfoInc = 2 * (float)Math.PI * (vars.getRate() / buffer.getSampleRate());
		float depth = vars.getDepth();
		float mod;
		for ( int s = 0; s < ns; s++ ) {
            mod  = 1 - depth * ((FastMath.sin( lfoPhase ) + 1f) * 0.5f);
            lfoPhase += _lfoInc;
            if ( lfoPhase >= Math.PI )
            	lfoPhase -= Math.PI * 2;

			for ( int c = 0; c < nc; c++ ) {
				samples[c][s] *= mod;
			}
		}
		return AUDIO_OK;
	}

	public static interface Variables
	{
		boolean isBypassed();
		float getDepth();
		float getRate();
	}
}
