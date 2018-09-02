// Copyright (C) 2009 Steve Taylor.
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
 * A formant filter.
 */
public class FormantEQ extends AbstractParallelEQ
{
    /**
     * Creates a default FormantFilter object.
     */
    public FormantEQ() {
        this(new Controls());
    }

    /**
     * Creates a FormantFilter object with the specified controls.
     */
    public FormantEQ(Controls spec) {
        super(spec, false); // false means levels are absolute
    }

    /**
     * The Controls for a 4 band formant filter.
     */
    static public class Controls extends EQ.Controls
    {
        private final static float R = 15f;	// dB range, +/-
        private final static ControlLaw GAIN_LAW = new LinearLaw(-R, R, "dB"); // lin(dB) is log(val) !
        private final static ControlLaw Q_LAW = new LogLaw(0.5f, 10f, "");

        public Controls() {
            super(EQIds.FORMANT_EQ_ID, getString("Formant.EQ"));
            add(new ClassicFilterControls("1", 0,
                	FilterShape.BPF, true,
                    125f, 500f, 250, false,
                    Q_LAW, 2f, false,
                    GAIN_LAW, 0f, false));
            add(new ClassicFilterControls("2", 4,
                	FilterShape.BPF, true,
                    250f, 1000f, 500, false,
                    Q_LAW, 2f, false,
                    GAIN_LAW, 0f, false));
            add(new ClassicFilterControls("3", 8,
                	FilterShape.BPF, true,
                    500f, 2000f, 1000, false,
                    Q_LAW, 2f, false,
                    GAIN_LAW, 0f, false));
            add(new ClassicFilterControls("4", 16,
                	FilterShape.BPF, true,
                    1000f, 4000f, 2000, false,
                    Q_LAW, 2f, false,
                    GAIN_LAW, 0f, false));
        }
    }
}
