// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.core.SimpleAudioProcess;

/**
 * @author st
 *
 */
public class NoiseProcess extends SimpleAudioProcess
{
	private NoiseControls controls;
	private float y2;
	private boolean pass = true;
	
	public NoiseProcess(NoiseControls controls) {
		this.controls = controls;
	}
	
	public int processAudio(AudioBuffer buffer) {
		if ( controls.isBypassed() ) return AUDIO_OK;
		buffer.setChannelFormat(ChannelFormat.MONO);
		int ns = buffer.getSampleCount();
		float[] samples = buffer.getChannel(0);
		for ( int i = 0; i < ns; i++ ) {
			samples[i] = noise() * 0.1f;
		}
		return AUDIO_OK;
	}
	
	protected float noise() {
		float x1, x2, w, y1;

		pass = !pass;
		if ( pass ) return y2;
		
        do {
        	x1 = (float)(2 * Math.random() - 1);
        	x2 = (float)(2 * Math.random() - 1);
        	w = x1 * x1 + x2 * x2;
        } while ( w >= 1.0 );

        w = (float)Math.sqrt( (-2.0 * Math.log( w ) ) / w );
        y1 = x1 * w;
        y2 = x2 * w;
        return y1;
	}
}
