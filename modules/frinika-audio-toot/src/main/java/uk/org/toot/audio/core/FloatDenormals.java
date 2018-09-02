// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

import static uk.org.toot.dsp.FastMath.abs;

/**
 * Provides ways to deal with FPU denormals, which, if left
 * in audio data tend to cause exceptionally high CPU uage.
 * Floating point denormals typically occur when a feedback
 * network causes an exponential decay. Eventually the value
 * becomes so small it cannot be represented efficiently by the
 * FPU.
 */
public class FloatDenormals
{
    public static final float THRESHOLD = 1e-15f;

    /**
     * Detect a denormal float (excluding zero).
     */
    public final static boolean isDenormal(float x) {
        return x != 0f && abs(x) < THRESHOLD;
    }

    /**
     * Detect a denormal (or zero) float.
     * Faster than isDenormal() if appropriate.
     */
    public final static boolean isDenormalOrZero(float x) {
        return abs(x) < THRESHOLD;
    }

    /**
     * Replace a denormal float with zero.
     */
    public final static float zeroDenorm(float x) {
        return abs(x) < THRESHOLD ? 0f : x;
    }

    /**
     * Replace denormal floats in an array with zeros.
     */
    public final static void zeroDenorms(float[] array, int len) {
        for ( int i = 0; i < len; i++ ) {
            array[i] = zeroDenorm(array[i]);
        }
    }

    /**
     * Count denormal floats in an array.
     */
    public final static int countDenorms(float[] array, int len) {
        int count = 0;
        for ( int i = 0; i < len; i++ ) {
            if ( isDenormal(array[i]) ) count++;
        }
        return count;
    }
}
