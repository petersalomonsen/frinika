// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import java.awt.Color;
import java.util.List;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.EnumControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.control.LogLaw;

import static uk.org.toot.misc.Localisation.*;

public class PhaserControls extends AudioControls implements PhaserProcess.Variables
{
	protected final static ControlLaw rateLaw = new LogLaw(0.1f, 2f, "Hz");
    protected final static int RATE_ID = 1;
    protected final static int DEPTH_ID = 2;
    protected final static int FEEDBACK_ID = 3;
    protected final static int STAGES_ID = 0;

	private FloatControl rateControl;
	private FloatControl depthControl;
	private FloatControl feedbackControl;
    private StagesControl stagesControl;
	private float rate, depth, feedback;
    private int stages = 4;
	
    public PhaserControls() {
		super(DelayIds.PHASER_ID, getString("Phaser"));
		ControlColumn cc = new ControlColumn();
        cc.add(stagesControl = new StagesControl(stages));
		cc.add(rateControl = createRateControl());
		cc.add(depthControl = createDepthControl());
		cc.add(feedbackControl = createFeedbackControl());
		add(cc);
		derive(rateControl);
		derive(depthControl);
		derive(feedbackControl);
        derive(stagesControl);
	}

    protected void derive(Control c) {
    	switch ( c.getId() ) {
    	case RATE_ID: rate = rateControl.getValue(); break;
    	case DEPTH_ID: depth = depthControl.getValue(); break;
    	case FEEDBACK_ID: feedback = feedbackControl.getValue(); break;
        case STAGES_ID: stages = stagesControl.getStages(); break;
    	}
    }
    
    protected FloatControl createRateControl() {
 		return new FloatControl(RATE_ID, getString("Rate"), rateLaw, 0.01f, 0.5f);
 	}

    protected FloatControl createDepthControl() {
 		FloatControl c = new FloatControl(DEPTH_ID, getString("Depth"), LinearLaw.UNITY, 0.01f, 1f);
        c.setInsertColor(Color.LIGHT_GRAY);
        return c;
 	}

    protected FloatControl createFeedbackControl() {
 		return new FloatControl(FEEDBACK_ID, getString("Resonance"), LinearLaw.UNITY, 0.01f, 0f);
 	}

	public float getDepth() {
		return depth;
	}

	public float getFeedback() {
		return feedback;
	}
	
	public float getRate() {
		return rate;
	}

    public int getStages() {
        return stages;
    }
    
    private static class StagesControl extends EnumControl
    {
        private static List<String> values;
        
        static {
            values = new java.util.ArrayList<String>();
            values.add("2");
            values.add("4");
            values.add("6");
            values.add("8");
            values.add("10");
            values.add("12");
        }

        public StagesControl(int stages) {
            super(STAGES_ID, getString("Stages"), String.valueOf(stages));
        }

        @Override
        public List getValues() {
            return values;
        }
        
        public int getStages() {
            return Integer.parseInt((String)getValue());
        }
    }
}
