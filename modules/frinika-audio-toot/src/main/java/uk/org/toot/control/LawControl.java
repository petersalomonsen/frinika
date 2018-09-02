// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.awt.Color;

/**
 * A LawControl enables control of a float value using the specified control law.
 */
public class LawControl extends Control
{
    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    private static InsertColorer colorer = new DefaultInsertColorer();

    /**
     * @link aggregationByValue
     * @supplierCardinality 1 
     */
    private ControlLaw law;

    /** The current value. */
    private float value;

    /** The control's precision. */
    private float precision;

    private Color insertColor;
    
    private String valueFormat;

    public LawControl(int id, String name, ControlLaw law, float precision, float initialValue) {
        super(id, name);
        this.law = law;
        this.precision = precision;
        this.value = initialValue;
        assert initialValue >= Math.min(law.getMinimum(), law.getMaximum());
        assert initialValue <= Math.max(law.getMinimum(), law.getMaximum());
        valueFormat = "%1$."+calculateDecimalPlaces()+"f %2$s";
        insertColor = colorer.getColor(this);
    }

    protected int calculateDecimalPlaces() {
        return law.getMaximum() - law.getMinimum() > 100f ? 1 : 2;
    }

    public ControlLaw getLaw() { return law; }

    /**
     * Obtains this control's current value.
     * @return the current value
     */
    public float getValue() {
        return value;
    }

    public String getValueString() {
        return String.format(valueFormat, getValue(), getLaw().getUnits());
    }

    /**
     * Sets the current value for the control.  The default implementation
     * simply sets the value as indicated.  If the value indicated is greater
     * than the maximum value, or smaller than the minimum value, an IllegalArgumentException is thrown.
     * @param value the desired new value
     * @throws IllegalArgumentException if the value indicated does not fall within the allowable range
     */
    public void setValue(float value) {
    	if ( !isEnabled() ) return; 
        /* in Trim AutomationMode we want to
           add the value/null delta
           to the value set by setIntValue.
           But, we don't want here to know about automation or trim mode
           we want here to just support the requirement generally.
           Maybe reconsider the requirements, separate trim slider?
         */
        this.value = value;
        notifyParent(this);
    }

    /**
     * Obtains the precision or granularity of the control, in the units that the control measures.
     * The precision is the size of the increment between discrete valid values
     * for this control, over the set of supported floating-point values.
     * @return the control's precision
     */
    public float getPrecision() {
        return precision;
    }

    public Color getInsertColor() {
        return insertColor;
    }

    public void setInsertColor(Color insertColor) {
        this.insertColor = insertColor;
    }

    public void setIntValue(int value) {
        setValue(getLaw().userValue(value));
    }

    public int getIntValue() {
        return getLaw().intValue(getValue());
    }

    public String[] getPresetNames() {
        return null;
	}

	public void applyPreset(String name) {
	}
	
	public static void setInsertColorer(InsertColorer aColorer) {
		colorer = aColorer;
	}
}
