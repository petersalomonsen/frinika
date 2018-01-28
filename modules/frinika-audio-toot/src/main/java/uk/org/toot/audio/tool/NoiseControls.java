// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.AudioControls;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.audio.tool.ToolIds.NOISE_ID;

/**
 * @author st
 *
 */
public class NoiseControls extends AudioControls
{

	public NoiseControls() {
		super(NOISE_ID, getString("Noise"));
	}

}
