// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui;

import uk.org.toot.control.*;
import javax.swing.*;
//import java.util.Observer;
//import java.util.Observable;
import uk.org.toot.control.ControlSelector;
import uk.org.toot.swingui.DisposablePanel;
import uk.org.toot.swingui.controlui.PanelFactory;

public class AudioCompoundStripPanel extends DisposablePanel
{
    protected CompoundControl controls;
    protected ControlSelector controlSelector = null;
    protected PanelFactory panelFactory;

    public AudioCompoundStripPanel(CompoundControl controls, PanelFactory panelFactory) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        assert controls != null;
        this.controls = controls;
        this.panelFactory = panelFactory;
    }

    protected void dispose() {
        panelFactory = null;
        removeAll();
    }

    public void setControlSelector(ControlSelector selector) {
        controlSelector = selector;
        // if we're not added to parent, no need to reset
        if ( getParent() != null ) {
        	reset();
        }
    }

    protected void reset() {
        setup();
        revalidate();
    }

    protected void setup() {
        removeAll(); // !!! brute force change
        if ( controls == null ) return; // !!! !!! bug finding
        for ( Control control : controls.getControls() ) {
		    setupStrip((CompoundControl)control);
        }
    }

    protected void setupStrip(CompoundControl stripControls) {
        AudioCompoundControlPanel stripPanel =
            new AudioCompoundControlPanel(stripControls, BoxLayout.Y_AXIS, controlSelector, panelFactory, false, true);
        add(stripPanel);
    }

    public void addNotify() {
        super.addNotify();
        setup();
    }

    public void removeNotify() {
		removeAll();
        super.removeNotify();
    }
}
