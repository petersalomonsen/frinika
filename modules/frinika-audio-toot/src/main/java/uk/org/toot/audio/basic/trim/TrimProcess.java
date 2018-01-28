// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.basic.trim;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

/**
 * @author st
 */
public class TrimProcess extends SimpleAudioProcess
{
	private TrimControls controls;
	
	public TrimProcess(TrimControls controls) {
		this.controls = controls;
	}
	
	public int processAudio(AudioBuffer buffer) {
		if ( controls.isBypassed() ) return AUDIO_OK;
		float trim = controls.getTrim();
		int nc = buffer.getChannelCount();
		int ns = buffer.getSampleCount();
		float[] samples;
		for ( int c = 0; c < nc; c++ ) {
			samples = buffer.getChannel(c);
			for ( int s = 0; s < ns; s++ ) {
				samples[s] *= trim;
			}
		}
		return AUDIO_OK;
	}

}
