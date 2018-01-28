// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.basic.stereoImage;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;
import uk.org.toot.audio.core.ChannelFormat;

/**
 * Implements the stereo image digital signal processing
 * @author st
 *
 */
public class StereoImageProcess extends SimpleAudioProcess
{
    /** @link aggregation
     * @supplierCardinality 1 
     */
    private StereoImageProcessVariables vars;

    public StereoImageProcess(StereoImageProcessVariables variables) {
        vars = variables;
    }

    public int processAudio(AudioBuffer buffer) {
		if ( vars.isBypassed() ) return AUDIO_OK;
        int nsamples = buffer.getSampleCount();
        buffer.monoToStereo();
        float otherFactor = vars.getWidthFactor();
        boolean swap = vars.isLRSwapped();
        ChannelFormat format = buffer.getChannelFormat();
        // get left/right pairs
        int[] leftChans = format.getLeft();
        int[] rightChans = format.getRight();
        float tmp;
        for ( int pair = 0; pair < leftChans.length; pair++ ) {
	   	    float[] left = buffer.getChannel(leftChans[pair]);
    	   	float[] right = buffer.getChannel(rightChans[pair]);
	        // first we process the L/R width
   	    	for ( int i = 0; i < nsamples; i++ ) {
   	    		tmp = left[i];
    			left[i] += otherFactor * right[i];
	            right[i] += otherFactor * tmp;
   			}
   			// then we swap if necessary
            if ( swap ) buffer.swap(leftChans[pair], rightChans[pair]);
        }
        return AUDIO_OK;
    }
}
