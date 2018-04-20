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
import com.frinika.sequencer.gui.selection.SelectionFocusable;
import com.frinika.sequencer.model.Selectable;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.AbstractAction;

public class DeleteAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private AbstractProjectContainer project;

    public DeleteAction(AbstractProjectContainer project) {
        super(getMessage("sequencer.project.delete"));
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        project.getEditHistoryContainer().mark(getMessage("sequencer.project.delete"));
        SelectionFocusable focus = project.getSelectionFocus();
        if (focus == null) {
            System.out.println(" Please set the foucs ");
            return;
        }

        Collection<Selectable> list = focus.getObjects();
        if (list.isEmpty()) {
            System.out.println(" Please select something");
            return;
        }

        //	project.clipBoard().copy(list);
        Collection<Selectable> list2 = new ArrayList<>(list);
        for (Selectable it : list2) {
            it.removeFromModel();
        }
        project.getSelectionFocus().clearSelection();
        project.getEditHistoryContainer().notifyEditHistoryListeners();
    }
}
