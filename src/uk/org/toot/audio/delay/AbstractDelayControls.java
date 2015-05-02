// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.*;

import java.awt.Color;

import static uk.org.toot.misc.Localisation.*;

public abstract class AbstractDelayControls extends AudioControls
{
    // reserve some id's at the top of our range for common controls
    private static final int FEEDBACK_INVERT_ID = 121;
    private static final int FEEDBACK_ID = 122;
//    private static final int MIX_INVERT_ID = 123;
    protected static final int MIX_ID = 124;
    protected static final int DELAY_FACTOR_ID = 125;

    private BooleanControl feedbackInvertControl;
    private FloatControl feedbackControl;
    private FloatControl mixControl;
    
    private float feedback, mix = 1f;

    public AbstractDelayControls(int id, String name) {
        super(id, name);
    }

    protected void derive(Control c) {
    	switch ( c.getId() ) {
    	case FEEDBACK_INVERT_ID:
    	case FEEDBACK_ID:
    		feedback = isFeedbackInverted() ? -feedbackControl.getValue() :
    										   feedbackControl.getValue();
    		break;
    	case MIX_ID: mix = mixControl.getValue(); break;
    	case DELAY_FACTOR_ID: break; // handles itself
    	}
    	
    }
    
    protected boolean isFeedbackInverted() {
        if ( feedbackInvertControl == null ) return false;
        return feedbackInvertControl.getValue();
    }

    protected BooleanControl createFeedbackInvertControl() {
        feedbackInvertControl = new BooleanControl(FEEDBACK_INVERT_ID, getString("Invert"), false);
        feedbackInvertControl.setStateColor(true, Color.orange);
        return feedbackInvertControl;
    }

    protected FloatControl createFeedbackControl() {
 		feedbackControl = new FloatControl(FEEDBACK_ID, getString("Resonance"), LinearLaw.UNITY, 0.01f, 0f);
 		derive(feedbackControl); // assumes invert control already created if required
        return feedbackControl;
 	}

    protected FloatControl createMixControl() {
        mixControl = new MixControl();
        return mixControl;
    }

    protected ControlColumn createCommonControlColumn(boolean withInverts) {
        ControlColumn g = new ControlColumn();
        if ( withInverts ) g.add(createFeedbackInvertControl());
        g.add(createFeedbackControl());
        g.add(createMixControl());
        return g;
    }

    public float getFeedback() {
        return feedback;
    }

    public float getMix() {
        return mix;
    }

    static public class MixControl extends FloatControl
    {
        private static final String[] presetNames = {
            getString("Dry"), getString("Wet")
        };

        public MixControl() {
            super(MIX_ID, getString("Mix"), LinearLaw.UNITY, 0.01f, 0.5f);
            setInsertColor(Color.white);
        }

        public String[] getPresetNames() {
        	return presetNames;
    	}

    	public void applyPreset(String name) {
            if ( getString("Dry").equals(name) ) {
                setValue(0f);
            } else if ( getString("Wet").equals(name) ) {
                setValue(1f);
            }
    	}
    }
}
