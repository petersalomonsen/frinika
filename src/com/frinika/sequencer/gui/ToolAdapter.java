/*
 * Created on Jan 17, 2006
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


/*
 * Mouse handler for the default state.
 * 
 */

package com.frinika.sequencer.gui;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 *  Default implementation for mouse tools for operations on Items in an ItemPanel. 
 * 
 * @author pjl
 *
 */
public class ToolAdapter implements MouseMotionListener, MouseListener ,KeyListener {

	protected ItemPanel client;
	
	Cursor cursor;
	boolean overCursor=false;
	
	/**
	 * Should not be able to create unless subclassed
	 * @param pianoRoll
	 */
	protected ToolAdapter(Cursor cursor) {
		this.client = null;
		this.cursor = cursor;
	}

	
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseMoved(MouseEvent e) {
			
	}

	public void mouseClicked(MouseEvent e) {

		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
	
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void setDragPoint(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	public void releaseDrag(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	/**
	 *@deprecated
	 */
	public void keyTyped(KeyEvent e) {
		System.out.println("   HELLO  1 ");
		
		if(e.getKeyCode()==KeyEvent.VK_DELETE) {
		// TODO	client.deleteSelected();
		}
		// TODO Auto-generated method stub
		
	}

	public void keyPressed(KeyEvent e) {
		
	System.out.println("   HELLO   2");
		// TODO Auto-generated method stub
		
	}

	public void keyReleased(KeyEvent e) {
		System.out.println("   HELLO   3");
		// TODO Auto-generated method stub
		
	}

	public Cursor getCursor() {
		// TODO Auto-generated method stub
		return cursor;
	}
	
/*	void setClient(ItemPanel client) {
		this.client=client;
	}*/
}
