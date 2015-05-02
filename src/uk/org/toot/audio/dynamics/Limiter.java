// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import static uk.org.toot.misc.Localisation.*;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LogLaw;

public class Limiter extends DynamicsProcess
{
    public Limiter(DynamicsVariables vars) {
        super(vars, true); // peak detection
    }

    // limit
    protected float function(float value) {
        return value > threshold ? threshold / value : 1f;
    }

    public static class Controls extends DynamicsControls
    {
        private final static ControlLaw RELEASE_LAW = new LogLaw(20f, 2000f, "ms");

        public Controls() {
            super(DynamicsIds.LIMITER_ID, getString("Limiter"));
        }
        
        protected ControlLaw getRelaseLaw() { return RELEASE_LAW; }
        
		protected boolean hasGainReductionIndicator() { return true; }
    }
}


