// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.dynamics;

public class DynamicsControlIds
{
    public static final int KEY = 0;                    // all
    public static final int THRESHOLD = 1;              // all
    public static final int RATIO = 2;                  // compress / expand
    public static final int KNEE = 3;                   // compress / limit / expand
    public static final int ATTACK = 4;                 // all
    public static final int HOLD = 5;                   // gate
    public static final int RELEASE = 6;                // all
    public static final int GAIN = 7;                   // compress / limit
    public static final int DRY_GAIN = 8;               // compress / limit
    public static final int DEPTH = 9;                  // gate
    public static final int CROSSOVER_FREQUENCY = 10;   // multiband
    public static final int HYSTERESIS = 11;            // gate
    public static final int RMS = 12;                   // compress / expand
}
