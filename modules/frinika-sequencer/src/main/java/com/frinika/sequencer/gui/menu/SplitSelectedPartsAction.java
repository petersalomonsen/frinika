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
import static com.frinika.localization.CurrentLocale.getMessage;
import com.frinika.sequencer.gui.ProjectFrame;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.tools.Tools;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;

public class SplitSelectedPartsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private ProjectFrame project;

    public SplitSelectedPartsAction(ProjectFrame project) {
        super(getMessage("sequencer.project.split_parts"));
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        //	JFrame frame=new JFrame();
        List<MidiPart> parts = new ArrayList<>();

        for (Part part : project.getProjectContainer().getPartSelection()
                .getSelected()) {
            if (part instanceof MidiPart) {
                parts.add((MidiPart) part);
            }
        }

        project.getProjectContainer().getEditHistoryContainer().mark(
                CurrentLocale.getMessage("sequencer.project.split_lane"));

        long ticksPerBeat = project.getProjectContainer().getSequence()
                .getResolution();

        List<MidiPart> newParts = Tools.splitParts(parts, ticksPerBeat);

        project.getProjectContainer().getPartSelection().setSelected(newParts);
        project.getProjectContainer().getEditHistoryContainer()
                .notifyEditHistoryListeners();
        project.getProjectContainer().getPartSelection().notifyListeners();
    }
}
