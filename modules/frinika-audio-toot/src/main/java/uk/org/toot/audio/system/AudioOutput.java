// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

public interface AudioOutput extends AudioPort
{
	/**
	 * Return a unique string representing the location.
	 * e.g. MultiSynth C Channel 2
	 * e.g. Sequencer 17
	 * This should be the same string as used by AudioBuffer.MetaInfo.
	 * @return String the location of this AudioOutput
	 */
	String getLocation();
}
