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
 * A Multi Tap Delay Process
 * Basically delegating to DelayBuffer
 */
public class MultiTapDelayProcess implements AudioProcess
{
    /**
     * @link aggregationByValue
     * @supplierCardinality 1
     */
    private DelayBuffer delayBuffer;

    /**
     * @link aggregationByValue
     * @supplierCardinality 1
     */
    private DelayBuffer tappedBuffer; // just for conform()

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private final Variables vars;

    private boolean wasBypassed;

    public MultiTapDelayProcess(Variables vars) {
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

        buffer.monoToStereo();
        float sampleRate = buffer.getSampleRate();
        float samplesPerMilli = sampleRate * 0.001f;
        int ns = buffer.getSampleCount();
        int nc = buffer.getChannelCount();

        float feedback = vars.getFeedback();
		float mix = vars.getMix();

        if ( delayBuffer == null ) {
	        delayBuffer = new DelayBuffer(nc,
                (int)(vars.getMaxDelayMilliseconds() * samplesPerMilli),
                sampleRate);
        } else {
            delayBuffer.conform(buffer);
        }

        if ( tappedBuffer == null ) {
	        tappedBuffer = new DelayBuffer(nc, ns, sampleRate);
        } else {
            tappedBuffer.conform(buffer);
            // conform only changes number of channels and sample rate
            if ( tappedBuffer.getSampleCount() != ns ) {
                tappedBuffer.changeSampleCount(ns, false);
            }
        }

    	// tapped from delay
    	tappedBuffer.makeSilence();
        float delayFactor = vars.getDelayFactor();
        ChannelFormat format = buffer.getChannelFormat();
        for ( int c = 0; c < nc; c++ ) {
            int c2; // map buffer channel to control channel
			if ( format.isLeft(c) ) c2 = ChannelFormat.STEREO.getLeft()[0];
            else if ( format.isRight(c) ) c2 = ChannelFormat.STEREO.getRight()[0];
            else continue; // neither left nor right
	        for ( DelayTap tap : vars.getTaps(c2) ) {
	            float level = tap.getLevel();
    	        if ( level < 0.001 ) continue; // insignificant optimisation
				int delay = (int)(tap.getDelayMilliseconds()*delayFactor*samplesPerMilli);
            	if ( delay < ns ) continue; // can't evaluate. push down to called method?
    			delayBuffer.tap(c, tappedBuffer, delay, level); // optimised mix
			}
		}
    	// delay append process + tapped * feedback
    	delayBuffer.append(buffer, tappedBuffer, feedback);
    	// process mixed from process and tapped
        for ( int c = 0; c < nc; c++ ) {
            float[] samples = buffer.getChannel(c);
            float[] tapped = tappedBuffer.getChannel(c);
            for ( int i = 0; i < ns; i++ ) {
                samples[i] += mix * tapped[i];
            }
        }

        wasBypassed = bypassed;
        return AUDIO_OK;
    }

    public void close() {
        delayBuffer = null;
        tappedBuffer = null;
    }

    public interface Variables extends DelayVariables
    {

        /**
         * Provide a list of delay taps.
         * Parameterisation by channel index ALLOWS per channel delay taps but
         * also ALLOWS a single list of taps to be used for all channels.
         * Allocation of taps to channels is the responsibility of the
         * implementation so other allocations are also possible.
         */
        List<DelayTap> getTaps(int chan);

        float getFeedback();

        float getMix();

        int getChannelCount();

        float getDelayFactor();
    }

}
