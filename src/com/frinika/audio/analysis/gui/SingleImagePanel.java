/*
 * Created on Mar 20, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

package com.frinika.audio.analysis.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;


public class SingleImagePanel extends JPanel implements Observer  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SpectralSliceImage image;
	
	public SingleImagePanel(SpectralSliceImage image) {
		this.image = image;
		image.addObserver(this);
	}

	void dispose() {
		image.deleteObserver(this);	
	}
	
	
    @Override
	public void paint(Graphics g) {
	//	super.paintComponent(g);
		image.setRect(this.getBounds());
		image.drawImage((Graphics2D)g,0,0);
		
	//	System.out.println("Graph Paint");
	
	}


    @Override
	public Dimension getPreferredSize() {
		return new Dimension(1000, 120);  // TODO 
	}

	

	public void update(Observable arg0, Object arg1) {
		repaint();
	}


}
