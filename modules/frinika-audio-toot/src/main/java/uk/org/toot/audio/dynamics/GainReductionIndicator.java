// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class GainReductionIndicator extends FloatControl
{
    private static LinearLaw GR_LAW = new LinearLaw(-20f, 0, "dB");
    
    public GainReductionIndicator() {
        super(-1, "Gain Reduction", GR_LAW, 3f, 0f);
        indicator = true;
        setHidden(true); // prevent normal layout
    }
}