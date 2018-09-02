// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.core.SimpleAudioProcess;
import uk.org.toot.dsp.Sine;

/**
 * @author st
 *
 */
public class SineProcess extends SimpleAudioProcess
{
	private Variables vars;
    private Sine sine;
    private int freq = -1;
    private int sampleRate = 44100;
	
	public SineProcess(Variables variables) {
		vars = variables;
	}
	
	public int processAudio(AudioBuffer buffer) {
		if ( vars.isBypassed() ) return AUDIO_OK;
        final int f = vars.getFrequency();
        final int sr = (int)buffer.getSampleRate();
        boolean update = false;
        if ( freq != f ) {
            freq = f;
            update = true;
        }
        if ( sampleRate != sr ) {
            sampleRate = sr;
            update = true;
        }
        if ( update ) {
            sine = createOscillator(freq, sampleRate);
        }
		buffer.setChannelFormat(ChannelFormat.MONO);
		final int ns = buffer.getSampleCount();
		float[] samples = buffer.getChannel(0);
		for ( int i = 0; i < ns; i++ ) {
			samples[i] = sine.out() * 0.1f;
		}
		return AUDIO_OK;
	}
    
    protected Sine createOscillator(int f, int fs) {
        return new Sine(f*2*Math.PI/fs);
    }
    
    public interface Variables
    {
        boolean isBypassed();
        int getFrequency();
    }
}
