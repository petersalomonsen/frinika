// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp.filter;

import static java.lang.Math.*;

/**
 * Methods to obtain the frequency response of a biquad section
 * @author st
 */
public class BiQuadTools
{
    /**
     * Calculate the response in dB at the specified frequency
     * @param fn f/fs, i.e. normalised to sample rate, 0 .. 0.5
     * @param a the a coefficients
     * @param b the b coefficients
     * @return reponse in dB at the specified frequency
     */
    public static float dB(float fn, float[] a, float[] b) {
        assert a.length == 3;
        assert b.length == 3;
        return dB(fn, a[0], a[1], a[2], b[0], b[1], b[2]);
    }
    
    /**
     * Calculate the response in dB at the specified frequency
     * @param fn f/fs, i.e. normalised to sample rate, 0 .. 0.5
     * other params are the a and b coefficients
     * @return reponse in dB at the specified frequency
     */
    public static float dB(float fn, float a0, float a1, float a2,
                                     float b0, float b1, float b2) {
        float phi = (float)(2 * (1 - cos(2*PI*fn)));
        return
        10*(float)log10(sqr(b0+b1+b2) + (b0*b2*phi - (b1*(b0+b2) + 4*b0*b2))*phi) -
        10*(float)log10(sqr(a0+a1+a2) + (a0*a2*phi - (a1*(a0+a2) + 4*a0*a2))*phi);
    }

    private static float sqr(float x) { return x*x; }
    
}
