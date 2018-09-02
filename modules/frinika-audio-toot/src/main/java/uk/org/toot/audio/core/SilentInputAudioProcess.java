// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

/**
 * An input AudioProcess which provides silence with a specified ChannelFormat
 * and source label.
 * @author st
 */
public class SilentInputAudioProcess extends SimpleAudioProcess {
    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private ChannelFormat channelFormat;
    private AudioBuffer.MetaInfo metaInfo;

    public SilentInputAudioProcess(ChannelFormat format, String label) {
        channelFormat = format;
        metaInfo = new AudioBuffer.MetaInfo(label, "");
    }

    public int processAudio(AudioBuffer buffer) {
        buffer.setMetaInfo(metaInfo);
        buffer.setChannelFormat(channelFormat);
        buffer.makeSilence();
    	return AUDIO_OK;
    }
}
