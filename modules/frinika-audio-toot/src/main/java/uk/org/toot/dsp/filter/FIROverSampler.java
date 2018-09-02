// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp.filter;

import java.util.Arrays;

/**
 * A multi channel OverSampler making efficient use of FIR filters.
 * @author st
 */
public class FIROverSampler extends OverSampler
{
	private int nitaps, ndtaps; // number of taps
	private float[][] ix, dx;	// history, per channel
	private float[] ia, da;		// filter coefficients
	private float[] isamples;	// interpolated samples
	
	/**
	 * @param rate oversampling rate 2..64
	 * @param nchans number of channels
	 * @param iCoeffs the interpolator coefficients
	 * @param dCoeffs the decimator coefficients
	 */
	public FIROverSampler(int rate, int nchans, float[] iCoeffs, float[] dCoeffs) {
		super(rate, nchans);
		// setup interpolation
		ia = iCoeffs;
		nitaps = ia.length;
		ix = new float[NC][nitaps];
		isamples = new float[R];
		// setup decimation
		da = dCoeffs;
		ndtaps = da.length;
		dx = new float[NC][ndtaps];
	}

	/**
	 * We can optimise interpolation by noting that only 1 in R samples is
	 * non-zero and thus the product with their weight is zero and oes not
	 * need to be calculated.
	 * i.e. the output samples only comprise 1 in R multiply accumulates.
	 * Note that zero insertions cause attenuation by the factor R.
	 */
	@Override
	public float[] interpolate(float sample, int nchan) {
		assert nchan >= 0 && nchan < NC;
		float[] x = ix[nchan];
		Arrays.fill(isamples, 0);
		isamples[0] = sample * R;		// compensate for interpolation loss!
		for ( int i = 0; i < R; i++ ) {
			float y = 0;
			// shift to make space for a new sample
	        for ( int k = nitaps - 1; k > 0; k-- ) {
	            x[k] = x[k - 1];
	        }
	        // insert a new sample
			x[0] = isamples[i];
			// derive an output sample
	        for ( int k = i; k < nitaps; k += R ) {
	            y += ia[k] * x[k];
	        }
	        isamples[i] = y;
		}
		return isamples;
	}

	/**
	 * We can optimise decimation by noting that we discard all but 1 in R
	 * samples. Since a FIR is not recursive we can simply avoid calculating
	 * the output except 1 in R times.
	 * We can also do all R shifts in a single pass.
	 */
	@Override
	public float decimate(float[] samples, int nchan) {
		assert samples.length == R;
		assert nchan >= 0 && nchan < NC;
		float[] x = dx[nchan];
		// shift to make space for R new samples
        for ( int k = ndtaps - 1; k >= R; k-- ) {
            x[k] = x[k - R];
        }
        // insert R new samples
		for ( int i = 0; i < R; i++ ) {
			x[i] = samples[R-i-1];
		}
		// output decimated sample
		float y = 0;
        for ( int k = 0; k < ndtaps; k++ ) {
            y += da[k] * x[k];
        }
		return y;
	}
    
    public void clear() {
        for ( int c = 0; c < NC; c++ ) {
            for ( int i = 0; i < nitaps; i++ ) {
                ix[c][i] = 0f;
            }
            for ( int d = 0; d < ndtaps; d++ ) {
                dx[c][d] = 0;
            }
        }
    }
}
