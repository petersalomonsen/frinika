// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.basicui;

import java.awt.Color;
import uk.org.toot.audio.basic.stereoImage.StereoImageControls;
import uk.org.toot.swingui.controlui.*;
import uk.org.toot.swingui.audioui.AudioCompoundControlPanel;
import uk.org.toot.control.ControlSelector;

public class StereoImagePanel extends AudioCompoundControlPanel
{
    public StereoImagePanel(StereoImageControls controls, int axis,
        	ControlSelector controlSelector, PanelFactory panelFactory, boolean hasBorder, boolean hasHeader) {
        super(controls, axis, controlSelector, panelFactory, hasBorder, hasHeader);
        setBackground(Color.magenta);
    }
}
