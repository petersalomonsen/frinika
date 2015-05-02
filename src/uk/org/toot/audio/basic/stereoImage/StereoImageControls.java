// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.basic.stereoImage;

import java.awt.Color;
import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.BooleanControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LinearLaw;

import static uk.org.toot.misc.Localisation.*;
import static uk.org.toot.audio.basic.BasicIds.STEREO_IMAGE;

/**
 * Implements stereo image controls and obeys the process variables contract
 * @author st
 *
 */
public class StereoImageControls extends AudioControls
    implements StereoImageProcessVariables
{
    public static final int LR_SWAP = 1;
    public static final int LR_WIDTH = 2;

    private BooleanControl swapControl;
    private FloatControl widthControl;
    
    private float width;
    private boolean swapped;

    private final static ControlLaw WIDTH_LAW = new LinearLaw(0f, 2f, "");

    public StereoImageControls() {
        super(STEREO_IMAGE, getString("Stereo"));
        widthControl = new FloatControl(LR_WIDTH, getString("Width"), WIDTH_LAW, 0.01f, 1f);
        widthControl.setInsertColor(Color.WHITE);
        add(widthControl);
        swapControl = new BooleanControl(LR_SWAP, getString("Swap"), false); // initially not swapped
        swapControl.setStateColor(true, Color.red);
        add(swapControl);
        derive(widthControl);
        derive(swapControl);
    }

    @Override
    protected void derive(Control c) {
    	switch ( c.getId() ) {
    	case LR_WIDTH: width = -(widthControl.getValue()-1); break;
    	case LR_SWAP: swapped = swapControl.getValue(); break; 
    	}
    }
    public float getWidthFactor() {
        return width;
    }

    public boolean isLRSwapped() {
        return swapped;
    }
}
