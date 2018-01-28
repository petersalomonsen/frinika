// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.eq;

import uk.org.toot.audio.filter.Filter;

/**
 * The abstract class for serial EQ such as parametric, graphic and cut EQs.
 */
abstract public class AbstractSerialEQ extends AbstractEQ
{
    public AbstractSerialEQ(EQ.Specification spec, boolean relative) {
        super(spec, relative);
    }

    protected int filter(float[] buffer, int length, int chan) {
        if ( filters.isEmpty() || length <= 0 ) return length;
        for ( Filter filter : filters ) {
        	filter.filter(buffer, buffer, length, chan, false);
        }
        return length;
    }
}
