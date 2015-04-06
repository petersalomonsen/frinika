/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.gui.menu;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Vector;

import javax.swing.AbstractAction;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionFocusable;
import com.frinika.sequencer.model.Selectable;

public class CutAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private ProjectContainer project;
	
	public CutAction(ProjectContainer project) {
		super(getMessage("sequencer.project.cut"));
		this.project=project;		
	}

	@SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
			
		project.getEditHistoryContainer().mark(getMessage("sequencer.project.cut"));
		SelectionFocusable focus = project.getSelectionFocus();
		if (focus == null) {
			System.out.println(" Please set the foucs ");	
			return;
		}
		
		Collection<Selectable> list=focus.getObjects();
		if (list.size() == 0) {
			System.out.println(" Please select something");
			return;
		}
		
        if(focus instanceof SelectionContainer && ((SelectionContainer)focus).getSelectionStartTick()>-1)
        {    // Used by the tracker to position the selected data according to the selected row
            SelectionContainer selectionContainer = (SelectionContainer)focus;
            project.clipBoard().copy(list,selectionContainer.getSelectionStartTick(),selectionContainer.getSelectionLeftColumn(),project);
        }
        else
            project.clipBoard().copy(list,project);

        Vector<Selectable> list2=new Vector<Selectable>(list);
		for (Selectable it:list2) {
			it.removeFromModel();
		}
		
	//	project.getSelectionFocus().clearSelection();
		project.getEditHistoryContainer().notifyEditHistoryListeners();
	}
}
