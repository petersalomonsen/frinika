// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import static uk.org.toot.misc.Localisation.*;
import static uk.org.toot.dsp.FastMath.pow;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LogLaw;

public class Compressor extends DynamicsProcess
{
    protected float ratio2;
    protected float inverseThreshold;

    public Compressor(DynamicsVariables vars) {
		super(vars, false); // RMS, not peak
    }

    @Override
    protected void cacheProcessVariables() {
        super.cacheProcessVariables();
        ratio2 = 1f - vars.getInverseRatio();
        inverseThreshold = vars.getInverseThreshold();
    }
    
    protected float function(float value) {
        if ( value > threshold ) {
            return 1f / (float)pow(value * inverseThreshold, ratio2);
        }
        return 1f;
    }
    
    public static class Controls extends DynamicsControls
    {
        private final static ControlLaw ATTACK_LAW = new LogLaw(10f, 100f, "ms");

        public Controls() {
            super(DynamicsIds.COMPRESSOR_ID, getString("Compressor"));
        }

        public Controls(String name, int idOffset) {
            super(DynamicsIds.COMPRESSOR_ID, name, idOffset);
        }

        protected ControlLaw getAttackLaw() { return ATTACK_LAW; }
        
        protected boolean hasGainReductionIndicator() { return true; }

	    protected boolean hasRatio() { return true; }

	    protected boolean hasGain() { return true; }
	    
        protected boolean hasDryGain() { return true; }

        protected boolean hasKey() { return true; }
    }
}
