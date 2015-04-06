/*
 * Created on Jan 23, 2006
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


public class EraseTool extends ToolAdapter implements EditTool {

	public EraseTool( Cursor cursor) {
		super( cursor);
	}

	public void mousePressed(MouseEvent e) {
		client=(ItemPanel)e.getSource();
		
		if (client.pointInTimeLine(e.getY())) {
			int x=client.mapX(e.getX());
			client.setTimeAtX(x);	
			return;
		}
	
		Point p = new Point(e.getX(),e.getY());
		client.map(p);
		Item item= client.itemAt(p);

		if (item != null) {
		
			client.erase(item);

		}
	}

	public void mouseDragged(MouseEvent e) {

		// TODO effecient ?
		Point p = new Point(e.getX(),e.getY());
		client.map(p);
		Item item = client.itemAt(p);
		if (item != null) {
			client.erase(item);
		}
	}
	
}
