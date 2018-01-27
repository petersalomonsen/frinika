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

import com.frinika.global.Toolbox;
import com.frinika.localization.CurrentLocale;
import com.frinika.model.EditHistoryAction;
import com.frinika.sequencer.model.TextLane;
import com.frinika.sequencer.project.SequencerProjectContainer;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.*;

/**
 * Lane-view for text-lane.
 *
 * @author Jens Gulden
 */
public class TextLaneView extends LaneView implements ChangeListener {

    private static final long serialVersionUID = 1L;

    public final static String DELIMITER = "\n\n---\n\n";

    private JEditorPane editor;
    private SequencerProjectContainer project;
    private String textBackup = null;
    private boolean initialRefresh = true;

    public TextLaneView(TextLane lane, SequencerProjectContainer project) {
        super(lane);
        this.project = project;
        init();
        refreshFromTrack(); // with initialRefresh==true
        lane.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshFromTrack();
    }

    @Override
    protected void makeButtons() {
        this.setLayout(new BorderLayout());
        editor = new JEditorPane();
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textBackup = getTextNormalized();
            }

            @Override
            public void focusLost(FocusEvent e) {
                final String text = getTextNormalized();
                assert (textBackup != null); // rely on swing to always fire focusGained before focusLost
                if (!text.equals(textBackup)) {
                    final String localTextBackup = textBackup;
                    textBackup = text;
                    project.getEditHistoryContainer().mark(CurrentLocale.getMessage("sequencer.project.edit_text_lane"));
                    EditHistoryAction action = new EditHistoryAction() { // undo editing the whole text in textlaneview
                        @Override
                        public void redo() {
                            editor.setText(text);
                            updateToTrack();
                        }

                        @Override
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
        final String text = ((TextLane) lane).getAllText(DELIMITER);
        if (initialRefresh) {
            editor.setText(text);
            initialRefresh = false;
        } else {
            final String oldtext = editor.getText().trim();
            if (!oldtext.equals(text.trim())) {
                project.getEditHistoryContainer().mark(CurrentLocale.getMessage("sequencer.project.edit_text_lane"));
                EditHistoryAction action = new EditHistoryAction() { // undo editing an individual TextPart
                    @Override
                    public void redo() {
                        editor.setText(text);
                        updateToTrack();
                    }

                    @Override
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
        ((TextLane) lane).setAllText(text, DELIMITER);
        project.repaintPartView();
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
