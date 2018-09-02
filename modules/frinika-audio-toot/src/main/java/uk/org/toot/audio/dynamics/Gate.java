// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import static uk.org.toot.misc.Localisation.*;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LinearLaw;

public class Gate extends DynamicsProcess
{
    private int holdCount = 0;
    private int hold;
    private boolean open = false;
    private float depth;
    private float hysteresis;

    public Gate(DynamicsVariables vars) {
        super(vars, false); // rms detection
    }

    @Override
    protected void cacheProcessVariables() {
        super.cacheProcessVariables();
        depth = vars.getDepth();
        hold = vars.getHold();
        hysteresis = vars.getHysteresis();
    }
    
    // gate
    protected float function(float value) {
        if ( open ) {
            if ( value < threshold * hysteresis ) {
                if ( holdCount > 0 ) {
                    holdCount -= 1;
                    return 1f; // hold open
                }
                open = false;
            }
        } else { // !wasOpen
            if ( value > threshold ) {
                holdCount = hold;
                open = true;
            }
        }
        return open ? 1f : depth;
    }

    public static class Controls extends DynamicsControls
    {
    	private final static ControlLaw THRESH_LAW = new LinearLaw(-80f, 20f, "dB");

        public Controls() {
            super(DynamicsIds.GATE_ID, getString("Gate"));
        }

        protected ControlLaw getThresholdLaw() { return THRESH_LAW; }

	    protected boolean hasHold() { return true; }

	    protected boolean hasDepth() { return true; }

        protected boolean hasHysteresis() { return true; }
    }
}
