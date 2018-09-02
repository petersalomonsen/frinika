// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

public class FilterTools
{
    private static float LN2 = (float)Math.log(2);

    public static float getDampingFactor(float resonance) {
        return 1f / resonance;
    }

    public static float getHzBandwidth(float frequency, float resonance) {
        return frequency / resonance;
    }

    public static float getOctaveBandwidth(float resonance) {
        double a = 1.0 / (2 * resonance);
        double b = Math.sqrt((a * a) + 1);
        return (float)(2 * Math.log(a + b) / LN2);
    }
}
