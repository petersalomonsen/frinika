// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

import uk.org.toot.audio.system.AudioDevice;
import uk.org.toot.midi.core.MidiDevice;
import uk.org.toot.midi.core.MidiInput;

/**
 * A MidiSynth is a MidiDevice which is a MidiInput.
 * It is also an AudioDevice, it may implement AudioProcess at any level.
 * @author st
 */
public interface MidiSynth extends MidiDevice, MidiInput, AudioDevice
{
	/**
	 * Set a unique location in a system for this MidiSynth to allow
	 * disambiguation of multiple synths of the same type.
	 * @param location the unique location
	 */
	public void setLocation(String location);	
}
