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
 * A parametric EQ.
 */
public class ParametricEQ extends AbstractSerialEQ
{
    /**
     * Creates a default ParametricEQ object.
     */
    public ParametricEQ() {
        this(new Controls());
    }

    /**
     * Creates a ParemetricEQ object with the specified controls.
     */
    public ParametricEQ(Controls spec) {
        super(spec, false); // true means relative levels (to input)
    }

    /**
     * The Controls for a 4 band ParametricEQ.
     */
    static public class Controls extends EQ.Controls
    {
        private final static float R = 15f;	// dB range, +/-
        private final static ControlLaw GAIN_LAW = new LinearLaw(-R, R, "dB"); // lin(dB) is log(val) !
        private final static ControlLaw Q_LAW = new LogLaw(0.5f, 10f, "");
        public Controls() {
            super(EQIds.PARAMETRIC_EQ_ID, getString("Parametric.EQ"));
            add(new ClassicFilterControls(getString("Low"), 0,
                	FilterShape.LSH, true,
                    40f, 3000f, 80, false,
                    Q_LAW, 1f, true,
                    GAIN_LAW, 0f, false));
            add(new ClassicFilterControls(getString("Lo.Mid"), 4,
                	FilterShape.PEQ, true,
                    40f, 3000f, 600, false,
                    Q_LAW, 1f, false,
                    GAIN_LAW, 0f, false));
            add(new ClassicFilterControls(getString("Hi.Mid"), 8,
                	FilterShape.PEQ, true,
                    3000f, 20000f, 4000, false,
                    Q_LAW, 1f, false,
                    GAIN_LAW, 0f, false));
            add(new ClassicFilterControls(getString("High"), 16,
                	FilterShape.HSH, true,
                    3000f, 20000f, 12000, false,
                    Q_LAW, 1f, true,
                    GAIN_LAW, 0f, false));
        }
    }
}
