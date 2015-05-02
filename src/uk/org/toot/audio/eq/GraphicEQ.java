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
 * An octave graphic EQ.
 */
public class GraphicEQ extends AbstractSerialEQ {

    public GraphicEQ() {
        this(new Controls());
    }

    /**
     * Creates a new GraphicEQ object.
     */
    public GraphicEQ(int flow) {
        this(new Controls(flow));
    }

    public GraphicEQ(Controls c) {
        super(c, false); // true means levels are relative (to input)
    }

    /**
     * The controls for a GraphicEQ.
     */
    static public class Controls extends EQ.Controls
    {
        private final static ControlLaw GAIN_LAW = new LinearLaw(-12, 12, "dB"); // lin(dB) is log(val) !
        private static final float Q = 1.4f;
        private final static ControlLaw Q_LAW = new LogLaw(Q, Q, "");

        /**
         * Create default controls with ISO standard frequencies.
         */
        public Controls() {
            this(50);
        }

        public Controls(int flow) {
            super(EQIds.GRAPHIC_EQ_ID, getString("Graphic.EQ"));
            int fc = flow;
            int id = 1;
            while ( fc < 20001 ) {
                add(new ClassicFilterControls(String.valueOf(fc), id,
                    	FilterShape.PEQ, true,
                        fc, fc, fc, true,
                        Q_LAW, Q, true,
                        GAIN_LAW, 0f, false));
                fc += fc;
                id += 4; // !!! !!! 4 FFS!
            }
        }
    }
}
