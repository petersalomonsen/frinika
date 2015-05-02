// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import java.util.List;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;

/**
 * A mono multi tap delay for simulating close micing of a multiple drive unit cab.
 * Basically delegating to DelayBuffer
 */
public class CabMicingProcess implements AudioProcess
{
    /**
     * @link aggregationByValue
     * @supplierCardinality 1
     */
    private DelayBuffer delayBuffer;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private final Variables vars;

    private boolean wasBypassed;

    public CabMicingProcess(Variables vars) {
        this.vars = vars;
        wasBypassed = !vars.isBypassed(); // force update
    }

    public void open() {
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
        wasBypassed = bypassed;

        buffer.convertTo(ChannelFormat.MONO);
        float sampleRate = buffer.getSampleRate();
        float samplesPerMilli = sampleRate * 0.001f;

        if ( delayBuffer == null ) {
	        delayBuffer = new DelayBuffer(1, (int)(30 * samplesPerMilli), sampleRate);
        } else {
            delayBuffer.conform(buffer); // for sample rate changes
        }

        delayBuffer.append(buffer);
        for ( DelayTap tap : vars.getTaps() ) {
            float level = tap.getLevel();
            if ( level < 0.001 ) continue; // insignificant optimisation
            int delay = (int)(tap.getDelayMilliseconds() * samplesPerMilli);
            delayBuffer.tap(0, buffer, delay, level); // optimised mix
        }
        return AUDIO_OK;
    }

    public void close() {
        delayBuffer = null;
    }

    public interface Variables
    {
        boolean isBypassed();
        
        /**
         * Provide a list of delay taps.
         */
        List<DelayTap> getTaps();
    }

}
