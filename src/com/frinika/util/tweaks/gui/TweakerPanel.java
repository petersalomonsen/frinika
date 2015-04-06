/*
 * Created on 23-Feb-2007
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

package com.frinika.util.tweaks.gui;

import java.awt.*;
import javax.swing.*;

import com.frinika.util.tweaks.Tweakable;

public class TweakerPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GridBagConstraints c;

	private int rows;

	private int cols;
	private int minWidth;
	
	public TweakerPanel(int rows, int cols) {
		setLayout(new GridBagLayout());
		this.rows = rows;
		this.cols = cols;
		c = new GridBagConstraints();
		c.ipadx = 2;
		c.ipady = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 0;
	}

	
//	public TweakerPanel(int rows, int cols,int minWidth) {
//		setLayout(new GridBagLayout());
//		this.rows = rows;
//		this.cols = cols;
//		c = new GridBagConstraints();
//		c.ipadx = 2;
//		c.ipady = 1;
//		c.gridy = 0;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.weightx = 1.0;
//		c.weighty = 1.0;
//		c.gridx = 0;
//		c.gridy = 0;
//		this.minWidth=minWidth;
//	}

	// public void add(JComponent label, JComponent cntrl) {
	// add(label, c);
	// c.gridx++;
	// add(cntrl, c);
	// c.gridx++;
	// }
	//	

	public void newRow() {
		c.gridy++;
		c.gridx = 0;
	}

	public void addSpinTweaker(Tweakable tweak) {
		add(new SpinTweakerPanel(tweak),c);
		c.gridx++;
		if (c.gridx >= cols) newRow();
	}
	
	public  void addComponent(JComponent comp) {
		add(comp,c);
		c.gridx++;
		if (c.gridx >= cols) newRow();
	}

}
