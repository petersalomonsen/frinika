// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.system;

import uk.org.toot.audio.core.AudioProcess;

public interface AudioPort extends AudioProcess
{
	/**
	 * Return a unique name for this audio port.
	 * Typically by concatenating the logical, but not necessarily unique, port name 
	 * with a unique decice name.
	 * @return String - a unique port name
	 */
    String getName();
}
