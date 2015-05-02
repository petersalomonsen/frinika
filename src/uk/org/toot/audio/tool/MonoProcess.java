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
public class MonoProcess extends SimpleAudioProcess
{
    private Variables vars;
    
    public MonoProcess(Variables vars) {
        this.vars = vars;
    }

    public int processAudio(AudioBuffer buffer) {
        if ( vars.isBypassed() ) return AUDIO_OK;
        buffer.convertTo(ChannelFormat.MONO);
        return AUDIO_OK;
    }

    public interface Variables
    {
        boolean isBypassed();
    }
}
