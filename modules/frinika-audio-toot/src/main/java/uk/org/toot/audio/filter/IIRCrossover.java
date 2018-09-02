// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

public class IIRCrossover implements Crossover
{
    /**
     * @supplierCardinality 1
     * @link aggregationByValue 
     * @label high pass
     */
    private Filter hpf;

    /**
     * @supplierCardinality 1
     * @link aggregationByValue 
     * @label low pass
     */
    private Filter lpf;

    public IIRCrossover(FilterSpecification low, FilterSpecification high) {
        lpf = new BiQuadFilter(low, true);
        hpf = new BiQuadFilter(high, true);
        lpf.open(); // !!!
        hpf.open(); // !!!
    }

    public void setSampleRate(int rate) {
        lpf.setSampleRate(rate);
        hpf.setSampleRate(rate);
    }

    public void filter(float[] source, float[] lo, float[] hi, int nsamples, int chan) {
        // 2nd order,
        // 180 degree cancellation at crossover frequency if not compenstated
        lpf.filter(source, lo, nsamples, chan, false);
        hpf.filter(source, hi, nsamples, chan, false);
    }

    public void clear() {
        lpf.clear();
        hpf.clear();
    }
}
