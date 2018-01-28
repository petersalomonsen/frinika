// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer;

public interface MixControlIds
{
    static final int GAIN = 1;
    static final int MUTE = 2;
    static final int SOLO = 3;
    static final int ROUTE = 4;
    /**
     * Represents a control for the relative balance of a stereo signal
     * between two stereo speakers.
     * Represents a control for the relative pan (left-right positioning)
     * of the signal.  The signal may be mono; the pan setting affects how
     * it is distributed by the mixer in a stereo mix.
     */
    static final int LCR = 5;
    static final int FRONT_SURROUND = 6;
}
