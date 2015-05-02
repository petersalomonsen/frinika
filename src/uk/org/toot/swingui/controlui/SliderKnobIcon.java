// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
//import javax.swing.JSlider;
import javax.swing.plaf.UIResource;


public class SliderKnobIcon implements Icon, UIResource { // was Serializable
	private static int IW = 24; // icon width
    private static int IH = 31; // icon height
    private static int IH2 = 15; // < half width
    
    private final static Color INSERT_COLOR = Color.BLACK;

    public SliderKnobIcon() {
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);

        Color lineColor = Color.WHITE;
        Color insertColor = INSERT_COLOR;
        
        if ( c instanceof SliderKnobColor ) {
            SliderKnobColor s = (SliderKnobColor)c;
            insertColor = s.getInsertColor();
        }
        
        Color backgroundColor;
        float[] hsb = Color.RGBtoHSB(insertColor.getRed(), insertColor.getGreen(), insertColor.getBlue(), null);
        if ( hsb[1] > 0f ) {
        	hsb[1] = hsb[1] < 0.5f ? 2f * hsb[1] : 0.5f * hsb[1];
        	backgroundColor = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
        } else {
        	backgroundColor = Color.GRAY;
        }
        // Fill in the background
        g.setColor(backgroundColor);
        g.fillRect(0, 0, IW-1, IH-1);

        // Fill in the insert
	    g.setColor(insertColor);
        g.fillOval(1, 1, IW-3, IH-3);
        
        // draw reference line
        if ( lineColor == insertColor ) lineColor = Color.BLACK;
        g.setColor(lineColor);
        g.drawLine(0, IH2, IW-1, IH2);

        g.translate(-x, -y);
    }

    public int getIconWidth() {
        return IW;
    }

    public int getIconHeight() {
        return IH;
    }
}
