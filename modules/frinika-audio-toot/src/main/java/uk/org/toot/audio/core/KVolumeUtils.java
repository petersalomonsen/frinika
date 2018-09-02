// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

import org.tritonus.share.sampled.TVolumeUtils;

/**
 * KVolumeUtils provides conversion methods between linear and logarithmic
 * (dB). It delegates to Tritonus' VolumeUtils after applying a correction
 * such that 0dB is then KdB below fullscale.
 * K is fixed at 20dB below fullscale for this implementation which
 * perhaps needs refactoring so that different K's can be used in
 * different circumstances.
 */
public class KVolumeUtils {

    /**
     * Assumes dB(K) = dB(FS) - 20
     */
	private final static double K = 20;

    public static double lin2log(double dLinear) {
        return TVolumeUtils.lin2log(dLinear) + K;
    }

    public static double log2lin(double dLogarithmic) {
        return TVolumeUtils.log2lin(dLogarithmic - K);
    }
}
