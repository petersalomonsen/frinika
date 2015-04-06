/*
 * Created on Jan 19, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.frinika.sequencer.gui.partview.PartView;

public class PartSplitTool extends ToolAdapter  implements EditTool {

	boolean dragging = false;
	private Point deltaDrag;

	public PartSplitTool(Cursor c) {
		super(c);

	}

	public void mousePressed(MouseEvent e) {
		
		client=(ItemPanel)e.getSource();
		/**
		 * In the time line then set the time cursor
		 */
	
		if (client.isTimeLineEvent(e)) return;
	
	

		// Detect right button
//		if (e.getButton() == MouseEvent.BUTTON3) {
//			client.rightButtonPressedOnItem();
//			return;
//		}
//	

		boolean shift = e.isShiftDown();

		client.setControlState(e.isControlDown());
		
		Point p = new Point(e.getX(), e.getY());

		client.map(p);


		((PartView)client).splitAt(p);
		
		
		// TODO is this needed the notifies should do it ?
		// client.repaintItems();
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseMoved(MouseEvent e) {
		client=(ItemPanel)e.getSource();
	
		if (client.isTimeLineEvent(e)) return;
		
		Point p = new Point(e.getX(), e.getY());

		client.map(p);

		((PartView)client).splitIsOver(p);
		

	}
	
}
