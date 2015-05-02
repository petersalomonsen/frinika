// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.system.AudioOutput;

/**
 * Paraphonic in that it is not fully polyphonic, a Voice may have an oscillator, but
 * not necessarily its own filter or amplifier. It may have a single filter and/or amplifier
 * shared by all Voices, These shared paraphonic parts can be applied in postProcessAudio().
 * Obviously these paraphonic parts are always after the mixing of the individual Voices
 * so post processing is appropriate.
 * @author st
 */
public abstract class ParaphonicSynthChannel extends PolyphonicSynthChannel 
	implements AudioOutput
{

	public ParaphonicSynthChannel(String name) {
		super(name);
	}

	@Override
	public int processAudio(AudioBuffer buffer) {
		int ret = super.processAudio(buffer);
		postProcessAudio(buffer, ret);
		return ret;
	}
	
	abstract protected int postProcessAudio(AudioBuffer buffer, int ret);
	
}
