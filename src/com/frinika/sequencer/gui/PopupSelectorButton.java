/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.frinika.sequencer.gui.mixer.MidiDeviceIconProvider;

public class PopupSelectorButton extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Insets insets = new Insets(0, 0, 0, 0);

	ListProvider resource;

	PopupClient client;

	// JButton button;
	public final JButton label;

	JPopupMenu menu;

	Object[] list;

	private boolean displaySelected = true;
	
	Icon icon;
	
	public void setIcon(Icon icon)
	{
		this.icon = icon;
		label.setIcon(icon);
	}

	public PopupSelectorButton(final ListProvider resource, PopupClient client) {
		this(resource, client, null, false);
	}

	public PopupSelectorButton(final ListProvider resource, PopupClient client,
			String currentState) {
		this(resource, client, currentState, true);
	}

	/**
	 * 
	 * @param resource
	 *            provides list for a popup menu
	 * @param client
	 *            is notified when a slection is made (fireSelected())
	 * @param currentState
	 *            string to describe the initial selection.
	 */
	public PopupSelectorButton(final ListProvider resource, PopupClient client,
			String currentState, boolean displaySelected) {
		this.resource = resource;
		this.client = client;
		this.displaySelected = displaySelected;
		
		if (!displaySelected && currentState == null) {
			ImageIcon icon = new ImageIcon(ClassLoader
					.getSystemResource("icons/1downarrow.png"));
			label = new JButton(icon);
		} else {
			// add(button);
			label = new JButton(currentState);
		}
		
		label.setMargin(insets);
		label.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createMenu(resource.getList(),e);
			}

		});

		add(label);
	}

	private void createMenu(Object[] list,ActionEvent e) {
		
		this.list = list;
		if (list== null) return;
		menu = new JPopupMenu();

        int count = 0;
		for (final Object o : list) {
			if (o != null) {
				JMenuItem it = new JMenuItem(o.toString());
				if(o instanceof MidiDeviceIconProvider)
					it.setIcon(((MidiDeviceIconProvider)o).getIcon());
				else
					if(icon != null) it.setIcon(icon);
				menu.add(it);
                final int index = count++;
                /**
				 * PJS The action listener is moved in here in order to send the
				 * correct index. Before the index was resolved based on
				 * object.toString(). If two objects returned the same only the
				 * first index were sent..
				 */
				it.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {                    	
    					if (displaySelected)
    						label.setText(e.getActionCommand());
                        client.fireSelected(PopupSelectorButton.this, o, index);
                    }});
			}
		}

        
        	menu.show(label, 0,0);
        
	}

   
}
