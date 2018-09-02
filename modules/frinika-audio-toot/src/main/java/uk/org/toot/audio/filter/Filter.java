// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.filter;

public interface Filter
{
    /**
     * Open this Filter, acquire resources, addObservers etc.
     */
    void open();

    /**
     * Close this Filter, release resources, deleteObservers etc.
     */
    void close();

    /**
     * Filter the source signal to the destination signal.
     * If the source and dest are the same a simple in-place filter should result.
     * If the source and destination are different the Filter should add (or
     * subtract) a portion of the source to the destination. This portion
     * should represent the contribution to an equaliser comprising multiple
     * such filters.
     * @param chan permits the Filter to use different States for different
     * logical filter channels.
     */
    void filter(float[] source, float[] dest, int length, int chan, boolean mix);

    /**
     * Clear the filter's states (i.e. the delay taps).
     * This is to avoid glitches in non-contiguous filtering.
     */
    void clear();

    /**
     * Called when the sample rate changes.
     */
    void setSampleRate(int sampleRate);

    interface State {
        void clear();
    }
}
