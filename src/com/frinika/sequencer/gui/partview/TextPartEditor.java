/*
 * Created on Feb 1, 2007
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

package com.frinika.sequencer.gui.partview;

import com.frinika.global.FrinikaConfig;
import com.frinika.sequencer.model.TextPart;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.*;

/**
 * GUI-component for in-place editing a text-part inside the lane view.
 * 
 * Basically, this displays an editable JTextArea with the same size, font and color
 * right 'above' the displayed text-part in the lane-view. When editing stops (the
 * user presses return or escape), the component gets removed again.
 * 
 * @author Jens Gulden
 */
public class TextPartEditor extends JPanel implements FocusListener {

	TextPart part;
	private PartView partView;
	private JTextArea textArea;
	
	public TextPartEditor(TextPart part, PartView partView, Rectangle rect) {
		super();
		this.part = part;
		this.partView = partView;
		this.setLayout(new BorderLayout());
		textArea = new JTextArea();
		textArea.setFont( FrinikaConfig.TEXT_LANE_FONT );
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setText(part.getText());
		textArea.setBackground(Color.WHITE);
		textArea.addFocusListener(this);
		textArea.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e) {
				int k = e.getKeyCode();
				if (k == KeyEvent.VK_ESCAPE) {
					editCancel();
					e.consume();
				} else if (k == KeyEvent.VK_ENTER)  {
					if ( ! e.isShiftDown() ) { // return serves as "ok"
						editOK();
					} else { // shift-return serves as line-break
						textArea.setText( textArea.getText() + System.getProperty("line.separator") );
					}
					e.consume();
				}
				if ( ((k >= KeyEvent.VK_A) && (k <= KeyEvent.VK_Z)) ) { // || ((k == KeyEvent.VK_ENTER) && (e.isShiftDown())) ) { // ordinary letter key, or shift-return
					e.consume(); // avoid others (main frame) to do more with key-press, e.g. as menu-acceleraator etc.
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scrollPane, BorderLayout.CENTER);
		this.setSize(rect.width + 2, rect.height);
		this.setLocation(rect.x, rect.y + 20 ); // TODO +20?
		partView.add(this);
		textArea.requestFocus();
	}
	
	public void editOK() {
		part.setText(textArea.getText());
		LaneView voiceView = partView.getProjectFrame().getVoicePartViewSplitPane().laneView;
		if (voiceView instanceof TextLaneView) {
			((TextLaneView)voiceView).refreshFromTrack();
		}
		editCancel();
	}
	
	public void editCancel() {
		textArea.removeFocusListener(this);
		partView.remove(this);
		part.endInplaceEdit(partView);
		partView.repaintItems();
	}
	
	public void focusGained(FocusEvent e) {
		//nop
	}	

	public void focusLost(FocusEvent e) {
		editOK();
	}	
}