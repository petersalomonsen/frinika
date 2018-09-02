// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class DenormalControls extends AudioControls
{
	private final static int DENORM_PERCENT_ID = 1;
	private final static float ALPHA = 0.99f;	
	private final static ControlLaw PERCENT_LAW = new LinearLaw(0, 100, "%");
	
	private float denormAverage = 0f;
	
    public DenormalControls() {
        super(ToolIds.DENORMAL_ID, "Denorm");
        add(new DenormIndicator());
    }

    public void setDenormFactor(float factor) {
    	denormAverage = (ALPHA * denormAverage) + (1f - ALPHA) * factor;
    }
    
    private class DenormIndicator extends FloatControl
    {
		public DenormIndicator() {
			super(DENORM_PERCENT_ID, "Denorms", PERCENT_LAW, 0.1f, 0f);
			indicator = true;
		}
    	
	    @Override
		public float getValue() {
			return denormAverage * 100;
		}
    }
}
