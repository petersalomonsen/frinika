// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

import java.awt.Color;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.control.LogLaw;

import static uk.org.toot.misc.Localisation.*;

public class TremoloControls extends AudioControls implements TremoloProcess.Variables
{
	protected final static ControlLaw rateLaw = new LogLaw(0.5f, 10f, "Hz");
    protected final static int RATE_ID = 1;
    protected final static int DEPTH_ID = 2;

	private FloatControl rateControl;
	private FloatControl depthControl;
	
    public TremoloControls() {
		super(DynamicsIds.TREMOLO_ID, getString("Tremolo"));
		ControlColumn cc = new ControlColumn();
		cc.add(rateControl = createRateControl());
		cc.add(depthControl = createDepthControl());
		add(cc);
	}

    protected FloatControl createRateControl() {
 		return new FloatControl(RATE_ID, getString("Rate"), rateLaw, 0.01f, 3.5f);
 	}

    protected FloatControl createDepthControl() {
 		FloatControl c = new FloatControl(DEPTH_ID, getString("Depth"), LinearLaw.UNITY, 0.01f, 0.25f);
        c.setInsertColor(Color.LIGHT_GRAY);
        return c;
 	}

	public float getDepth() {
		return depthControl.getValue();
	}

	public float getRate() {
		return rateControl.getValue();
	}
	
}
