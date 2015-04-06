/*
 * Created on Feb 10, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

import com.frinika.sequencer.gui.pianoroll.ControllerHandle;
import com.frinika.sequencer.model.ControllerListProvider;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

/**
 * GUI element for selecting a MIDI-controller type from a drop-down-list.
 * 
 * @author Jens Gulden
 */
public class ControllerSelector extends JPanel {
	
	int controllerType = 7;
	private JComboBox comboBox;

	public ControllerSelector() {
		super(new BorderLayout());
		comboBox = new JComboBox();
		comboBox.setEditable(true);
		this.add(comboBox);
	}	
	
	public int getControllerType() {
		Object o = comboBox.getSelectedItem();
		if (o instanceof ControllerHandle) {
			ControllerHandle ch = (ControllerHandle)o;
			controllerType = ch.getController(); 
		} else if (o instanceof String) { // user-edited: direct number
			try {
				controllerType = Integer.parseInt((String)o);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		return controllerType;
	}

	public void setControllerType(int controllerType) {
		this.controllerType = controllerType;
		for (int i = 0; i < comboBox.getItemCount(); i++) {
			ControllerHandle ch = (ControllerHandle)comboBox.getItemAt(i);
			if (ch.getController() == controllerType) {
				comboBox.setSelectedIndex(i);
				return;
			}
		}
		// reached here: not found in list
		comboBox.getEditor().setItem(String.valueOf(controllerType));
	}

	public void setControllerList(ControllerListProvider clp) {
		comboBox.setModel(new DefaultComboBoxModel(clp.getList()));
	}


	public void addPseudoController(String name, int nr) {
		comboBox.addItem(new ControllerHandle(name, 0, 0, nr, nr)); // pseudo, local-only
	}

	public void addActionListener(ActionListener l) {
		comboBox.addActionListener(l);
	}

	public void removeActionListener(ActionListener l) {
		comboBox.removeActionListener(l);
	}

	
}
