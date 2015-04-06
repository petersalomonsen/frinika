/*
 * Created on Mar 14, 2006
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

package com.frinika.sequencer.gui.partview;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import com.frinika.sequencer.model.Lane;

/**
 * A Panel to hold parameters for a lane.
 */
public class LaneView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GridBagConstraints gc;

	Lane lane = null;

	public LaneView(Lane lane) {
		
		setLayout(new GridBagLayout());

		this.lane = lane;
	
	}

	/**
	 * gets called to reconstruct the GUI.
	 * 
	 */
	void init() {

		removeAll();

		gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = GridBagConstraints.RELATIVE;
		gc.ipady = 0;
		gc.anchor = GridBagConstraints.NORTHWEST;
		// gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weighty = 0.0;
		gc.weightx = 1.0;

		if (lane != null)  {
			makeButtons();
		}
		validate();
		repaint();
	}

	/**
	 * defualt lane has no buttons. OVERRIDE please
	 *
	 */
	protected void makeButtons() {
		
	}
	
	public Dimension getMinimumSize() {
		return new Dimension(250, 0);
	}


}
