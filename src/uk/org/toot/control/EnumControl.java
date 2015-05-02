// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.util.List;

/**
 * A <code>EnumControl</code> provides control over a set of discrete possible values, each represented by an object.  In a
 * graphical user interface, such a control might be represented
 * by a set of buttons, each of which chooses one value or setting.
 */
public abstract class EnumControl extends Control
{
    /** The current value. */
    private Object value;

    /**
     * Constructs a new enumerated control object with the given parameters.
     * @param value the initial control value
     */
    public EnumControl(int id, String name, Object value) {
        super(id, name);
        this.value = value;
    }

    /**
     * Sets the current value for the control.  The default implementation
     * simply sets the value as indicated.  If the value indicated is not supported, an IllegalArgumentException is thrown.
     * Some controls require that their line be open before they can be affected by setting a value.
     * @param value the desired new value
     * @throws IllegalArgumentException if the value indicated does not fall within the allowable range
     */
    public void setValue(Object value) {
    	if ( !isEnabled() ) return; 
        if (!isValueSupported(value)) {
            throw new IllegalArgumentException("Requested value " + value + " is not supported.");
        }
        if (!value.equals(this.value)) {
            this.value = value;
            notifyParent(this);
    	}
    }

    /**
     * Obtains this control's current value.
     * @return the current value
     */
    public Object getValue() {
        return value;
    }

    public abstract List getValues();

    /**
     * Indicates whether the value specified is supported.
     * @param value the value for which support is queried
     * @return <code>true</code> if the value is supported, otherwise <code>false</code>
     */
    protected boolean isValueSupported(Object value) {
        return getValues().contains(value);
    }

    /**
     * Provides a string representation of the control.
     * @return a string description
     */
    public String toString() {
        return getName() + " with current value: " + getValue();
    }

    public String getValueString() {
        return getValue().toString();
    }

    public void setIntValue(int value) {
    	try {
    		setValue(getValues().get(value));
    	} catch ( Exception e ) {
    		e.printStackTrace();
    	}
    }

    public int getIntValue() {
        return getValues().indexOf(getValue());
    }

    public int getWidthLimit() { return 40; }
    
    public boolean hasLabel() { return false; }
}
