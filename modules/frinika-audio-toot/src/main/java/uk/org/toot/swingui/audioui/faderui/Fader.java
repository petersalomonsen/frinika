// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.faderui;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;
import java.util.Hashtable;
import uk.org.toot.audio.fader.FaderControl;
import uk.org.toot.swingui.controlui.SliderKnobColor;

import javax.swing.*;

public class Fader extends JSlider implements Observer, SliderKnobColor
{
    private final FaderControl control;
    private Runnable updater;

    private static final String SINGLE_DASH = " -";
    private static final String DOUBLE_DASH = " --";

	public Fader(final FaderControl control) {
    	super(VERTICAL, 0, control.getLaw().getResolution()-1, 0);
        this.control = control;
        super.setValue(sliderValue(control.getValue()));
        setPaintTrack(false);
        setLabelTable(createLabelTable());
        setPaintLabels(true);
        updater = new Runnable() {
   			public void run() {
   				Fader.super.setValue(sliderValue(control.getValue())); 
   			}
   		}; 
	}

	public void addNotify() {
        super.addNotify();
        control.addObserver(this);
    }

    public void removeNotify() {
        control.deleteObserver(this);
        super.removeNotify();
    }

   	public void update(Observable obs, Object obj) {
   		SwingUtilities.invokeLater(updater);
    }

    protected Hashtable createLabelTable() {
        Hashtable<Integer,JLabel> table = new Hashtable<Integer,JLabel>();
        JLabel label;
        for ( int i = 15; i > -101; i -= (i > -39) ? 5 : 20 ) {
            label =  new JLabel(i == 0 ? DOUBLE_DASH : SINGLE_DASH);
            label.setHorizontalAlignment(SwingConstants.RIGHT);
        	table.put(new Integer(sliderValue(i)), label);
        }
        return table;
    }

	protected float userValue(int sliderVal) {
        return control.getLaw().userValue(sliderVal);
	}

	protected int sliderValue(float userVal) {
        return control.getLaw().intValue(userVal);
    }

	public void setValue(int sliderVal) {
        super.setValue(sliderVal);
    	control.setValue(userValue(sliderVal));
	}

    public Color getInsertColor() { return control.getInsertColor(); }
}


