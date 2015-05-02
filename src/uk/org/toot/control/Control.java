// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.util.Observable;

/**
 * The abstract base class for generic concrete Controls.
 * Because the various types of Controls have different purposes and features,
 * all of their functionality is accessed from the subclasses that define each
 * kind of Control.
 * The Controls are concrete so they separate the control state from a process.
 * This eases development of simple processes.
 * 
 * Differences to javax.sound.sampled.Controls:
 * Composite parent association is provided to support Chain of Responsibility
 * pattern through Control hierarchies.
 * Observable to support the Observer pattern.
 * Controls may be hidden. This is intended to inhibit UI display of
 * a Control. e.g. filter Q when NOT Parametric, obviously the filter
 * still has a Q, it's just immutable.
 * Controls may be indicators. This is intended to allow UIs to decide
 * how to represent and manage a Control.
 * Controls do not have a Type inner class.
 */
public abstract class Control extends Observable
{
	/**
	 * The unique id of the control, to support static and dynamic automation/persistence.
	 * Only unique within a limited sub-tree of a Control tree.
	 * Negative ids should not be persisted.
	 * Positive ids should be persisted.
	 * There may be a limited range for positive ids, depending on how persistence
	 * is implemented.
	 */
	private final int id;

    /**
     * The parent of the control.
     * @supplierCardinality 0..1
     * @link aggregation
     */
    private CompoundControl parent = null;

    private String name;
	private String annotation;
    private boolean hidden = false; // visible, hidden if true
    protected boolean indicator = false; // mutable, immutable if true
	private boolean adjusting = false; // UI should set for knobs, sliders etc.
	private boolean enabled = true;

    /**
     * Constructs a Control with the specified id and name.
     * @param id the id of the control
     * @param name the name of the control
     */
    protected Control(int id, String name) {
        this.name = name;
        this.id = id;
        annotation = name; // default annotation
    }

    public void setHidden(boolean h) {
        hidden = h;
    }

    /**
     * Obtains the control's id.
     * @return the control's id.
     */
    public int getId() {
        return id;
    }

    /**
     * Obtains the control's parent control.
     * @return the control's parent control.
     */
    public CompoundControl getParent() {
        return parent;
    }

    protected void setParent(CompoundControl c) {
    	parent = c;
    }
    
    protected void notifyParent(Control obj) {
    	derive(obj);
        setChanged();
        notifyObservers(obj);
        // we don't broadcast indicators to parent observers
        // they're probably changed frequently, i.e. every 2ms
        // and they are probably polled, i.e. every 200ms
        if ( obj.isIndicator() ) return;
        if ( parent != null ) {
        	parent.notifyParent(obj);
        }
    }

    /**
     * Provided to allow ComoundControls at the level of plugin modules
     * to easily derive process value from control values without
     * needing to add an Observer.
     * @param obj
     */
    protected void derive(Control obj) {}
    
    /**
     * Obtains the control's name.
     * @return the control's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the control's name
     * @param s the name
     */
    public void setName(String s) {
        name = s;
        annotation = s;
    }

    public String getAnnotation() {
        return annotation;
    }

    /**
     * Set a specific annotation, other than the default name
     * @param a the annotation
     */
    public void setAnnotation(String a) {
        annotation = a;
    }

    public void setIntValue(int value) {
        System.err.println("Unexpected setIntValue("+value+") called on "+getControlPath());
    }

    public int getIntValue() { return -1; }

    /** override for real value strings where possible **/
    public String getValueString() {
        return "";
    }

    /** a hint to a UI to inhibit display of this control. **/
    public boolean isHidden() {
        return hidden;
    }

    /** a hint to a UI to decide how to represent this control. */
    public boolean isIndicator() {
        return indicator;
    }

    public boolean isAdjusting() {
        return adjusting;
    }

    public void setAdjusting(boolean state) {
        adjusting = state;
        notifyParent(this); // tickle automation etc.
    }

    public void setEnabled(boolean enable) {
    	enabled = enable;
    }
    
    public boolean isEnabled() {
    	return enabled;
    }
    
    /**
     * Obtains a String describing the control type and its current state.
     * @return a String representation of the Control.
     */
    public String toString() {
        return getName() + " Control";
    }

    public String getControlPath() {
        return getControlPath(null, "/");
    }

    public String getControlPath(Control from, String sep) {
        if (parent != from) {
            if ( getName().length() > 0 ) { // avoid separator if name is ""
	            return parent.getControlPath(from, sep) + sep + getName();
            } else {
                return parent.getControlPath(from, sep);
            }
        }
        return getName();
    }
}
