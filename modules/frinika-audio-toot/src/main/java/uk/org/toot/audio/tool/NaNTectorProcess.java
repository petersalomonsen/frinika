// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

/**
 * Detect and protect against NaN float values.
 * @author st
 *
 */
public class NaNTectorProcess extends SimpleAudioProcess
{
	private NaNTectorControls controls;
	
	public NaNTectorProcess(NaNTectorControls controls) {
		this.controls = controls;
	}

	public int processAudio(AudioBuffer buffer) {
		if ( controls.isBypassed() ) return AUDIO_OK;
		int ns = buffer.getSampleCount();
		int nc = buffer.getChannelCount();
		int sc = ns * nc;
		int nans = 0;
		float[] samples;
		float f;
				
		for ( int c = 0; c < nc; c++ ) {
			samples = buffer.getChannel(c);
			for ( int s = 0; s < ns; s++ ) {
				f = samples[s];
				if ( f == f ) continue;
				samples[s] = 0;
				nans++;
			}
		}		
		controls.setNaNFactor((float)nans / sc);		
		return AUDIO_OK;
	}
}
