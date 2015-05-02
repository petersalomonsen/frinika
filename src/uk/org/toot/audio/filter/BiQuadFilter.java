// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

import static uk.org.toot.audio.core.FloatDenormals.*;

/* Simple implementation of Biquad filters -- Tom St Denis
 *
 * Based on the work

Cookbook formulae for audio EQ biquad filter coefficients
---------------------------------------------------------
by Robert Bristow-Johnson

 * Enjoy.
 *
 * Tom St Denis -- http://tomstdenis.home.dhs.org
 */

public class BiQuadFilter extends AbstractFilter 
{
    private double a0;
    private double a1;
    private double a2;
    private double a3;
    private double a4; // coefficients (faster than array)

    public BiQuadFilter(FilterSpecification spec, boolean relative) {
        super(spec, relative);
    }

    public void filter(float[] buffer, float[] mixBuffer, int length, int chan, boolean doMix) {
        BiQuadState s = (BiQuadState)getState(chan);
        float sample;
        float y;
        for (int index = 0; index < length; index++) {
            sample = buffer[index];
        	y = (float)(a0 * sample + a1 * s.x1 + a2 * s.x2 -
                 					  a3 * s.y1 - a4 * s.y2);

	        y = zeroDenorm(y); // anti-denormal helps

		    /* shift x1 to x2, sample to x1 */
    	    s.x2 = s.x1;
        	s.x1 = sample;

		    /* shift y1 to y2, result to y1 */
    	    s.y2 = s.y1;
        	s.y1 = y;

            if (!doMix) {
	            mixBuffer[index] = y;
            } else {
            	mixBuffer[index] += (amplitudeAdj * y);
            }
        }
    }

	protected void updateFilterCoefficients() {
        // synchronize coefficients from design
        BiQuadDesign d = (BiQuadDesign)getDesign();
        a0 = d.a[0];
        a1 = d.a[1];
        a2 = d.a[2];
        a3 = d.a[3];
        a4 = d.a[4];
//    	System.out.println("coeffs: "+a0+", "+a1+", "+a2+", "+a3+", "+a4);
    }

    protected FilterDesign createDesign(FilterSpecification spec) {
        return new BiQuadDesign(spec);
    }

    protected State createState() {
        return new BiQuadState();
    }

	/* this holds the state required to update samples thru the filter */
    static private class BiQuadState implements State
    {
        public float x1;
        public float x2;
        public float y1;
        public float y2; // state

        public BiQuadState() {
            clear();
        }

        public void clear() {
            x1 = 0;
            x2 = 0;
            y1 = 0;
            y2 = 0;
        }
    }
}
