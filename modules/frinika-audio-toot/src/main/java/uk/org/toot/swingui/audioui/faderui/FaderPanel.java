// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.faderui;

import java.awt.Font;
import uk.org.toot.audio.fader.FaderControl;
import javax.swing.*;
import uk.org.toot.swingui.controlui.*;

public class FaderPanel extends ControlPanel
{
    private final FaderControl control;
    private JComponent pot;

    private static Font font = new Font("Arial", Font.PLAIN, 10);

    public FaderPanel(final FaderControl control, boolean rotary) {
        super(control);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.control = control;
        String name = control.getName();
       	if ( name.length() > 6 ) {
           	name = name.substring(0, 4);
   		}
        if ( !rotary ) {
          	pot = new Fader(control);
        } else {
            pot = new ControlKnob(control);
	       	JLabel label = new JLabel(name);
   		    label.setLabelFor(pot);
   	   		label.setFont(font);
       		label.setAlignmentX(0.5f);
   	    	add(label);
	        addMenu(pot); // doesn't work with a Fader (JSlider issue)
        }
        pot.setAlignmentX(0.5f);
        add(pot);
    }

    protected void addMenu(JComponent pot) {
        if ( control.getPresetNames() == null ) return;
		pot.setComponentPopupMenu(LawControlPresetMenu.getInstance());
    }
}
