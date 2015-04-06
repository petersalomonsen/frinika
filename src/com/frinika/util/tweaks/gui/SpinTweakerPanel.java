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

import com.frinika.util.tweaks.Tweakable;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class SpinTweakerPanel extends JPanel implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Tweakable t;

	SpinnerNumberModel model;

	public SpinTweakerPanel(Tweakable t) {
		this.t = t;

		model = new SpinnerNumberModel(t.getNumber(), t
				.getMinimum(), t.getMaximum(), t.getStepSize());
		final JSpinner spin = new JSpinner(model);
		// model = (SpinnerNumberModel)spin.getModel();
		// model.setValue(t.getNumber());
		// model.setMinimum((Comparable)t.getMinimum());
		// model.setMaximum((Comparable)t.getMaximum());

		spin.addChangeListener(this);
		add(new JLabel(t.getLabel()));
		add(spin);
		spin.getEditor().addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseEntered(MouseEvent e) {
				System.out.println("N");

				spin.getEditor().requestFocusInWindow();

				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				System.out.println("X");
				spin.getEditor().getRootPane().requestFocusInWindow();
				
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	}
	
//	public SpinTweaker(TweakerPanel p, Tweakable t) {
//		this.t = t;
//
//		model = new SpinnerNumberModel(t.getNumber(), (Comparable) t
//				.getMinimum(), (Comparable) t.getMaximum(), t.getStepSize());
//		JSpinner spin = new JSpinner(model);
//		// model = (SpinnerNumberModel)spin.getModel();
//		// model.setValue(t.getNumber());
//		// model.setMinimum((Comparable)t.getMinimum());
//		// model.setMaximum((Comparable)t.getMaximum());
//
//		spin.addChangeListener(this);
//
//		p.add(new JLabel(t.getLabel()), spin);
//	}

	public void stateChanged(ChangeEvent e) {
		t.set(model.getNumber());
		model.setValue(t.getNumber());
	}
}
