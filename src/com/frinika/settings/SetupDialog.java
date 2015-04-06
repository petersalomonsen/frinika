/*
 * Created on 20.5.2007
 *
 * Copyright (c) 2006-2007 Karl Helgason
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

package com.frinika.settings;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;



public class SetupDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args)
	{
		new SetupDialog().setVisible(true);
	}
	
	private static SetupDialog instance = null;
	
	public static void showSettings()
	{
		if(instance == null) instance = new SetupDialog();
		instance.setAlwaysOnTop(true);
		instance.setModal(false);
		instance.setVisible(true);
	}
	
	public static void showSettingsModal()
	{
		if(instance == null) instance = new SetupDialog();
		instance.setAlwaysOnTop(false);
		instance.setModal(true);
		instance.setVisible(true);		
	}


	public SetupDialog()
	{
		super();
		
		setTitle("Audio Server Setup");
		setUndecorated(true);
	//	setBackground(Color.WHITE);	
		// setForeground(Color.WHITE);	
//		JTabbedPane tabPanel=new JTabbedPane();
//		add(tabPanel);
		JPanel panel=new InitialAudioServerPanel();
	//	panel.setBackground(Color.WHITE);	
		add(panel);
	//	panel.setBorder(BorderFactory.createLineBorder(Color.black, 2)); //"Audio Server Setup"));

		pack();
		setSize(400, 180);		
	    Rectangle windowSize ;
	    Insets windowInsets;		
		
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();	    
	    if(gc == null) 
	        gc = getGraphicsConfiguration();	    
	    
	    if(gc != null) {
	    	windowSize = gc.getBounds();
	    } else {
	    	windowSize = new java.awt.Rectangle(toolkit.getScreenSize());
	    }	   		
		
		Dimension size = getSize();		
		Point parent_loc = getLocation();			
		setLocation(parent_loc.x + windowSize.width/2 - (size.width/2),
				    parent_loc.y + windowSize.height/2 - (size.height/2)+100);
		
	}
	
}
