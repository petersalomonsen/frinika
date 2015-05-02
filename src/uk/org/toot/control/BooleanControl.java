// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.awt.Color;

import static uk.org.toot.misc.Localisation.*;

/**
 * A BooleanControl enables control of a boolean value.
 */
public class BooleanControl extends Control
{
    /** The <code>true</code> state label, such as "true" or "on." */
    private final String trueStateLabel;

    /** The <code>false</code> state label, such as "false" or "off." */
    private final String falseStateLabel;

    private Color[] stateColor = { Color.white, Color.white };

    private boolean momentary = false;

    /** The current value. */
    private boolean value;

    /**
     * Constructs a new boolean control object with the given parameters.
     * @param initialValue the initial control value
     * @param trueStateLabel the label for the state represented by <code>true</code>, such as "true" or "on."
     * @param falseStateLabel the label for the state represented by <code>false</code>, such as "false" or "off."
     */
    public BooleanControl(int id, String name, boolean initialValue, String trueStateLabel, String falseStateLabel) {
        super(id, name);
        this.value = initialValue;
        this.trueStateLabel = trueStateLabel;
        this.falseStateLabel = falseStateLabel;
    }

    /**
     * Constructs a new boolean control object with the given parameters.
     * The labels for the <code>true</code> and <code>false</code> states default to "On" and "Off".
     * @param id the id of the control represented by this float control object
     * @param initialValue the initial control value
     */
    public BooleanControl(int id, String name, boolean initialValue) {
        this(id, name, initialValue, getString("On"), getString("Off"));
    }

    public BooleanControl(int id, String name, boolean initialValue, boolean momentary) {
        this(id, name, initialValue);
        this.momentary = momentary;
    }

    public boolean isMomentary() {
        return momentary;
    }

    /**
     * Sets the current value for the control.  The default implementation simply sets the value as indicated.
     * Some controls require that their line be open before they can be affected by setting a value.
     * @param value desired new value.
     */
    public void setValue(boolean value) {
    	if ( !isEnabled() ) return; 
        if (value != this.value) {
            this.value = value;
            notifyParent(this);
        }
    }

    // default null implementation
    // override for specific momentary control action
    public void momentaryAction() {
    }

    /**
     * Obtains this control's current value.
     * @return current value.
     */
    public boolean getValue() {
        return value;
    }

    public void setStateColor(boolean state, Color color) {
        stateColor[state ? 1 : 0] = color;
    }

    public Color getStateColor(boolean state) {
        return stateColor[state ? 1 : 0];
    }

    /**
     * Obtains the label for the specified state.
     * @return the label for the specified state, such as "true" or "on"
     * for <code>true</code>, or "false" or "off" for <code>false</code>.
     */
    public String getStateLabel(boolean state) {
        return state ? trueStateLabel : falseStateLabel;
    }

    /**
     * Provides a string representation of the control
     * @return a string description
     */
    public String toString() {
        return super.toString() + " with current value: " + getStateLabel(getValue());
    }

    public String getValueString() {
        return isMomentary() ? "" : getStateLabel(getValue());
    }

    public void setIntValue(int value) { setValue(value == 0 ? false : true); }

    public int getIntValue() { return getValue() ? 1 : 0; }

    public int getWidthLimit() { return 42; }
}
