// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class NaNTectorControls extends AudioControls
{
	private final static int NAN_PERCENT_ID = 1;
	private final static float ALPHA = 0.99f;
	private final static ControlLaw PERCENT_LAW = new LinearLaw(0, 100, "%");
	
	private float nanAverage = 0f;
	
	public NaNTectorControls() {
		super(ToolIds.NAN_TECTOR_ID, "NaN?");
		add(new NaNIndicator());
	}
	
    public void setNaNFactor(float factor) {
    	nanAverage = (ALPHA * nanAverage) + (1f - ALPHA) * factor;
    }
    
    private class NaNIndicator extends FloatControl
    {
		public NaNIndicator() {
			super(NAN_PERCENT_ID, "NaN", PERCENT_LAW, 0.1f, 0f);
			indicator = true;
		}
    	
	    @Override
		public float getValue() {
			return nanAverage * 100;
		}
    }
}
