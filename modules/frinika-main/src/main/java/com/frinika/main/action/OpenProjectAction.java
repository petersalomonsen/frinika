/*
 * Created on 03-Aug-2006
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
package com.frinika.main.action;

import com.frinika.global.FrinikaConfig;
import com.frinika.localization.CurrentLocale;
import com.frinika.main.FrinikaFrame;
import com.frinika.main.ProgressOperation;
import com.frinika.tools.ProjectFileFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

/**
 * Action for opening project.
 *
 * @author peter
 */
public class OpenProjectAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private static final JFileChooser chooser = new JFileChooser();

    static {
        chooser.setDialogTitle(CurrentLocale.getMessage("project.menu.file.open_project.dialogtitle"));
        chooser.setFileFilter(new ProjectFileFilter());
    }

    public static void setSelectedFile(File file) {
        chooser.setSelectedFile(file);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File newProject = chooser.getSelectedFile();

                FrinikaFrame frame = new FrinikaFrame();
                ProgressOperation.openProjectFile(frame, newProject);
                FrinikaConfig.setLastProject(newProject);
            }
        } catch (Exception ex) {
            Logger.getLogger(ProgressOperation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
