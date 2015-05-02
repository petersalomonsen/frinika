// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp.filter;

/**
 * @author st
 *
 */
public enum FilterShape {
    LPF, 	/* low pass filter */
    HPF, 	/* High pass filter */
    BPF, 	/* band pass filter */
    NOTCH, 	/* Notch Filter, unity gain passband */
    PEQ, 	/* Peaking band EQ filter */
    LSH, 	/* Low shelf filter */
    HSH, 	/* High shelf filter */
    RESONATOR // Resonator filter, unity gain peak
}
