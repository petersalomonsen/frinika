// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import uk.org.toot.control.*;

public class CrossoverControl extends FloatControl
{
    private final static ControlLaw XO_LAW = new LogLaw(100f, 10000f, "Hz");

    public CrossoverControl(String name, float freq) {
        // this will need an idOffset for > dual band controls
        super(DynamicsControlIds.CROSSOVER_FREQUENCY, name, XO_LAW, 1.0f, freq);
    }

    public int getFrequency() {
        return (int)getValue();
    }
}


