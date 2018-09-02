// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.*;
import static uk.org.toot.audio.core.FloatDenormals.*;

/**
 * This class
 */
public class DenormalProcess extends SimpleAudioProcess
{
    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private DenormalControls controls;

    public DenormalProcess(DenormalControls c) {
        controls = c;
    }

    public int processAudio(AudioBuffer buffer) {
        if ( controls.isBypassed() ) return AUDIO_OK;
        int ns = buffer.getSampleCount();
        int nc = buffer.getChannelCount();
		int sc = ns * nc;
		int denorms = 0;
        float[] samples;
        
        for ( int c = 0; c < nc; c++ ) {
        	samples = buffer.getChannel(c);
        	for ( int i = 0; i < ns; i++ ) {
        		if ( isDenormal(samples[i]) ) {
        			samples[i] = 0f;
        			denorms++;
        		}
        	}
        }
		controls.setDenormFactor((float)denorms / sc);
        return AUDIO_OK;
    }
}
