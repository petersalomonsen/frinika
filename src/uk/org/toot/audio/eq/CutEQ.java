// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.eq;

import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.control.LogLaw;
import uk.org.toot.dsp.filter.FilterShape;

import static uk.org.toot.misc.Localisation.*;

/**
 * A serial cut-only EQ to provide band-limiting effects.
 * Just remember the Low filter is HighPass and the High filter is LowPass.
 */
public class CutEQ extends AbstractSerialEQ
{
    /**
     * Create a CutEQ with default controls.
     */
    public CutEQ() {
        this(new Controls());
    }

    /**
     * Create a CutEQ with the specified controls.
     */
    public CutEQ(Controls controls) {
        super(controls, true);
    }

    /**
     * The controls for a CutEQ.
     */
    public static class Controls extends EQ.Controls
    {
        private final static ControlLaw GAIN_LAW = new LinearLaw(0, 0, "dB"); // lin(dB) is log(val) !
        private final static float Q = 1.1f;
        private final static ControlLaw Q_LAW = new LogLaw(Q, Q, "");

        public Controls() {
            super(EQIds.CUT_EQ_ID, getString("Cut.EQ"));
            ControlColumn g = new ControlColumn();
            g.add(new ClassicFilterControls("High", 4, // !!! !!!
                	FilterShape.LPF, true,
                    40f, 12000f, 12000f, false,
                    Q_LAW, Q, true,
                    GAIN_LAW, 0, true));
            g.add(new ClassicFilterControls("Low", 0, // !!! !!!
                	FilterShape.HPF, true,
                    20f, 5000f, 20f, false,
                    Q_LAW, Q, true,
                    GAIN_LAW, 0, true));
            add(g);
        }
    }
}
