// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.meter;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.SimpleAudioProcess;

/**
 * A partial K-System meter process, for proper K-System compatibility 0dBr
 * should equal 83dBC, you should adjust your monitoring level to achieve this.
 * http://www.digido.com/portal/pmodule_id=11/pmdmode=fullscreen/pageadder_page_id=59
 * Deficiencies:
 * high peak is always infinite hold, last 10s unimplemented.
 * no pink noise source.
 */
public class MeterProcess extends SimpleAudioProcess
{
    /**
     * @supplierCardinality 1
     * @link aggregation 
     */
    private MeterControls controls;

    private float bufferTime = -1;

    public MeterProcess(MeterControls controls) {
        this.controls = controls;
    }

    public int processAudio(AudioBuffer buffer) {
        int nc = buffer.getChannelCount();
        int ns = buffer.getSampleCount();
        float[] array;
        // write derived overs, peaks and averages to controls (indicators really)
        // as side effects of methods called
        // controls maintains derived states
        check(buffer);
        for ( int c = 0; c < nc; c++ ) {
            array = buffer.getChannel(c);
            detectOvers(c, array, ns);
            detectPeak(c, array, ns);
            detectAverage(c, array, ns);
        }
        return AUDIO_OK;
    }

    private void check(AudioBuffer buffer) {
        // tell controls of buffer latency time changes
        // so it can update exponential decay factors accordingly
        float ms = buffer.getSampleCount() / buffer.getSampleRate() * 1000;
        if ( bufferTime != ms ) {
            bufferTime = ms;
            controls.setUpdateTime(ms);
        }
    }

    protected void detectOvers(int chan, float[] samples, int len) {
        int overs = 0;
        float sample;
        for ( int i = 0; i < len; i++ ) {
            sample = samples[i]; // single array dereference
            if ( sample > 1 ) overs++;
            else if ( sample < -1 ) overs++;
        }
        if ( overs > 0 ) {
        	controls.addOvers(chan, overs);
        }
    }

    protected void detectPeak(int chan, float[] samples, int len) {
        float peak = 0;
        float sample;
        for ( int i = 0; i < len; i++ ) {
            sample = samples[i]; // single array dereference
            if ( sample > peak ) {
                peak = sample;
            } else if ( -sample > peak ) {
                peak = -sample;
            }
        }
        controls.setPeak(chan, peak);
	}

    // default average is RMS, override for LEQ etc.
    protected void detectAverage(int chan, float[] samples, int len) {
        float sumOfSquares = 0f;
        float sample;
        for ( int i = 0; i < len; i++ ) {
            sample = samples[i]; // single array dereference
            sumOfSquares += (sample * sample);
        }
        // return the square root of the mean of the sum of the squared sample
        // note we multiple by root 2 for AES-17 peak/average equivalence
        float rms = (float)(1.41*Math.sqrt(sumOfSquares / len));
        controls.setAverage(chan, rms);
    }
}
