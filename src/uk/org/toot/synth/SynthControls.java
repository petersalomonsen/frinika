// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

import uk.org.toot.control.CompoundControl;

/**
 * This class represents the controls for a synth.
 * All of the children of SynthRackControls must extend this class.
 * @author st
 */
public abstract class SynthControls extends CompoundControl
{
	public SynthControls(int id, String name) {
		super(id, name);
	}
}
