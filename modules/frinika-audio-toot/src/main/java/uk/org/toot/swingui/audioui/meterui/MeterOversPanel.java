// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.meterui;

import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.control.Control;
import uk.org.toot.audio.meter.MeterControls;
import uk.org.toot.swingui.controlui.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.Timer;

public class MeterOversPanel extends ControlPanel
{
    private MeterControls controls;
    private JLabel leftOvers;
    private JLabel rightOvers;
    private JLabel centerOvers;

    private int[] left;
    private int[] right;
    private int center;

    public MeterOversPanel(Control indicator) {
        super(indicator);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        controls = (MeterControls)indicator.getParent();
        ChannelFormat format = controls.getChannelFormat();
        center = format.getCenter();
        left = format.getLeft();
        right = format.getRight();
        if ( left != null ) {
	        leftOvers = new OverLabel();
    	    add(leftOvers);
        }
        if ( center >= 0 ) {
            centerOvers = new OverLabel();
            centerOvers.setHorizontalAlignment(JLabel.CENTER);
            add(centerOvers);
		}
        if ( right != null ) {
			rightOvers = new OverLabel();
	        rightOvers.setHorizontalAlignment(JLabel.RIGHT);
    	    add(rightOvers);
        }
        Timer t = new Timer(500,
    	        new ActionListener() {
	                public void actionPerformed(ActionEvent ae) {
                	    pollAndUpdate();
            	    }
        	    }
            );
    	    setTimer(t);
    }

   	public void pollAndUpdate() {
       	int overs;
        if ( leftOvers != null ) {
            overs = 0;
            for ( int i = 0; i < left.length; i++ ) {
       			overs += controls.getState(left[i]).overs;
        	}
       		leftOvers.setText(overs < 100 ? String.valueOf(overs) : "^^");
        }
        if ( centerOvers != null ) {
	        overs = controls.getState(center).overs;
    	   	centerOvers.setText(overs < 100 ? String.valueOf(overs) : "^^");
        }
        if ( rightOvers != null ) {
            overs = 0;
            for ( int i = 0; i < left.length; i++ ) {
       			overs += controls.getState(right[i]).overs;
        	}
    	   	rightOvers.setText(overs < 100 ? String.valueOf(overs) : "^^");
        }
   	}

    static public class OverLabel extends JLabel
    {
        public OverLabel() {
            super("0");
        }

        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            d.height = 22;
            d.width = 22;
            return d;
        }
    }
}
