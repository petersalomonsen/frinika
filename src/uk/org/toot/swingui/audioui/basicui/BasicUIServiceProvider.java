// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.basicui;

import javax.swing.JComponent;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.audio.basic.stereoImage.*;
import uk.org.toot.control.ControlSelector;
import uk.org.toot.swingui.controlui.PanelFactory;
import uk.org.toot.swingui.controlui.spi.ControlPanelServiceProvider;

import uk.org.toot.audio.id.ProviderId;

public class BasicUIServiceProvider extends ControlPanelServiceProvider
{
    public BasicUIServiceProvider() {
    	super(ProviderId.TOOT_PROVIDER_ID, "Toot Software", "Basic Audio GUI", "0.1");
    }

    public JComponent createControlPanel(CompoundControl c, int axis,
        	ControlSelector s, PanelFactory f, boolean hasBorder, boolean hasHeader) {
        if ( c instanceof StereoImageControls ) {
            return new StereoImagePanel((StereoImageControls)c, axis, s, f, hasBorder, hasHeader);
        }
        return null;
    }
}
