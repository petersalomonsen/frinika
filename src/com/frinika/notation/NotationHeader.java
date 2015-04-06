/*
 * Created on 17.4.2007
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

package com.frinika.notation;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.notation.ClefChange;

public class NotationHeader extends JPanel implements MouseListener,
AdjustmentListener {

	private static final long serialVersionUID = 1L;
	private int yScroll;
	public NotationEditor notationEditor;
	public NotationHeader(NotationEditor notationEditor, int timePanelHeight, int value) {
		
		this.notationEditor = notationEditor;
		int width = 135;
		
		int yBot = 100;
		setSize(new Dimension(width, yBot));		
		setPreferredSize(new Dimension(width, yBot));
		setMaximumSize(new Dimension(width, 20000));		
		setBackground(Color.WHITE);
		
		addMouseListener(this);
		enableEvents( AWTEvent.MOUSE_EVENT_MASK );
	}
	
	public void setClef(int clef, int pos)
	{
		if(selectedlane != null)
		{
			
			MidiPart head = selectedlane.getHeadPart();
			
			ClefChange clef_event = null;
			
			Iterator<MultiEvent> iter = head.getMultiEvents().iterator();
			while (iter.hasNext()) {
				MultiEvent event = iter.next();
				if(event.getStartTick() > 0) break;
				if(event instanceof ClefChange)
				{
					clef_event = (ClefChange)event;
					break;
				}
			}
			
			if(clef_event == null)
			{
				clef_event = new ClefChange(head, 0);
				head.add(clef_event);
			}
			
			clef_event.clef_type = clef;
			clef_event.clef_pos = pos;
			clef_event.clef_octave = 0;
			
			notationEditor.repaintItems();
			
		}
	}
	
	
	MidiLane selectedlane = null;
	
	public void showPopup(MouseEvent event)
	{
		MidiLane lane = notationEditor.getLaneAtY(event.getY()-20+yScroll);
		if(lane == null) return;
		if(event.getX() > 45) return;
		
		selectedlane = lane;
		
		JPopupMenu popupmenu = new JPopupMenu();
		JMenuItem menuitem = new JMenuItem("Treble Clef (G)");
		menuitem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						setClef(NotationGraphics.CLEF_G, 2);
					}
				});
		popupmenu.add(menuitem);
		menuitem = new JMenuItem("Bass Clef (F)");
		menuitem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						setClef(NotationGraphics.CLEF_F, 6);
					}
				});
		popupmenu.add(menuitem);
		menuitem = new JMenuItem("Alto Clef (C)");
		menuitem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						setClef(NotationGraphics.CLEF_C, 4);
					}
				});
		popupmenu.add(menuitem);
		menuitem = new JMenuItem("Tenor Clef (C)");
		menuitem.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						setClef(NotationGraphics.CLEF_C, 6);
					}
				});
		popupmenu.add(menuitem);
		popupmenu.show(event.getComponent(), event.getX(), event.getY() );		
	}
	
	public void processMouseEvent( MouseEvent event )
	{
		if( event.isPopupTrigger() )
			showPopup(event);
		super.processMouseEvent( event );
	}	
	
	public void mouseClicked(MouseEvent event) {
		showPopup(event);
	}
	public void mousePressed(MouseEvent e) {
	}
	public void mouseReleased(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void paintComponent(Graphics g) {
		//Thread.yield();
		super.paintComponent(g);
		notationEditor.paintHeader(g, yScroll);
		
	}	
	public void adjustmentValueChanged(AdjustmentEvent e) {
		yScroll = e.getValue();
		repaint();
		
	}

}
