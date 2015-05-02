// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.util.List;
import java.util.Collections;
import java.util.Hashtable;

/**
 * A <code>CompoundControl</code>, such as a graphic equalizer, provides control
 * over two or more related properties, each of which is itself represented as
 * a <code>Control</code>.
 */
public abstract class CompoundControl extends Control
{
    public static final int USE_PARENT_PROVIDER_ID = 0;

    /**
     * @link aggregation
     * @supplierCardinality 0..1 
     */
    private static CompoundControlPersistence persistence;

    /**
     * The set of member controls.
     * @link aggregationByValue
     * @supplierCardinality 0..*
     */
    protected List<Control> controls;

    int instanceIndex = 0;

    private Hashtable<Object, Object> properties;

	protected int providerId = USE_PARENT_PROVIDER_ID;

    protected CompoundControl(int id, String name) {
        this(id, deriveInstanceIndex(name), name);
    }

    protected CompoundControl(int id, int instanceIndex, String name) {
        super(id, name);
        checkInstanceIndex(instanceIndex);
        this.instanceIndex = instanceIndex;
    }

    protected static int deriveInstanceIndex(String name) {
        // if name ends with #n, instanceIndex = n-1;
        int hash = name.lastIndexOf('#');
        return ( hash > 0 ) ? Integer.parseInt(name.substring(hash+1)) - 1 : 0;
    }

    // overridden by some subclasses
	protected void checkInstanceIndex(int index) {
        if ( index < 0 )
            throw new IllegalArgumentException(getName()+" instance "+index+" < 0");
        if ( index > getMaxInstance() )
            throw new IllegalArgumentException(getName()+" instance "+index+" > "+getMaxInstance());
    }

	protected int getMaxInstance() { return 8-1; }
	
	/*
	 * a subclass should provide a public add() method
	 * which accepts a specific subtype of Control.
	 * That's why this method is protected.
	 */
    protected void add(Control control) {
        if ( control == null ) return;
        if ( controls == null ) {
            controls = new java.util.ArrayList<Control>();
        }
        controls.add(control);
        control.setParent(this);
    }

	/*
	 * a subclass should provide a public remove() method
	 * which accepts a specific subtype of Control.
	 * That's why this method is protected.
	 */
    protected void remove(Control control) {
        if ( control == null ) return;
        controls.remove(control);
        control.setParent(null);
    }

    /**
     * Returns the set of member controls that comprise the compound control.
     * @return the set of member controls.
     */
    public Control[] getMemberControls() {
        Control[] emptyArray = new Control[0];
        if ( controls == null ) return emptyArray;
        return controls.toArray(emptyArray);
    }

    public List<Control> getControls() {
        if ( controls == null ) return Collections.<Control>emptyList();
        return Collections.unmodifiableList(controls);
    }

    /**
     * Provides a string representation of the control
     * @return a string description
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < controls.size(); i++) {
            if (i != 0) {
                buf.append(", ");
                if ((i + 1) == controls.size()) {
                    buf.append("and ");
                }
            }
            buf.append(controls.get(i).getName());
        }
        return getName() + " Control containing " + buf + " Controls.";
    }

    public boolean isAlwaysVertical() { return false; }

    public boolean isAlwaysHorizontal() { return false; }

    public boolean isNeverBordered() { return false; }

    public float getAlignmentY() { return -1f; } // -ve values are ignored
    
    // override for tab
    public String getAlternate() { return null; }

    public int getInstanceIndex() { return instanceIndex; }

    @SuppressWarnings(value={"unchecked"})
    public <T> T find(Class<T> clazz) {
        if ( controls == null ) return null;
        for ( Control control : controls ) {
            if ( clazz.isInstance(control) ) {
                return (T)control;
            }
        }
        return null;
    }

    public Control find(String name) {
        if ( controls == null ) return null;
        for ( Control c : controls ) {
            if ( c.getName().equals(name) ) {
                return c;
            }
        }
        return null;
    }

    public CompoundControl find(int providerId, int moduleId, int instanceIndex) {
        // linear search for matching module
        for ( Control m : controls ) {
            if ( m instanceof CompoundControl ) {
	            CompoundControl cc = (CompoundControl)m;
    	        if ( providerId == cc.getProviderId() &&
	    	         moduleId == cc.getId() &&
	        	     instanceIndex == cc.getInstanceIndex() ) {
 					return cc;
            	}
            }
        }
        return null;
    }

	public Control deepFind(int controlId) {
        // depth first search of control tree
        for ( Control c : controls ) {
            if ( c instanceof CompoundControl ) {
                Control c2 = ((CompoundControl)c).deepFind(controlId);
                if ( c2 != null ) return c2;
            } else if ( controlId == c.getId() ) {
 				return c;
            }
        }
        return null;
    }

    public final Object getClientProperty(Object key) {
        if ( properties == null ) return null; // lazy instantiation intentional
        return properties.get(key);
    }

    public void putClientProperty(Object key, Object value) {
        if (properties == null) {
            properties = new Hashtable<Object, Object>();
        }
        properties.put(key, value);
    }

    public static CompoundControlPersistence getPersistence() {
        return persistence;
    }

    public static void setPersistence(CompoundControlPersistence p){
        persistence = p;
    }

    public boolean canBeMoved() { return true; }

    public boolean canBeMovedBefore() { return true; }

    public boolean canBeInsertedBefore() { return true; }

    public boolean canBeDeleted() { return true; }

    public boolean canBeMinimized() { return false; }
    
    public boolean hasPresets() { return true; }

    public boolean hasCustomUI() { return false; }
    
    public boolean canLearn() { return false; }
    
    public boolean getLearn() { return false; }
    
    public void setLearn(boolean learn) {}
    
    // return a domain specific string for preset organisation
    // i.e. audio, synth
    public String getPersistenceDomain() {
    	return getParent().getPersistenceDomain();
    }
    
    // one level of the hierarchy should override and return true
    public boolean isPluginParent() { return false; }
    
    public int getProviderId() {
	    if ( providerId == USE_PARENT_PROVIDER_ID ) {
	        return getParent().getProviderId(); // CoR
	    }
	    return providerId;
	}

	public void setProviderId(int id) {
	    providerId = id;
	}

	public void setInstanceIndex(int idx) {
		instanceIndex = idx;
	}
	
	protected void disambiguate(CompoundControl c) {
		String original = c.getName();
		if ( find(original) == null ) return;
		int index = 1;
		String str;
		do {
			index++;
			str = original+" #"+index;
		} while ( find(str) != null ) ;
		c.setName(str);
		c.setInstanceIndex(index-1);
	}

	public void close() {}
	
    // if the visibility is more than the max visibility the UI should supppress display
    public int getVisibility() { return 0; }
    
    public int getMaxVisibility() { return 0; }
        
    private final static String VISIBILITY_KEY = "Visibility";
    
    public int getCurrentVisibility() {
        Integer i = (Integer)getClientProperty(VISIBILITY_KEY);
        if ( i == null ) return 99;
        return i.intValue();
    }
    
    public void setCurrentVisibility(int i) {
        putClientProperty(VISIBILITY_KEY, new Integer(i));
    }
    /**
     * A ControlColumn groups certain Controls vertically.
     * It is always vertical and never bordered.
     */
    protected static class ControlColumn extends CompoundControl
    {
        public ControlColumn() {
            super(0, ""); // must be unnamed
        }

        public void add(Control c) { // make public
            super.add(c);
        }

        public boolean isAlwaysVertical() { return true; }

        public boolean isNeverBordered() { return true; }
    }

    /**
     * A ControlRow groups certain Controls horizontally.
     * It is always horizontal and never bordered.
     */
    protected static class ControlRow extends CompoundControl
    {
        public ControlRow() {
            super(0, ""); // must be unnamed
        }

        public void add(Control c) { // make public
            super.add(c);
        }

        public boolean isAlwaysHorizontal() { return true; }

        public boolean isNeverBordered() { return true; }
    }

    /**
     * A BypassControl is used if canBypass() is overridden to return true
     * (default is false).
     */
    public static class BypassControl extends BooleanControl
    {
        public BypassControl(int id) {
            super(id, "Bypass", true); // !!! !!! Id required, 127 ???
            // we set it to hidden because we don't want it to be automatically
            // displayed in the UI, we want to incorporate it in the UI header.
            setHidden(true);
        }
    }
    
    public NativeSupport getNativeSupport() { return null; }
    
    @Override
    public void setEnabled(boolean enable) {
    	super.setEnabled(enable);
    	for ( Control control : getControls() ) {
    		control.setEnabled(enable);
    	}
    }
    

}
