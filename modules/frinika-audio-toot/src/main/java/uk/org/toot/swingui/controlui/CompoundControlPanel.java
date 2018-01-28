// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.*;
import java.util.Observable;
import java.util.List;
import uk.org.toot.control.*;

import static uk.org.toot.control.CompoundControlChain.ChainMutation.*;

public class CompoundControlPanel extends ControlPanel
{
    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    protected ControlSelector controlSelector = null;

    /**
     * @link aggregation
     * @supplierCardinality 1 
     */
    protected PanelFactory panelFactory;
    protected CompoundControl control;
    protected Container target;
    protected JTabbedPane tabbedPane = null;
    protected int axis;
    protected boolean mutating = false;
    protected boolean pending = false;

    public CompoundControlPanel(final CompoundControl control, final int axis,
        	ControlSelector controlSelector, PanelFactory panelFactory,
            boolean hasBorder, boolean hasHeader) {
        super(control);
        setLayout(new BoxLayout(this, axis));
        this.control = control;
        this.controlSelector = controlSelector;
        this.panelFactory = panelFactory;
        this.axis = axis;
        target = panelFactory.layout(control, axis, hasBorder, this, hasHeader);
        create();
    }

    private Runnable recreator = new Runnable() {
   	    public void run() {
      		recreate();
    		revalidate();
   			repaint();
   		}
    };

    // public as an implementation side-affect
    public void update(Observable obs, Object obj) {
        if ( obj instanceof CompoundControlChain.ChainMutation ) {
            CompoundControlChain.ChainMutation mutation =
                (CompoundControlChain.ChainMutation)obj;
            switch ( mutation.getType() ) {
            case INSERT:
            case MOVE:
            case DELETE:
            	pending = true;
                break;
            case COMMENCE: mutating = true; pending = false; break;
            case COMPLETE: mutating = false; break;
            }
            if ( pending && !mutating ) {
    	    	SwingUtilities.invokeLater(recreator); // brute force recreation
                pending = false;
            }
        }
    }

    protected boolean select(Control control) {
        if ( control == null ) return false;
        if ( controlSelector == null ) return true;
        return controlSelector.select(control);
    }

    protected void recreate() {
        if ( target == null ) return;
        target.removeAll();
        create();
    }

    protected void recreate(CompoundControl control) {
        this.control.deleteObserver(this);
        this.control = control;
        control.addObserver(this);
        recreate();
    }

    protected void create() {
        List<Control> controls = control.getControls();
        if ( controls == null ) return;
        if ( axis == BoxLayout.Y_AXIS && reverseIfYAxis() ) {
            // add controls in reverse natural order
	        for ( int i = controls.size()-1; i >= 0; i-- ) {
                addControlPanelConditionally(controls.get(i));
        	}
        } else {
	        for ( Control cont : controls ) {
                addControlPanelConditionally(cont);
    	    }
		}
    }

    protected boolean reverseIfYAxis() {
        return false;
    }

    protected void addControlPanelConditionally(Control c) {
       	if ( !select(c) ) return;
        if ( c.isHidden() ) return;
        if ( target == null ) return; // minimised are null!
        // cope with alternates
        if ( c instanceof CompoundControl ) {
            CompoundControl cc = (CompoundControl)c;
            String alt = cc.getAlternate();
            if ( alt != null ) { // it's an alternate
                if ( tabbedPane == null ) {
                    // !!! should be per alt
                    tabbedPane = new JTabbedPane(){
                        public Dimension getMaximumSize() {
                            return getPreferredSize();
                        }
                    };
                    target.add(tabbedPane); // add tabbed pane to target
                }
		        Component comp = panelFactory.createComponent(c, axis, false);
       			if ( comp == null ) return;
                tabbedPane.addTab(cc.getName(), comp);
                return;
            }
        }
        Component comp = panelFactory.createComponent(c, axis, true);
   	   	if ( comp == null ) return;
    	target.add(comp);
//        Thread.yield(); // ???
    }

    public CompoundControl getControl() { return control; }
    
    @SuppressWarnings("all")
    public Container getContainer() { return target; }
}
