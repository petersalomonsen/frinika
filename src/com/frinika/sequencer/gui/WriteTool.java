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

/**
 * Generic write tool. Calls the clients methods for press drag and release.
 * 
 * @author Paul
 *
 */
public class WriteTool extends ToolAdapter  implements EditTool {

	EraseTool erasetool;
	
	public WriteTool(Cursor c) {
		super(c);
		erasetool = new EraseTool(c);
	//	this.pianoRoll=pianoRoll;
	}
	
	int button = 0;
	public void mousePressed(MouseEvent e) {

		button = e.getButton();
		if(e.getButton() == 3)
		{
			erasetool.mousePressed(e);
			return;
		}
		client=(ItemPanel)e.getSource();
		
		if (client.isTimeLineEvent(e)) return;

		Point p = new Point(e.getX(),e.getY());
		client.map(p);
		client.writePressedAt(p);	
		
	}
	
	public void mouseDragged(MouseEvent e) {

		if(button == 3)
		{
			erasetool.mouseDragged(e);
			return;
		}
		Point p = new Point(e.getX(),e.getY());
		client.map(p);
		Point d=client.scrollToContian(p);
		client=(ItemPanel)e.getSource();
		client.writeDraggedAt(p);		
	}
	
	
	public void mouseReleased(MouseEvent e) {

		if(e.getButton() == 3)
		{
			erasetool.mouseReleased(e);
			return;
		}
		Point p = new Point(e.getX(),e.getY());
		client.map(p);
		client=(ItemPanel)e.getSource();
		client.writeReleasedAt(p);		
		
	}
	
	public void mouseClicked(MouseEvent e) { // Jens
		if(e.getButton() == 3)
		{
			erasetool.mouseClicked(e);
			return;
		}
		SelectTool.handleMouseClicked(e, this); // do the same as SelectTool (this allows editing text-parts by double-clicking also when the WriteTool is selected)
	}
	
}
