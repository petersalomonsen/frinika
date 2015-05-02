// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

/**
 * A <code>FloatControl</code> object provides control over a range of floating-point values.  Float controls are often
 * represented in graphical user interfaces by continuously adjustable objects such as sliders or rotary knobs.  Concrete
 * subclasses of <code>FloatControl</code> implement controls, such as gain and pan, that
 * affect a line's audio signal in some way that an application can manipulate.
 */
public class FloatControl extends LawControl
{
    /** A label for the minimum value, such as "Left." */
    private final String minLabel;

    /** A label for the maximum value, such as "Right." */
    private final String maxLabel;

    /** A label for the mid-point value, such as "Center." */
    private final String midLabel;

    /**
     * Constructs a new float control object with the given parameters.
     * The labels for the minimum, maximum, and mid-point values are set to zero-length strings.
     * @param precision the resolution or granularity of the control.
     * This is the size of the increment between discrete valid values.
     * @param initialValue the value that the control starts with when constructed
     */
    public FloatControl(int id, String name, ControlLaw law, float precision, float initialValue) {
        this(id, name, law, precision, initialValue, "", "", "");
    }

    /**
     * Constructs a new float control object with the given parameters
     * @param precision the resolution or granularity of the control.
     * This is the size of the increment between discrete valid values.
     * @param initialValue the value that the control starts with when constructed
     * @param minLabel the label for the minimum value, such as "Left" or "Off"
     * @param midLabel the label for the midpoint value, such as "Center" or "Default"
     * @param maxLabel the label for the maximum value, such as "Right" or "Full"
     */
    public FloatControl(int id, String name, ControlLaw law, float precision, float initialValue,
        	String minLabel, String midLabel, String maxLabel) {
        super(id, name, law, precision, initialValue);
        this.minLabel = ((minLabel == null) ? "" : minLabel);
        this.midLabel = ((midLabel == null) ? "" : midLabel);
        this.maxLabel = ((maxLabel == null) ? "" : maxLabel);
    }

    // a hint for the UI
    public boolean isRotary() { return true; }

    /**
     * Obtains the maximum value permitted.
     * @return the maximum allowable value
     */
    public float getMaximum() {
        return getLaw().getMaximum();
    }

    /**
     * Obtains the minimum value permitted.
     * @return the minimum allowable value
     */
    public float getMinimum() {
        return getLaw().getMinimum();
    }

    /**
     * Obtains the label for the minimum value, such as "Left" or "Off."
     * @return the minimum value label, or a zero-length string if no label	 * has been set
     */
    public String getMinLabel() {
        return minLabel;
    }

    /**
     * Obtains the label for the mid-point value, such as "Center" or "Default."
     * @return the mid-point value label, or a zero-length string if no label	 * has been set
     */
    public String getMidLabel() {
        return midLabel;
    }

    /**
     * Obtains the label for the maximum value, such as "Right" or "Full."
     * @return the maximum value label, or a zero-length string if no label	 * has been set
     */
    public String getMaxLabel() {
        return maxLabel;
    }

    /**
     * Provides a string representation of the control
     * @return a string description
     */
    public String toString() {
        return getName() + " with current value: " + getValue() + " " + getLaw().getUnits() + " (range: " + getMinimum() +
            " - " + getMaximum() + ")";
    }
}
