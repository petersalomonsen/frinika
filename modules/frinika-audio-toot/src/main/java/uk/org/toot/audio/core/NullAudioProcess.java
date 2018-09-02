// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

/**
 * An AudioProcess that does nothing which is useful for testing.
 */
public class NullAudioProcess extends SimpleAudioProcess
{
    public int processAudio(AudioBuffer buffer) {
    	// By returning AUDIO_DISCONNECT rather than AUDIO_OK we
    	// allow subsequent processing to be avoided.
    	return AUDIO_DISCONNECT;
    }
}
