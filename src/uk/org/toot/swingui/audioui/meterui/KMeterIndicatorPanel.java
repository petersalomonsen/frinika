// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.meterui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Font;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.meter.MeterControls;
import javax.swing.*;

public class KMeterIndicatorPanel extends AbstractMeterIndicatorPanel
{
    private MeterControls controls;
    /**
     * @supplierCardinality 1..*
     * @link aggregationByValue
     */
    private MeterMovement[] movement;

    private MeterScale scale;
    private int scaleRange = -1;
	private int nchannels;

    private static Font scaleFont = new Font("Arial", Font.PLAIN, 10);

    // some prime number milliseconds to reduce cpu utilisation
    private static int[] times = { 47, 53, 43, 59, 41, 37, 61, 67, 71, 73};
    private static int timeIndex = 0;

    public KMeterIndicatorPanel(MeterControls.MeterIndicator indicator) {
        super(indicator, times[timeIndex]);
//        System.out.println(indicator.getControlPath()+" "+times[timeIndex]+" ms");
        timeIndex += 1;
        timeIndex %= times.length;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        controls = (MeterControls)indicator.getParent();
        ChannelFormat channelFormat = controls.getChannelFormat();
        nchannels = channelFormat.getCount();
        movement = new MeterMovement[nchannels];
        // assumes channel format channel order !!!
        if ( channelFormat == ChannelFormat.MONO ) {
        	add(movement[0] = new MeterMovement(indicator)); // center
        	add(scale = new MeterScale());
        } else if ( channelFormat == ChannelFormat.STEREO ) {
        	add(movement[0] = new MeterMovement(indicator)); // left
        	add(scale = new MeterScale());
        	add(movement[1] = new MeterMovement(indicator)); // right
        } else if ( channelFormat == ChannelFormat.QUAD ) {
        	add(movement[2] = new MeterMovement(indicator)); // rear left
        	add(movement[0] = new MeterMovement(indicator)); // front left
        	add(scale = new MeterScale());
        	add(movement[1] = new MeterMovement(indicator)); // front right
        	add(movement[3] = new MeterMovement(indicator)); // rear right
        } else if ( channelFormat == ChannelFormat.FIVE_1 ) {
        	add(movement[2] = new MeterMovement(indicator)); // rear left
        	add(movement[0] = new MeterMovement(indicator)); // front left
        	add(movement[4] = new MeterMovement(indicator)); // center
        	add(movement[1] = new MeterMovement(indicator)); // front right
        	add(movement[3] = new MeterMovement(indicator)); // rear right
        	add(scale = new MeterScale());
        	add(movement[5] = new MeterMovement(indicator)); // LF
        }
    }

/*    public Dimension getMaximumSize() {
        Dimension size = super.getMaximumSize();
        size.width = 45; // OK for mono, stereo
        return size;
    } */

    public Dimension getMinimumSize() {
        Dimension size = super.getMinimumSize();
        size.height = 256;
        return size;
    }

    // called on Swing thread so don't take too long
    protected void pollAndUpdate() {
        if ( !this.isShowing() ) return;
        for (int c = 0; c < nchannels; c++) {
            movement[c].updateState(controls.getState(c));
        }
        // if range has changed update scale
        int newRange = (int)(controls.getMaxdB() - controls.getMindB());
        if ( newRange != scaleRange ) {
            scale.repaint();
            scaleRange = newRange;
        }
    }

    public float dBFS(float val) {
        return (float)(20 * Math.log10(val));
    }

    public float dBK(float val) {
        return dBFS(val) + controls.getMaxdB();
    }

        public int dBtoY(float dB) {
            // controls.getMindB() <= dB  <= controls.getMaxdB()
            float mindB = controls.getMindB();
            float maxdB = controls.getMaxdB();
            float dBRange = maxdB - mindB;
            // becomes height-1 > y > 0
            int height = getHeight();
            if ( height == 0 ) return 0; // !!! avoids early divide by zero
            // dB	y
            // Max  1
            // 0
            // Min  height-1
//            float c = (maxdB / dBRange) * (height - 1) / height ;
            float c = (maxdB / dBRange) * (height - 2);
            float m = (2 - height) / dBRange;
            // y = m * dB + c;
            int y = (int)(m * dB + c);
            return y;
        }

    public class MeterScale extends JPanel
    {
        public MeterScale() {
            setOpaque(false);
        }

        public Dimension getPreferredSize() {
            Dimension pref = super.getPreferredSize();
            pref.width = 20;
            return pref;
        }

        public Dimension getMaximumSize() {
            Dimension max = super.getMaximumSize();
            max.width = 20;
            return max;
        }

        public void paintComponent(Graphics g ) {
            int textOffset = (int)(g.getFontMetrics().getAscent() / 2) - 2;
            float maxdB = controls.getMaxdB();
            float mindB = controls.getMindB();
            // draw the scales
            g.setColor(Color.darkGray);
            g.setFont(scaleFont);
            for ( int i = -24; i < maxdB; i++ ) {
                if ( (i % 4) == 0 && i > mindB ) {
                	g.drawString(String.valueOf(i), 2, dBtoY(i) + textOffset);
                }
            }
            for ( int i = -30; i > mindB; i -= 10 ) {
                g.drawString(String.valueOf(i), 2, dBtoY(i) + textOffset);
            }
        }
    }


    public class MeterMovement extends JPanel
    {
        private Marker maxPeakMarker = new Marker(Color.yellow);
        private Marker peakMarker = new Marker(Color.white);
        private Marker maxAverageMarker = new Marker(Color.yellow);
        private AverageBar averageBar = new AverageBar();

        public MeterMovement(MeterControls.MeterIndicator indicator) {
            setBackground(Color.darkGray);
        }

        public Dimension getPreferredSize() {
            Dimension pref = super.getPreferredSize();
            pref.width = 10;
            return pref;
        }

        public Dimension getMaximumSize() {
            Dimension max = super.getMaximumSize();
            max.width = 10;
            return max;
        }

        // note maximum marker is always at or above peak marker
        // note peak marker is always at or above average bar
        // which simplifies undrawing/drawing
        // ACTUALLY NOT GUARANTEED DUE TO RACE CONDITIONS !!!
        public void updateState(MeterControls.ChannelState state) {
            averageBar.setdB(dBK(state.average));
            maxAverageMarker.setdB(dBK(state.maxAverage));
            peakMarker.setdB(dBK(state.peak));
            maxPeakMarker.setdB(dBK(state.maxPeak));
        }


        protected class Marker 
        {
            protected Color color;
            protected int prevY = 2000; // for undrawing, component coordinate
            protected float prevdB = -150; // dB

            public Marker(Color c) {
                color = c;
            }

            public void setdB(float dB) {
                indicate(dB);
            }

            // override this method for average bar
            protected void indicate(float dB) {
                float mindB = controls.getMindB();
                if ( dB < mindB && prevdB < mindB ) return;
                prevdB = dB;
                Graphics g = getGraphics(); // !!!
                int w = getWidth() - 2;
                int y = dBtoY(dB);
                if ( y != prevY ) {
                	g.setColor(Color.darkGray);
                	g.drawLine(1, prevY, w, prevY);
                    prevY = y;
                }
                g.setColor(color);
                g.drawLine(1, y, w, y);
            }
        }


        protected class AverageBar extends Marker 
        {
            public AverageBar() {
                super(Color.green); // !!!
            }

            protected void indicate(float dB) {
                float mindB = controls.getMindB();
                if ( dB < mindB && prevdB < mindB ) return;
                
                prevdB = dB;
                int y = dBtoY(dB);
                if ( y == prevY ) return; // no change so do nothing
                if ( y  < 0 ) y = 0;
                
                Graphics g = getGraphics(); // !!!
                int w = getWidth() - 2;
                int h = getHeight() - 1;
            
                if ( y > prevY ) {
                    // if less dB (more y) just blank difference
                	g.setColor(Color.DARK_GRAY);
       	           	g.fillRect(1, prevY, w, y-prevY);
	                prevY = y;
                } else {
    	            prevY = y;
	                int y0 = dBtoY(0);
    	            int y4 = dBtoY(4);
        	        g.setColor(Color.green);
            	    if ( dB < 0 ) {
       	        	   	g.fillRect(1, y, w, h-y);
	                } else if ( dB < 4 ) {
    	   	           	g.fillRect(1, y0, w, h-y0);
        	            g.setColor(Color.yellow);
       	    	       	g.fillRect(1, y, w, y0-y);
                	} else {
       	           		g.fillRect(1, y0, w, h-y0);
	                    g.setColor(Color.yellow);
    	   	           	g.fillRect(1, y4, w, y0-y4);
        	            g.setColor(Color.red);
       	    	       	g.fillRect(1, y, w, y4-y);
                	}

	                // draw the scales
    	            g.setColor(Color.darkGray);
        	        for ( int i = 20; i >= -24; i-- ) {
            	        y = dBtoY(i);
                	    g.drawLine(1, y, w, y);
                	}
	                for ( int i = -30; i >= -120; i -= 10 ) {
    	                y = dBtoY(i);
        	            g.drawLine(1, y, w, y);
            	    }
                }
			}
        }
    }
}
