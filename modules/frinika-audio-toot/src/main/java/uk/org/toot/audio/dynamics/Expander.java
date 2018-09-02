// Copyright (C) 2006, 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import static uk.org.toot.misc.Localisation.*;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LinearLaw;

public class Expander extends DynamicsProcess
{
    public Expander(DynamicsVariables vars) {
        super(vars, false); // RMS, not peak
    }

    // expand
    protected float function(float value) {
/*        if ( value < threshold ) {
        	float valdB = (float)KVolumeUtils.lin2log(value);
            float underdB = thresholddB - valdB;
            copied from Compressor !!! needs changing
            float gainReductiondB = -overdB * (ratio - 1f) / ratio;
            float gain = (float)TVolumeUtils.log2lin(gainReductiondB);
            return gain;
        } */
        return 1f;
    }

    public static class Controls extends DynamicsControls
    {
    	private final static ControlLaw THRESH_LAW = new LinearLaw(-60f, 20f, "dB");

    	public Controls() {
            super(DynamicsIds.EXPANDER_ID, getString("Expander"));
        }

        protected ControlLaw getThresholdLaw() { return THRESH_LAW; }

	    protected boolean hasRatio() { return true; }
    }
}
