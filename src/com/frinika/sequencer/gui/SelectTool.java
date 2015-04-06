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

import com.frinika.sequencer.model.TextPart;
import com.frinika.sequencer.gui.partview.PartView;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;

public class SelectTool extends ToolAdapter  implements EditTool {

	boolean dragging = false;
	private Point deltaDrag;

	public SelectTool(Cursor c) {
		super(c);

	}

	public void mousePressed(MouseEvent e) {
		
		client=(ItemPanel)e.getSource();
		/**
		 * In the time line then set the time cursor
		 */

		if (client.isTimeLineEvent(e)) return;
	
	

		boolean shift = e.isShiftDown();

		client.setControlState(e.isControlDown());
		client.setAltState(e.isAltDown());
		
		Point p = new Point(e.getX(), e.getY());

		client.map(p);

		Item item = client.itemAt(p);
		client.ignorePartWarp(true);
		if (item != null) {
			client.feedBack(item);
		//	System.out.println(" Item is selcted = " + item.isSelected());
			if (!item.isSelected()) {
				if (!shift)
					client.clientClearSelection();
				client.clientAddToSelection(item);
			//	client.setDragMode(ItemPanel.OVER_ITEM_MIDDLE);
			} else {
				if (shift)
					client.clientRemoveFromSelection(item);
				else
					client.setFocus(item);
			}

			// Detect right button
			if (e.getButton() == MouseEvent.BUTTON3) {
				client.rightButtonPressedOnItem(e.getX(),e.getY());
				return;
			}

			// client.notifySelectionChange();
		}


		// Detect right button
//		if (e.getButton() == MouseEvent.BUTTON3) {
//			client.rightButtonPressedInSpace();
//			return;
//		}

		
		if (item == null) {
			if (!shift) client.clientClearSelection();
			client.selectRect.mousePressed(e);
		} else {
			client.armDrag(p,item);
			dragging = true;
		}

		// TODO is this needed the notifies should do it ?
		//client.repaintItems();
		client.clientNotifySelectionChange();
		client.ignorePartWarp(false);
	}

	public void mouseDragged(MouseEvent e) {
	
		Point p = new Point(e.getX(), e.getY());

		client.map(p);
	
		Point d=client.scrollToContian(p);
	    client.selectRect.translate(-d.x,-d.y);
		if (client.selectRect.isActive()) {
			client.selectRect.mouseDragged(e);
		} else if (dragging) {
			client.dragTo(p);
		}
		client.repaint();
	}

	public void mouseReleased(MouseEvent e) {

		if (client.selectRect.isActive()) {
		//	System.out.println(" release while dragging ");
			if (!e.isShiftDown())
				client.clientClearSelection();
			client.selectRect.mouseRelease(e);
			client.selectInRect(client.mapRect(client.selectRect),e.isShiftDown());
			// client.refreshSelect ionContainer();
			// assert(!dragging);

		} else if (dragging) {
			client.endDrag();
			dragging=false;
		}

	}

	
	static int dragMode=-1;

	public void mouseMoved(MouseEvent e) {
		client=(ItemPanel)e.getSource();
	//	System.out.println(" MOUSE MOVE");
		Point p=new Point(e.getX(),e.getY());
		client.map(p);
		int dragNew = client.getHoverStateAt(p);
		if (dragNew == dragMode ) return;
		dragMode=dragNew;
		client.setDragMode(dragNew);
	}

	public void mouseClicked(MouseEvent e) { // Jens
		handleMouseClicked(e, this);
	}
	
	static void handleMouseClicked(MouseEvent e, ToolAdapter a) { // Jens
		// (also called from WriteTool.mouseClicked())
		if ( e.getClickCount()==2 ) { // double click
			a.client = (ItemPanel)e.getSource();
			Point p = new Point(e.getX(), e.getY());
			a.client.map(p);
			Item item = a.client.itemAt(p);
			a.client.ignorePartWarp(true); // ?
			if (item != null) {
				if (item instanceof TextPart) { // (currently double-click only useful on text part)
					// start in-place editing
					((TextPart)item).startInplaceEdit((PartView)a.client);
				}
			}
		}		
	}

}
