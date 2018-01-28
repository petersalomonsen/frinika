// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

public interface DelayVariables {
    /**
     * Traditional Chorus, Flanger, Phaser and ADT etc. will return low values
     * Unmodulated delays may tend to have higher values.
     * This will only be checked once, before the taps are used.
     */
    float getMaxDelayMilliseconds();

    boolean isBypassed();
}
