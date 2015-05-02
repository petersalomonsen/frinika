// Copyright (C) 2006,2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import java.awt.Dimension;
import uk.org.toot.control.EnumControl;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import java.util.Observable;

public class EnumIndicatorPanel extends ControlPanel
{
    private final EnumControl control;
    private JLabel label;
	private String labelText;

    public EnumIndicatorPanel(final EnumControl control) {
        super(control);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.control = control;
        labelText = control.getValueString();
        label = new JLabel(labelText, JLabel.CENTER) {
        	@Override
            public Dimension getMaximumSize() {
                Dimension size = super.getPreferredSize();
                size.width = control.getWidthLimit();
                return size;
            }
            @Override
            public String getText() {
            	return labelText;
            }
        };
        if ( control.hasLabel() ) {
        	JLabel lbl = new JLabel(control.getAnnotation());
        	lbl.setLabelFor(label);
   	   		lbl.setFont(font);
   	   		lbl.setAlignmentX(0.5f);
   	   		add(lbl);
    	}
		label.setBorder(BorderFactory.createEmptyBorder(3, 1, 2, 2));
		setAlignmentX(0.5f);
        add(label);
    }

    public void update(Observable obs, Object obj) {
        labelText = control.getValueString();
       	label.repaint();
    }
}
