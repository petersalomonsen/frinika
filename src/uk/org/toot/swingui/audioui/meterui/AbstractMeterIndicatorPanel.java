// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.meterui;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import uk.org.toot.control.FloatControl;
import uk.org.toot.swingui.controlui.ControlPanel;

/**
 * Provides periodic polled update of a meter control panel
 */
abstract public class AbstractMeterIndicatorPanel extends ControlPanel
{
    public AbstractMeterIndicatorPanel(FloatControl indicator, int milliseconds) {
        super(indicator);
        if ( milliseconds > 0 ) {
	        Timer t = new Timer(milliseconds,
    	        new ActionListener() {
	                public void actionPerformed(ActionEvent ae) {
                	    pollAndUpdate();
            	    }
        	    }
            );
    	    setTimer(t);
        }
    }

    abstract protected void pollAndUpdate();
}
