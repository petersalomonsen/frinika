// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

/**
 * An abstract control law.
 */
public abstract class AbstractLaw implements ControlLaw
{
    protected static final int resolution = 1024;
    protected float min;
    protected float max;
    protected String units;

    protected AbstractLaw(float min, float max, String units) {
        this.min = min;
        this.max = max;
        this.units = units;
    }

    public int getResolution() { return resolution; }

    public float getMinimum() { return min; }
    public float getMaximum() { return max; }
    public String getUnits() { return units; }
}
