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
 * 
 */

package com.frinika.gui.util;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * A toolkit class for creating graphical buttons. Images are png images retrieved from the icons folder
 *  
 * @author P.J. Leonard
 * @author Peter Salomonsen
 */
public class ButtonFactory {
    
    /**
     * Make a toggle button
     * @param imageName - just the name, no folder or file extension information
     * @param actionCommand - action command string for the actionlistener
     * @param toolTipText
     * @param listener - action listener
     * @param group
     * @param panel
     * @return
     */
	public static JToggleButton makeToggleButton(String imageName,
			String actionCommand, String toolTipText, ActionListener listener,
			ButtonGroup group, JPanel panel) {

		JToggleButton button;

		try {
			button = new JToggleButton(new ImageIcon(ClassLoader
					.getSystemResource("icons/" + imageName + ".png")));
		} catch( Exception e) {
			System.err.println(" Problem creating icon  icons/" + imageName + ".png" );

			 button=new JToggleButton(imageName);
			
			
		}
		
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		if(listener!=null)
		    button.addActionListener(listener);
		if (group != null)
			group.add(button);
		panel.add(button);
		return button;
	}

    /**
     * Make a press button
     * @param imageName - just the name, no folder or file extension information
     * @param actionCommand - action command string for the actionlistener
     * @param toolTipText
     * @param listener - action listener
     * @param panel
     * @return
     */
	public static JButton makePressButton(String imageName, String actionCommand,
			String toolTipText, ActionListener listener, JComponent panel) {

		JButton button;

		button = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("icons/" + imageName + ".png")));
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		if(listener!=null)
		    button.addActionListener(listener);
		panel.add(button);
		return button;

	}

	public static JToggleButton makeToggleButton(String imageName, String actionCommand,
			String toolTipText, ActionListener listener, JComponent panel) {

		JToggleButton button;

		try {
		button = new JToggleButton(new ImageIcon(ClassLoader
				.getSystemResource("icons/" + imageName + ".png")));
		} catch(Exception e) {
			e.printStackTrace();
			button= new JToggleButton(imageName);
		}
		
		button.setActionCommand(actionCommand);
		button.setToolTipText(toolTipText);
		if(listener!=null)
		    button.addActionListener(listener);
		panel.add(button);
		return button;

	}
	
	public static JLabel makeIconLabel(String imageName) {

		JLabel button;

		button = new JLabel(new ImageIcon(ClassLoader
				.getSystemResource("icons/" + imageName + ".png")));
		return button;

	}

	
}
