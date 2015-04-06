/*
 * Created on Feb 17, 2006
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

package com.frinika.sequencer.gui;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;

public abstract class ItemScrollPane extends JPanel implements AdjustmentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// TODO think
	protected ItemPanel itemPanel;

	protected JScrollBar horizScroll;

	public JScrollBar vertScroll;
	// TODO make seperate class
	JPanel vPanel;
	JPanel buts;
	JButton plus;
	JButton minus;
	
	protected ItemScrollPane() {
		super(false);
		setLayout(new BorderLayout());

		horizScroll = new JScrollBar(JScrollBar.HORIZONTAL);
		add(horizScroll, BorderLayout.SOUTH);
		horizScroll.addAdjustmentListener(this);
		vertScroll = new JScrollBar(JScrollBar.VERTICAL);
		
		
		vPanel=new JPanel();
		vPanel.setLayout(new BorderLayout());
		
		
		//------------------
		buts=new JPanel();
		buts.setLayout(new BorderLayout());
		final JButton plus=new JButton("+");
		buts.add(plus,BorderLayout.NORTH);
		final JButton minus=new JButton("-");
		buts.add(minus,BorderLayout.SOUTH);
		Insets m=new Insets(0,0,0,0);
		plus.setMargin(m);
		minus.setMargin(m);
		
		ActionListener vzoom=new ActionListener() {		
			public void actionPerformed(ActionEvent e) {		
				JButton but=(JButton)e.getSource();			
				if (but == plus) {
					vertZoom(+1);
				} else {
					vertZoom(-1);
				}
				rebuild();
			}					
		};
		plus.addActionListener(vzoom);
		minus.addActionListener(vzoom);
		//-------------------
		
		vPanel.add(vertScroll,BorderLayout.CENTER);
		vPanel.add(buts,BorderLayout.NORTH);
		add(vPanel, BorderLayout.EAST);
		vertScroll.addAdjustmentListener(this);
	}

	protected abstract void vertZoom(int inc);
	
	protected abstract void rebuild();
	/*
	 * protected void addRowHeader(JPanel rowHeader) {
	 * add(rowHeader,BorderLayout.WEST); }
	 */

	protected void setView(ItemPanel itemPanel) {
		assert (itemPanel != null);
		this.itemPanel = itemPanel;
		// add(itemPanel, BorderLayout.CENTER);
	}

	protected void setToolBar(JComponent tb) {
		add(tb, BorderLayout.NORTH);
	}

	public void adjustmentValueChanged(AdjustmentEvent ev) {
		if (itemPanel == null) return;
		JScrollBar bar = (JScrollBar) (ev.getSource());
		int val = ev.getValue();

		if (bar == horizScroll) {
			itemPanel.setX(val);
		
		}
		if (bar == vertScroll)
			itemPanel.setY(ev.getValue());
	}

	public void setX(int left) {
	

		horizScroll.setValue(left);

	}

	public void scrollBy(int dx, int dy) {
		
		
		itemPanel.itemViewRect.translate(dx, dy);

		if (itemPanel.itemViewRect.x < 0)
			itemPanel.itemViewRect.x = 0;
		if (itemPanel.itemViewRect.y < 0)
			itemPanel.itemViewRect.y = 0;
		
		if (horizScroll.getMaximum() < itemPanel.itemViewRect.x)
			horizScroll.setMaximum(itemPanel.itemViewRect.x);
	//	if (vertScroll.getMaximum() < itemPanel.itemViewRect.y)
	//		vertScroll.setMaximum(itemPanel.itemViewRect.y);
		
		horizScroll.setValue(itemPanel.itemViewRect.x);
		vertScroll.setValue(itemPanel.itemViewRect.y);

		itemPanel.paintImages();
		itemPanel.repaint();

	}

}
