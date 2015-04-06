/*
 * Created on Mar 7, 2006
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

package com.frinika.sequencer.gui.pianoroll;


import javax.swing.JPanel;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.Item;
import com.frinika.sequencer.gui.LabelFieldEditor;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.EditHistoryAction;
import com.frinika.sequencer.model.EditHistoryListener;
import com.frinika.sequencer.model.MultiEvent;

public class MultiEventEditPanel extends JPanel implements FeedbackEventListener,
		EditHistoryListener, SelectionListener<MultiEvent> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	LabelFieldEditor lab[];

	MultiEventTableModel model;

	ProjectContainer project;

	MultiEvent event = null;

	public MultiEventEditPanel(ProjectContainer project) {
		this.project = project;
		setOpaque(false);
		int ticksPerBeat = project.getSequencer().getSequence().getResolution();

		// setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		model = new MultiEventTableModel(project,null, 1, ticksPerBeat);
		// table = new JTable(tableModel);

		lab = new LabelFieldEditor[model.getColumnCount()];

		for (int i = 0; i < model.getColumnCount(); i++) {
			add(lab[i] = new LabelFieldEditor(model, i, project));
			lab[i].setOpaque(false);
		}
		// setBackground(Color.BLUE);
	}

	public void notifyFeedbackItemChanged(Item it) {
		if (!(it instanceof MultiEvent)) return;
		event = (MultiEvent)it;
		model.setMultiEvent(event);
		for (int i = 0; i < model.getColumnCount(); i++) {
			lab[i].update();
		}
	}

	public void fireSequenceDataChanged(EditHistoryAction[] edithistoryEntries) {
		notifyFeedbackItemChanged(event);
	}



	public void selectionChanged(SelectionContainer<? extends MultiEvent> src) {
		// TODO Auto-generated method stub
		MultiEvent ev = project.getMultiEventSelection().getFocus();
		if (event == ev)
			return;
		 notifyFeedbackItemChanged(ev);
	}
}
