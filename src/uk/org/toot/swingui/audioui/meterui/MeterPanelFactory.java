// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.meterui;

import uk.org.toot.control.*;
import uk.org.toot.audio.meter.MeterControls;
import javax.swing.*;
import uk.org.toot.swingui.audioui.AudioPanelFactory;

public class MeterPanelFactory extends AudioPanelFactory
{
    public JComponent createComponent(Control c, int axis, boolean hasHeader) {
        if ( c instanceof MeterControls.MeterIndicator ) {
            // assume vertical for now
            return new KMeterIndicatorPanel((MeterControls.MeterIndicator)c);
        } else if ( c instanceof MeterControls.OverIndicator ) {
            return new MeterOversPanel(c);
        }
		return super.createComponent(c, axis, hasHeader);
    }
}


