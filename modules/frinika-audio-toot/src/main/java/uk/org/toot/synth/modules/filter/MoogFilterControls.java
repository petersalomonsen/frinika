// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

public class MoogFilterControls extends FilterControls
{
	public MoogFilterControls(int instanceIndex, String name, final int idOffset) {
		super(FilterIds.MOOG_LPF_ID, instanceIndex, name, idOffset);
	}
	
	protected float deriveResonance() {
		return super.deriveResonance() * 4;
	}
}
