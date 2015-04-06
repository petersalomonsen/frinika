/*
 * Created on Jan 15, 2006
 *
 * Copyright (c) 2005 P.J.Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frinika.sequencer.gui;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/**
 * 
 * @author pjl
 * 
 */

class StrechyRectangle extends Rectangle {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int x1;

	int y1;

	// JComponent parent;

	boolean isActive = false;

	StrechyRectangle(JComponent parent) {
		// this.parent = parent;
	}

	void mousePressed(MouseEvent e) {
		// System.out.println( " RS pressed ");
		isActive = true;
		x1 = x = e.getX();
		y1 = y = e.getY();
		width = 0;
		height = 0;

	}

	public void translate(int dx, int dy) {
		super.translate(dx, dy);
		x1 += dx;
		y1 += dy;
	}

	void mouseDragged(MouseEvent e) {
		// System.out.println( " RS dragged ");
		int x2 = e.getX();
		int y2 = e.getY();

		if (x2 > x1) {
			x = x1;
			width = x2 - x1;
		} else {
			x = x2;
			width = x1 - x2;
		}

		if (y2 > y1) {
			y = y1;
			height = y2 - y1;
		} else {
			y = y2;
			height = y1 - y2;
		}

		// dateDrawableRect(getWidth(), getHeight());
		// Rectangle totalRepaint =
		// this.rectToDraw.union(this.previousRectDrawn);
		// repaint(totalRepaint.x, totalRepaint.y,totalRepaint.width,
		// totalRepaint.height);
		// repaint();
	}

	// @Override
	// protected synchronized void paintComponent(Graphics g) {
	// super.paintComponent(g); // paints the background and image
	// g.setXORMode(Color.WHITE); // Color of line varies
	//
	// if (this.rect != null) {
	// g.drawRect(this.rect.x, this.rect.y,
	// this.rect.width - 1, this.rect.height - 1);
	// }
	//		
	// int y=getY();
	// int h=getHeight();
	// int x=getX();
	// int w=getWidth();
	// // System.out.println(" draewing curosr " + xCursor + " " + getBounds());
	// if (xCursor >=x && xCursor <= x+w ) {
	// // g.drawLine(xCursor,y,xCursor,y+h);
	// }
	//		
	// g.setPaintMode();
	//
	// }

	public void mouseRelease(MouseEvent e) {
		isActive = false;
	}

	public boolean isActive() {
		return isActive;
	}

}
