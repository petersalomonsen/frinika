// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

import static uk.org.toot.audio.core.FloatDenormals.*;

/**
 * A mono bicubic filter
 * @author st
 *
 */
public class ToneStackSection
{
	private float b0, b1, b2, b3;
    private float a1, a2, a3;
    private float x1, x2, x3, y1, y2, y3;
    private float gain;

    public void filter(float[] buffer, int length) {
        for ( int i = 0; i < length; i++) {
            buffer[i] = filter(buffer[i]);
        }
    }
    
    public float filter(float sample) {
        float y = b0 * sample + b1 * x1 + b2 * x2 + b3 * x3 -
                                a1 * y1 - a2 * y2 - a3 * y3;

        if ( y != y ) { 
            y = y1; // prevent NaN history lock when oversampled
        }
        y = zeroDenorm(y); // anti-denormal helps

        /* shift x2 to x3, x1 to x2, sample to x1 */
        x3 = x2;
        x2 = x1;
        x1 = sample;

        /* shift y2 to y3, y1 to y2, result to y1 */
        y3 = y2;
        y2 = y1;
        y1 = y;

        return y * gain;
    }
    
    // should be called synchronously with filter()
    public void updateCoefficients(Coefficients c) {
    	b0 = c.b0;
    	b1 = c.b1;
    	b2 = c.b2;
    	b3 = c.b3;
    	a1 = c.a1;
    	a2 = c.a2;
    	a3 = c.a3;
        gain = c.gain;
    }
    
    public void clear() {
        x1 = x2 = x3 = 0;
        y1 = y2 = y3 = 0;
    }
    
    public static class Coefficients
    {
    	public float b0, b1, b2, b3;
        public float a1, a2, a3;
        public float gain;
    }
}
