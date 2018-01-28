// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.eq;

import uk.org.toot.audio.filter.Filter;
import java.util.Arrays;

/**
 * The abstract class for parallel EQ such as formant EQ.
 */
abstract public class AbstractParallelEQ extends AbstractEQ
{
    protected float[] mixBuffer = null;

    public AbstractParallelEQ(EQ.Specification spec, boolean relative) {
        super(spec, relative);
    }

    protected int filter(float[] buffer, int length, int chan) {
        if ( filters.isEmpty() || length <= 0 ) return length;
        // Realloc buffer as required
        if ( mixBuffer == null || mixBuffer.length != length ) {
            mixBuffer = new float[length];
        }
        if ( relative ) {
        	// Move samples into summation buffer for relative level processing
        	System.arraycopy(buffer, 0, mixBuffer, 0, length);
        } else {
        	Arrays.fill(mixBuffer, 0); // clear summation buffer
        }
        // Apply the filters
        for ( Filter filter : filters ) {
        	filter.filter(buffer, mixBuffer, length, chan, true);
        }
        // move samples back from summation buffer
        System.arraycopy(mixBuffer, 0, buffer, 0, length);
        return length;
    }
}
