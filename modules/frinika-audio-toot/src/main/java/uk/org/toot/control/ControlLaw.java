// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

/**
 * A 'control law' with bidirectional user/control value scaling.
 * The UI control values are integer based,
 * The user values are float based.
 *
 * A Law may be used either by a UI or a Control as appropriate.
 */
public interface ControlLaw
{
    /**
     * return the integer value for user value
     * valid integers are 0 .. resolution-1
     */
    int intValue(float userVal);

    /**
     * return the user value for the integer value
     * valid integers are 0 .. resolution-1
     */
    float userValue(int intVal);

    /**
     * return the resolution of the integer value
     * valid integers are 0 .. resolution-1
     */
    int getResolution();

    float getMinimum();

    float getMaximum();

    /**
     * return the user units string
     */
    String getUnits();
}


