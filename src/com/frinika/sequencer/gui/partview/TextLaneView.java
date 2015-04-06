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

import static com.frinika.localization.CurrentLocale.getMessage;

import com.frinika.global.Toolbox;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.EditHistoryAction;
import com.frinika.sequencer.model.TextLane;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;

/**
 * Lane-view for text-lane.
 *  
 * @author Jens Gulden
 */
public class TextLaneView extends LaneView implements ChangeListener {
	
	private static final long serialVersionUID = 1L;

	public final static String DELIMITER = "\n\n---\n\n"; 
	
	private JEditorPane editor;
	private ProjectFrame frame;
	private String textBackup = null;
	private boolean initialRefresh = true;
	
	public TextLaneView(TextLane lane, ProjectFrame frame) {
		super(lane);
		this.frame = frame;
		init();
		refreshFromTrack(); // with initialRefresh==true
		lane.addChangeListener(this);
	}
	
	public void stateChanged(ChangeEvent e) {
		refreshFromTrack();
	}

	protected void makeButtons() {
		this.setLayout(new BorderLayout());
		editor = new JEditorPane();
		editor.addFocusListener(new FocusAdapter(){
			public void focusGained(FocusEvent e) {
				textBackup = getTextNormalized();
			}
			public void focusLost(FocusEvent e) {
				final String text = getTextNormalized();
				assert (textBackup != null); // rely on swing to always fire focusGained before focusLost
				if (!text.equals(textBackup)) {
					final String localTextBackup = textBackup; 
					textBackup = text;
					ProjectContainer project = frame.getProjectContainer();
					project.getEditHistoryContainer().mark(getMessage("sequencer.project.edit_text_lane"));
					EditHistoryAction action = new EditHistoryAction() { // undo editing the whole text in textlaneview
						public void redo() {
							editor.setText(text);
							updateToTrack();
						}
						
						public void undo() {
							editor.setText(localTextBackup);
							updateToTrack();
						}
					};
					action.redo(); // do it
					project.getEditHistoryContainer().push(action);
					project.getEditHistoryContainer().notifyEditHistoryListeners();
				} else { // textBackup equals text in normalized version
					editor.setText(text); // make sure normalized version is displayed
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(editor, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	public void refreshFromTrack() {
		final String text = ((TextLane)lane).getAllText(DELIMITER);
		if (initialRefresh) {
			editor.setText(text);
			initialRefresh = false;
		} else {
			final String oldtext = editor.getText().trim();
			if (!oldtext.equals(text.trim())) {
				ProjectContainer project = frame.getProjectContainer();
				project.getEditHistoryContainer().mark(getMessage("sequencer.project.edit_text_lane"));
				EditHistoryAction action = new EditHistoryAction() { // undo editing an individual TextPart
					public void redo() {
						editor.setText(text);
						updateToTrack();
					}
					
					public void undo() {
						editor.setText(oldtext);
						updateToTrack();
					}
				};
				action.redo(); // do it
				project.getEditHistoryContainer().push(action);
				project.getEditHistoryContainer().notifyEditHistoryListeners();
			}
		}
	}
	
	public void updateToTrack() {
		String text = editor.getText();
		((TextLane)lane).setAllText(text, DELIMITER);
		frame.repaintPartView();
	}
	
	protected String getTextNormalized() {
		String s = editor.getText();
		String delim = DELIMITER.trim(); // (without \n)
		List<String> l = Toolbox.splitString(s, delim);
		String t = Toolbox.joinStrings(l, DELIMITER);
		return t;
	}
	
	/*public void setActive(boolean active) {
		//editor.setEnabled(active);
		editor.setEditable(active);
		if (active) {
			refreshFromTrack();
			//editor.setColor(Color.BLACK);
			editor.setBackground(Color.WHITE);
		} else {
			//editor.setColor(Color.DARK_GRAY);
			editor.setBackground(Color.LIGHT_GRAY);
		}
	}
	
	public boolean isActive() {
		return editor.isEditable();
	}*/
}
