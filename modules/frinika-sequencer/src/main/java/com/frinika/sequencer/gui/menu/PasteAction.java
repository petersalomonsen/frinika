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

import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionFocusable;
import com.frinika.sequencer.project.SequencerProjectContainer;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class PasteAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private SequencerProjectContainer project;

    public PasteAction(SequencerProjectContainer project) {
        super(CurrentLocale.getMessage("sequencer.project.paste"));
        this.project = project;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {

        project.getEditHistoryContainer().mark(
                CurrentLocale.getMessage("sequencer.project.paste"));

        SelectionFocusable focus = project.getSelectionFocus();
        if (focus instanceof SelectionContainer && ((SelectionContainer) focus).getSelectionStartTick() > -1) {    // Used by the tracker to position the pasted data according to the focus row
            SelectionContainer selectionContainer = (SelectionContainer) focus;
            project.clipBoard().paste(selectionContainer.getSelectionStartTick(), selectionContainer.getSelectionLeftColumn(), true, project);
        } else {
            project.clipBoard().paste(project);
        }
        project.getEditHistoryContainer().notifyEditHistoryListeners();
    }
}
