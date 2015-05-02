// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

/**
 * A linear control law.
 * Note that int values are always zero based, as required by the UI and persistence.
 * The user values have the range requested by the user
 */
public class IntegerLaw extends AbstractLaw
{
	private int res;
	
    public IntegerLaw(int min, int max, String units) {
        super(min, max, units);
        assert min >= 0;
        assert max < min + resolution;
        assert min < max;
        res = 1 + max - min; // naff but correct
    }

    /*
     * This value is  a zero based value used for UI components and persistence
     * (non-Javadoc)
     * @see uk.org.toot.control.ControlLaw#intValue(float)
     */
    public int intValue(float v) {
        return Math.round(v - min);
    }

    public float userValue(int v) {
        return v + min;
    }
    
    /**
     * We override resolution to force UI to use discrete positions.
     */
    @Override
    public int getResolution() { return res; }
}
