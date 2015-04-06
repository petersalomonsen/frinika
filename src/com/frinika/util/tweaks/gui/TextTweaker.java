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

class TextTweaker  implements ActionListener {

    Tweakable t;
    JTextField textField;


    TextTweaker(TweakerPanel p,Tweakable t) {
	this.t=t;
	int len = t.getMaximum().toString().length();
	textField = new JTextField(String.valueOf(t.getNumber()),len);
	textField.addMouseListener(new MouseListener(){

		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseEntered(MouseEvent e) {
			textField.requestFocusInWindow();

			// TODO Auto-generated method stub
			
		}

		public void mouseExited(MouseEvent e) {
			textField.getRootPane().requestFocusInWindow();
			
		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
	});
	
	textField.addActionListener(this);
	p.add(new JLabel(t.getLabel()),textField);
    }


    public void actionPerformed(ActionEvent e) {

	//	Object o=e.getSource();
	//	Object v = ((JFormattedTextField)o).getValue();
	t.set(textField.getText()); //.toString());
	textField.setText(t.toString());

    }
	

}
