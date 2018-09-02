// Copyright (C) 2008 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.spi;

import uk.org.toot.control.spi.ControlServiceDescriptor;
import uk.org.toot.synth.SynthControls;

public class SynthControlServiceDescriptor extends ControlServiceDescriptor
{
    public SynthControlServiceDescriptor(Class<? extends SynthControls> clazz, int moduleId,
        	String name, String description, String version) {
        super(clazz, moduleId, name, description, version);
    }
}
