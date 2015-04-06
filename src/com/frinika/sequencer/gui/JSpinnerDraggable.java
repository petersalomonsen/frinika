/*
 * Created on Jun 13, 2007
 *
 * Copyright (c) 2006-2007 Jens Gulden
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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import com.frinika.global.FrinikaConfig;

/**
 * A JSpinner, the value of which can also be edited by "dragging" the mouse
 * from the spinner, i.e. press the mouse button on the displayed number, and
 * then move the cursor (while keeping the button pressed) upwards or
 * downwards to change the value.
 * 
 * @author Jens Gulden
 */
public class JSpinnerDraggable extends JSpinner implements MouseMotionListener {

	private static final long serialVersionUID = 1L;
	
	protected int drag = 0;
	protected int keysTyped = 0;
	protected Robot robot;
	
	/**
	 * 
	 */
	public JSpinnerDraggable() {
		super();
		init();
	}

	/**
	 * @param model
	 */
	public JSpinnerDraggable(SpinnerModel model) {
		super(model);
		init();
	}
	
	private void init() {
		try {
			robot = new Robot();
		} catch (AWTException awte) {
			awte.printStackTrace();
		}
		JSpinner.DefaultEditor e = (JSpinner.DefaultEditor)this.getEditor();
		JTextField field = e.getTextField();
		MouseMotionListener[] listeners = field.getMouseMotionListeners();
		for (MouseMotionListener l : listeners) {
			field.removeMouseMotionListener(l);
		}
		field.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				drag = 0;
				keysTyped = 0;
			}
		});
		field.addMouseMotionListener(this);
	}
	
	

	public void mouseDragged(MouseEvent e) {
		System.out.println(e.getY());
		int y = e.getY();
		int diff;
		if (drag != 0) {
			diff = y - drag;
		} else {
			diff = (y > 0) ? 1 : -1;
			drag = y;
		}
		diff /= FrinikaConfig.MOUSE_NUMBER_DRAG_INTENSITY; // speed factor
		while (keysTyped < diff) {
			robot.keyPress(KeyEvent.VK_DOWN);			
			robot.keyRelease(KeyEvent.VK_DOWN);			
			keysTyped++;
		}
		while (keysTyped > diff) {
			robot.keyPress(KeyEvent.VK_UP);			
			robot.keyRelease(KeyEvent.VK_UP);			
			keysTyped--;
		}
	}

	public void mouseMoved(MouseEvent e) {
		// nop
	}

	/*public static void main(String[] args) {
		JFrame frame = new JFrame("Test JSpinnerDraggable");
		frame.setSize(200, 170);
		frame.setLocation(200, 200);
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		
		JSpinner spinner = new JSpinnerDraggable(new SpinnerNumberModel(66, 0 , 100, 1));
		
		GridBagConstraints gc = new GridBagConstraints();
		p.add(spinner, gc);
		frame.getContentPane().add(p);

		frame.show();
	}*/

}
