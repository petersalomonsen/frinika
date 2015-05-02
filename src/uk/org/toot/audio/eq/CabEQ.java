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
 * A simple EQ to simulate the frequency response of guitar/bass speaker cabinets.
 * The main bandlimiting effect is a second order highpass and a fourth order lowpass.
 * These resonant extremes may be over or under damped.
 * Various notches, bumps and ripples may also be present in the passband.
 * A notch around 400Hz is typical for some drive units.
 */
public class CabEQ extends AbstractSerialEQ
{
    /**
     * Create a CabEQ with default controls.
     */
    public CabEQ() {
        this(new Controls());
    }

    /**
     * Create a CabEQ with the specified controls.
     */
    public CabEQ(Controls controls) {
        super(controls, true);
    }

    /**
     * The controls for a CabEQ.
     */
    public static class Controls extends EQ.Controls
    {
        private final static ControlLaw GAIN_LAW = new LinearLaw(-15, 0, "dB"); // lin(dB) is log(val) !
//        private final static ControlLaw Q_LAW = new LogLaw(0.5f, 10f, "");
        private final static ControlLaw Q_LAW_2 = new LogLaw(0.5f, 2f, "");

        public Controls() {
            super(EQIds.CAB_EQ_ID, getString("Cab.EQ"));
            add(new ClassicFilterControls("Thud", 0,
                	FilterShape.HPF, true,
                    80f, 200f, 120f, false,
                    Q_LAW_2, 1.5f, false,
                    GAIN_LAW, 0, true));
/*            add(new ClassicFilterControls(getString("Notch"), 4,
                	FilterShape.PEQ, true,
                    400f, 1600f, 800, false,
                    Q_LAW, 10f, true,
                    GAIN_LAW, -6f, false)); */
            add(new ClassicFilterControls("Pres", 8,
                	FilterShape.LPF, true,
                    2000f, 4500f, 4000f, false,
                    Q_LAW_2, 1.5f, false,
                    GAIN_LAW, 0, true) {
            			public boolean is4thOrder() { return true; }
            	}
            );
        }
    }
}
