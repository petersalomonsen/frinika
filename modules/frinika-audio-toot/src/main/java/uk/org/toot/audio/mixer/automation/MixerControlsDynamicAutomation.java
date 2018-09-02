// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import java.util.Observable;
import java.util.Observer;
import uk.org.toot.control.Control;
import uk.org.toot.audio.mixer.MixerControls;

/**
 * Specifies the dynamic automation API in terms of Controls and their values.
 * Hooks into specified MixerControls as an Observer to efficiently
 * monitor all Control changes without needing individual listeners.
 * Relies on Control.notifyParent() Chain of Responsibility.
 **/
abstract public class MixerControlsDynamicAutomation extends BasicDynamicAutomation {
    /**
     * @supplierCardinality 1
     * @link aggregationByValue 
     */
    protected GlobalDynamicAutomationControls autoControls;

    private Observer observer;

    protected MixerControlsDynamicAutomation(MixerControls mcontrols) {
        super(mcontrols);
        // observe controls for automation write from control
        observer = new Observer() {
            public void update(Observable obs, Object obj ) {
				if ( obj instanceof Control ) {
                	MixerControlsDynamicAutomation.this.write((Control)obj);
                }
            }
        };
        mixerControls.addObserver(observer);

        // add global automation controls strip
/*        autoControls = new GlobalDynamicAutomationControls();
        mixerControls.addStripControls(autoControls); */
    }

    public void close() {
        mixerControls.deleteObserver(observer);
        observer = null;
    }

    // call to read automation to Control
    protected void read(Control c, int value) {
        // don't read if user is adjusting this control, for sanity
        if ( !c.isAdjusting() ) {
        	c.setIntValue(value);
        }
    }

    // override to write automation from Control
    protected abstract void write(Control c);
}
