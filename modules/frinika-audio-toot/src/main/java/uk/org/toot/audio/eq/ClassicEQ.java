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
 * A classic 4 band proportional Q EQ with shelves switchable to bells
 */
public class ClassicEQ extends AbstractSerialEQ
{
    /**
     * Creates a default ClassicEQ object.
     */
    public ClassicEQ() {
        this(new Controls());
    }

    /**
     * Creates a ClassicEQ object with the specified controls.
     */
    public ClassicEQ(Controls spec) {
        super(spec, false); // true means relative levels (to input)
    }

    /**
     * The Controls for a 4 band ClassicEQ.
     */
    static public class Controls extends EQ.Controls
    {
        private final static float R = 12f;	// dB range, +/-
        private final static ControlLaw GAIN_LAW = new LinearLaw(-R, R, "dB"); // lin(dB) is log(val) !
        private final static ControlLaw Q_LAW = new LogLaw(0.4f, 4f, "");
        public Controls() {
            super(EQIds.CLASSIC_EQ_ID, getString("Classic.EQ"));
            add(new ClassicFilterControls(getString("Low"), 0,
                	FilterShape.LSH, false,
                    40f, 3000f, 80, false,
                    Q_LAW, 1f, true,
                    GAIN_LAW, 0f, false) {
                @Override
                protected boolean isProportionalQ() { return true; }                
            });
            add(new ClassicFilterControls(getString("Lo.Mid"), 4,
                	FilterShape.PEQ, true,
                    40f, 3000f, 600, false,
                    Q_LAW, 1f, true,
                    GAIN_LAW, 0f, false) {
                @Override
                protected boolean isProportionalQ() { return true; }
            });
            add(new ClassicFilterControls(getString("Hi.Mid"), 8,
                	FilterShape.PEQ, true,
                    3000f, 20000f, 4000, false,
                    Q_LAW, 1f, true,
                    GAIN_LAW, 0f, false) {
                @Override
                protected boolean isProportionalQ() { return true; }                
            });
            add(new ClassicFilterControls(getString("High"), 16,
                	FilterShape.HSH, false,
                    3000f, 20000f, 12000, false,
                    Q_LAW, 1f, true,
                    GAIN_LAW, 0f, false) {
                @Override
                protected boolean isProportionalQ() { return true; }                
            });
        }
    }
}
