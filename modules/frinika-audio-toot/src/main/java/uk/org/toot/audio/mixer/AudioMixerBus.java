// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

import uk.org.toot.audio.core.*;
import uk.org.toot.audio.meter.MeterProcess;
import static uk.org.toot.audio.mixer.MixerControlsIds.*;

/**
 * An AudioMixerBus synchronously mixes various signals and then writes
 * its buffer to an AudioProcess specified with setOutputProcess().
 * It has a K-System meter.
 */
public class AudioMixerBus {
    /**
     * @link aggregationByValue
     * @supplierCardinality 1 
     */
    private AudioBuffer buffer;

    /**
     * @label output
     * @supplierCardinality 0..1
     * @link aggregation 
     */
    private AudioProcess output = null;

    private AudioProcess meter = null;

    private boolean isFx = false;

    private String name;

    private ChannelFormat channelFormat;

    public AudioMixerBus(AudioMixer mixer, BusControls busControls) {
        name = busControls.getName();
        isFx = busControls.getId() == FX_BUS; // !!! !!!
        channelFormat = busControls.getChannelFormat();
       	buffer = mixer.createBuffer(name);
        buffer.setChannelFormat(channelFormat);
        setMeterProcess(new MeterProcess(busControls.getMeterControls()));
    }

    public void setOutputProcess(AudioProcess output) {
        this.output = output;
    }

    public void setMeterProcess(AudioProcess meter) {
        this.meter = meter;
    }

    public AudioBuffer getBuffer() { return buffer; }

    public String getName() { return name; }

    public void silence() {
       	buffer.makeSilence();
    }

    public void write() {
        if ( output == null && !isFx ) return;
        if ( output != null ) {
        	output.processAudio(buffer);
        }
        if ( meter != null ) {
            meter.processAudio(buffer);
        }
    }
}


