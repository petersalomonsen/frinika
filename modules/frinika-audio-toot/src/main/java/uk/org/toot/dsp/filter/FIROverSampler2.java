// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.dsp.filter;

import java.util.Arrays;

/**
 * A multi channel OverSampler making efficient use of FIR filters.
 * We avoid rotating the history per sample by maintaining heads into the arrsys.
 * @author st
 */
public class FIROverSampler2 extends OverSampler
{
	private int nitaps, ndtaps; // number of taps, interpolation/decimation
	private int[] ih, dh;		// heads of history, per channel, interpolation/decimation
	private float[][] ix, dx;	// history, per channel, interpolation/decimation
	private float[] ia, da;		// filter coefficients, interpolation/decimation
	private float[] isamples;
	
	/**
	 * @param rate oversampling rate 2..64
	 * @param nchans number of channels
	 * @param iCoeffs the interpolator coefficients
	 * @param dCoeffs the decimator coefficients
	 */
	public FIROverSampler2(int rate, int nchans, float[] iCoeffs, float[] dCoeffs) {
		super(rate, nchans);
		// setup interpolation
		ia = iCoeffs;
		nitaps = ia.length;
		ix = new float[NC][nitaps];
        ih = new int[NC];
		isamples = new float[R];
		// setup decimation
		da = dCoeffs;
		ndtaps = da.length;
		dx = new float[NC][ndtaps];
        dh = new int[NC];
	}

	/**
	 * We optimise interpolation by noting that only 1 in R samples is
	 * non-zero and thus the product with their weight is zero and oes not
	 * need to be calculated.
	 * i.e. the output samples only comprise 1 in R multiply accumulates.
	 * Note that zero insertions cause attenuation by the factor R.
	 */
	@Override
	public float[] interpolate(float sample, int nchan) {
		assert nchan >= 0 && nchan < NC;
		float[] x = ix[nchan];
        int h = ih[nchan];
        assert h >= 0 && h < nitaps;
		Arrays.fill(isamples, 0);
		isamples[0] = sample * R;		// compensate for interpolation loss!
		int k0 = h > 0 ? h-1 : nitaps-1;	// index of the non-zero sample
		int i = 0;
		while ( i < R ) {
	        // insert a new sample
			if ( h == 0 ) h = nitaps;
			x[--h] = isamples[i];
			// derive an output sample
			float y = 0;
			int j = 0;
			int k = k0;
			while ( k < nitaps ) {
				y += ia[j] * x[k];
				j += R;
				k += R;
			}
			k -= nitaps;
			while ( j < nitaps ) {
				y += ia[j] * x[k];
				j += R;
				k += R;
			}
	        isamples[i++] = y;
		}
        ih[nchan] = h;
		return isamples;
	}

	/**
	 * We optimise decimation by noting that we discard all but 1 in R
	 * samples. Since a FIR is not recursive we can simply avoid calculating
	 * the output except 1 in R times.
	 */
	@Override
	public float decimate(float[] samples, int nchan) {
		assert samples.length == R;
		assert nchan >= 0 && nchan < NC;
        int h = dh[nchan];
		assert h >= 0 && h < ndtaps;
		float[] x = dx[nchan];
		// insert R new samples
		int i = 0;
		if ( h < R ) {
			while ( h > 0 ) {			// 0 .. R-1 iterations before wrap
				x[--h] = samples[i++];
			}
			h = ndtaps;				// wrap for predecrement
		}
		while ( i < R ) {
			x[--h] = samples[i++];		// R .. 1 iterations	
		}			
		// output decimated sample
		float y = 0;
		int j = 0;
		int k = h;
		while ( k < ndtaps ) {
			y += da[j++] * x[k++];		// 1 .. ndtaps iterations
		}
		k = 0;							// wrap (unnecessary if dh was 0)
		while ( j < ndtaps ) {
			y += da[j++] * x[k++];		// ndtaps-1 .. 0 iterations
		}
        dh[nchan] = h;
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
