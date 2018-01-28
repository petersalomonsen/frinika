// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import uk.org.toot.control.FloatControl;
import javax.swing.*;

public class FloatControlPanel extends ControlPanel
{
    private final FloatControl control;
    private JComponent pot;

    public FloatControlPanel(final FloatControl control, int axis) {
        super(control);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.control = control;
        boolean rotary = control.isRotary(); // || axis == BoxLayout.X_AXIS; // !!! simple heuristic
        if ( rotary ) {
            pot = new ControlKnob(control);
			addMenu(); // doesn't work with JSlider
        } else {
          	pot = new ControlSlider(control);
        }
        String name = abbreviate(control.getAnnotation());
       	JLabel label = new JLabel(name);
   	    label.setLabelFor(pot);
   	   	label.setFont(font);
       	label.setAlignmentX(0.5f);
   	    add(label);
        pot.setAlignmentX(0.5f);
        add(pot);
    }

    protected void addMenu() {
        if ( control.getPresetNames() == null ) return;
		pot.setComponentPopupMenu(LawControlPresetMenu.getInstance());
    }
}
