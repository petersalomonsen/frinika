/* Copyright (C) 2006 Steve Taylor (toot.org.uk) */

package uk.org.toot.audio.filter;

public interface Crossover
{
    void filter(float[] source, float[] lo, float[] hi, int nsamples, int chan);

    void clear();

    /**
     * Called when the sample rate changes.
     */
    void setSampleRate(int sampleRate);
}

