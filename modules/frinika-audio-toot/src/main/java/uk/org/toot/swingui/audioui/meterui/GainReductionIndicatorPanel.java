// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.meterui;

import javax.swing.*;
import uk.org.toot.control.FloatControl;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

public class GainReductionIndicatorPanel extends AbstractMeterIndicatorPanel
{
    private FloatControl indicator;
    private MeterMovement movement;

    public GainReductionIndicatorPanel(FloatControl indicator) {
        super(indicator, 59); // !!! millisecond update, avoid 50!
        this.indicator = indicator;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        movement = new MeterMovement();
        add(movement);
    }

    protected void pollAndUpdate() {
        if ( !this.isShowing() ) return;
        movement.updateState(indicator.getValue());
    }

    public int dBtoX(float dB) {
        // indicator.getMinimum() <= dB  <= indicator.getMaximum()
        float mindB = indicator.getMinimum();
        float maxdB = indicator.getMaximum();
        float dBRange = maxdB - mindB;
        // becomes height-1 > y > 0
        int width = getWidth();
        if ( width == 0 ) return 0; // !!! avoids early divide by zero
        // dB	x
        // Max  height-1
        // 0
        // Min  1
        float c = (maxdB / dBRange) * (width - 2);
        float m = (2 - width) / dBRange;
        int x = (int)(m * dB + c);
        return width - x;
    }

    public class MeterMovement extends JPanel
    {
        private Marker bar = new Marker(Color.yellow);

        public MeterMovement() {
            setBackground(Color.darkGray);
        	setMaximumSize(new Dimension(128, 10));
    	    setPreferredSize(new Dimension(64, 10));
	        setMinimumSize(new Dimension(24, 6));
        }

        public void updateState(float dB) {
           	bar.setdB(dB);
        }


        protected class Marker {
            protected Color color;
            protected int prevX = 10000; // for undrawing, component coordinate

            public Marker(Color c) {
                color = c;
            }

            public void setdB(float dB) {
                indicate(dB);
            }

            protected void indicate(float dB) {
                int x = dBtoX(dB);
                if ( x == prevX ) return; // no change
                Graphics g = getGraphics(); // !!!
                int h = getHeight() - 2;
                int w = getWidth() - 2;
                int x0 = dBtoX(-6); // !!!
                int x1 = dBtoX(-12); // !!!
                if ( x > prevX ) {
                	g.setColor(Color.darkGray);
                	g.fillRect(prevX, 1, x-prevX, h);
                } else {
                	g.setColor(Color.green);
                	if ( dB > -6 ) {
                		g.fillRect(x, 1, w-x, h);
                	} else if ( dB > -12 ) {
                		g.fillRect(x0, 1, w-x0, h);
                		g.setColor(Color.yellow);
                		g.fillRect(x, 1, x0-x, h);
                	} else {
                		g.fillRect(x0, 1, w-x0, h);
                		g.setColor(Color.yellow);
                		g.fillRect(x1, 1, x0-x1, h);
                		g.setColor(Color.red);
                		g.fillRect(x, 1, x1-x, h);
                	}
                }
                prevX = x;

                // draw the scales
                g.setColor(Color.darkGray);
                for ( int i = 1; i >= -3; i-- ) {
                    x = dBtoX(i);
                    g.drawLine(x, 1, x, h);
                }
                for ( int i = -6; i >= -24; i -= 3 ) {
                    x = dBtoX(i);
                    g.drawLine(x, 1, x, h);
                }
            }
        }
    }
}
