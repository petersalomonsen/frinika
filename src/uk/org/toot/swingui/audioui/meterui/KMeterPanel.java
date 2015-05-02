// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.meterui;

import uk.org.toot.audio.meter.MeterControls;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.swingui.controlui.*;

public class KMeterPanel extends CompoundControlPanel
{
    private MeterControls controls;
    private static MeterPanelFactory factory = new MeterPanelFactory();

    public KMeterPanel(MeterControls mc, int axis) {
        // we use our own panel factory to display specialised meter indicators
        super(mc, axis, null, factory, true, true); // null selector selects all
        controls = mc;
        if ( controls.getChannelFormat() != ChannelFormat.STEREO ) {
            System.out.println("WARNING: MeterPanel only handling first 2 channels");
        }
    }

    public void dispose() {
        removeAll(); // achieves nothing :( !!! !!!
    }

    // experimental, seems to work
    public void setMeterControls(MeterControls mc) {
        controls = mc;
        recreate(controls);
    }
}
