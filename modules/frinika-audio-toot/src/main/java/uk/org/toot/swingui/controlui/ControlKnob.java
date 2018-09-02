// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.controlui;

import uk.org.toot.control.LawControl;
import java.util.Observer;
import java.util.Observable;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * ControlKnob.java - A knob component.  The knob can be rotated by dragging
 * the knob around in a circle.
 * From source by Grant William Braught, Dickinson College, 12/4/2000
 */
public class ControlKnob extends JComponent implements Observer
{
	public static final int ROTARY_MODE = 0;
	public static final int VERTICAL_MODE = 1;
    private static int mouseMode = ROTARY_MODE;
    private static int linearRange = 20;
    
    private static final int radius = 16;
    private static final int spotRadius = 4;
    
    private int value;
    private double theta;
    private double thetaPrev = 0; // !!!
    private double thetaMax = Math.PI - Math.PI/6;
    private Color knobColor = Color.gray;
    private Color spotColor = Color.white;

    private MouseController mouseController;
	private LawControl control;

    public ControlKnob(LawControl c) {
        control = c;
		setValue(c.getLaw().intValue(c.getValue()));
        mouseController = createMouseController();
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }

    static public void setMouseMode(int mode) {
    	mouseMode = mode;
    }
    
	protected void setValue(int value) {
    	theta = (value * 2 * thetaMax) / (control.getLaw().getResolution()-1) - thetaMax;
	}

    public void addNotify() {
        super.addNotify();
        control.addObserver(this);
        addMouseListener(mouseController);
        addMouseMotionListener(mouseController);
    }

    public void removeNotify() {
        removeMouseListener(mouseController);
        removeMouseMotionListener(mouseController);
        control.deleteObserver(this);
        super.removeNotify();
    }

	public void update(Observable obs, Object obj) {
		setValue(control.getLaw().intValue(control.getValue()));
        repaint();
    }

    protected MouseController createMouseController() {
    	return new ModalMouseController();
    }

    /**
     * Paint the knob on the graphics context given.  The knob is a filled circle with a small filled circle offset within it
     * to show the current angular position of the knob.
     * @param g The graphics context on which to paint the knob.
     */
    public void paintComponent(Graphics g) {
        // Draw the knob.
        g.setColor(knobColor);
        g.fillOval(0, 0, 2 * radius, 2 * radius);
        // Draw the insert
        g.setColor(control.getInsertColor());
        g.fillOval(radius/2, radius/2, radius, radius);
        // Find the center of the spot.
        Point pt = getSpotCenter();
        final int xc = (int)pt.getX();
        final int yc = (int)pt.getY();
        // Draw the spot.
        g.setColor(spotColor);
        g.fillOval(xc - spotRadius, yc - spotRadius, 2 * spotRadius, 2 * spotRadius);
    }

    /**
     * Return the ideal size that the knob would like to be.
     * @return the preferred size of the knob.
     */
    public Dimension getPreferredSize() {
        return new Dimension(2 * (radius+1), 2 * (radius+1));
    }

    /**
     * Return the minimum size that the knob would like to be. This is the same
     * size as the preferred size so the knob will be of a fixed size.
     * @return the minimum size of the knob.
     */
    public Dimension getMinimumSize() {
        return new Dimension(2 * (radius+1), 2 * (radius+1));
    }

    /**
     * Return the maximum size that the knob would like to be. This is 50% bigger
     * than the preferred size so the knob separation will be of a limited size.
     * @return the minimum size of the knob.
     */
    public Dimension getMaximumSize() {
        return new Dimension((int)(2.2 * radius), (int)(2.2 * radius));
    }

    /**
     * Get the current anglular position of the knob.
     * @return the current anglular position of the knob.
     */
    public double getTheta() {
        return theta;
    }

    protected void setTheta(double val) {
        theta = val;
	    // theta -> internal -> control.setValue()
        value = (int)((theta + thetaMax) *
            		  (control.getLaw().getResolution()-1) /
            		  (2 * thetaMax));
        control.setValue(control.getLaw().userValue(value));
    }

    /**
     * Calculate the x, y coordinates of the center of the spot.
     * @return a Point containing the x,y position of the center of the spot.
     */
    private Point getSpotCenter() {
        // Calculate the center point of the spot RELATIVE to the
        // center of the of the circle.
        final int r = radius - spotRadius;
        final int xcp = (int)(r * Math.sin(theta));
        final int ycp = (int)(r * Math.cos(theta));
        // Adjust the center point of the spot so that it is offset
        // from the center of the circle.  This is necessary becasue
        // 0,0 is not actually the center of the circle, it is  the
        // upper left corner of the component!
        final int xc = radius + xcp;
        final int yc = radius - ycp;
        // Create a new Point to return since we can't
        // return 2 values!
        return new Point(xc, yc);
    }

    public interface MouseController extends MouseListener, MouseMotionListener {
    }

    public abstract class AbstractMouseController implements MouseController {
        protected boolean pressedOn = false;
        public void mouseClicked(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mousePressed(MouseEvent e) {
            if ( pressedOn ) control.setAdjusting(true);
        }
        public void mouseReleased(MouseEvent e) {
            control.setAdjusting(false);
            pressedOn = false;
        }
        public void mouseMoved(MouseEvent e) { }
        public void mouseDragged(MouseEvent e) { }

        // Compute RELATIVE to the center of the knob.
        // Math.atan2(...) computes the angle at which
        // x,y lies from the positive y axis with cw rotations
        // being positive and ccw being negative.
        protected double calculateTheta(int x, int y) {
            return Math.atan2(x-radius, radius-y);
        }
    }

    private class ModalMouseController extends AbstractMouseController
    {
        private int mode;
        private double thetaDelta; // rotary mode angle between spot and mouse
        							// also used in vertical mode
        private int yPrev; // vertical mode

        /**
         * When the mouse button is pressed, the dragging of the spot will be
         * enabled if the button was pressed over the knob.
         * @param e reference to a MouseEvent object describing the mouse press.
         */
        public void mousePressed(MouseEvent e) {
        	mode = mouseMode;
            pressedOn = true;
            switch ( mode ) {
            case VERTICAL_MODE:
            	yPrev = e.getY();
            	break;
            default:
            	thetaDelta = theta - calculateTheta(e.getX(), e.getY());
            	break;
            }
            super.mousePressed(e);
        }

        /**
         * Compute the new angle for the spot and repaint the knob.
         * If Shift is pressed sensitivity should be 10 times less in vertical mode.
         * @param e reference to a MouseEvent object describing the mouse drag.
         */
        public void mouseDragged(MouseEvent e) {
            if (pressedOn) {
            	double thetaNom;
            	switch ( mode ) {
            	case VERTICAL_MODE:
            		thetaDelta = (yPrev - e.getY()) / (float)linearRange;
            		if ( e.isShiftDown() ) thetaDelta /= 10;
            		thetaNom = theta + thetaDelta;
            		break;
            	default:
            		thetaNom = calculateTheta(e.getX(), e.getY()) + thetaDelta;
            		break;
            	}
                if ( thetaNom < -thetaMax ) {
                    thetaNom = -thetaMax;
                } else if ( thetaNom > thetaMax ) {
                    thetaNom = thetaMax;
                }
                // if thetaNom isn't a valid transition from theta, return !!!
                if ( thetaNom > 0 && thetaPrev < -Math.PI/2 ) return;
                if ( thetaNom < 0 && thetaPrev > Math.PI/2 ) return;
                thetaPrev = theta;
                setTheta(thetaNom);
                yPrev = e.getY();
                repaint();
            }
        }
    	
    }
}
