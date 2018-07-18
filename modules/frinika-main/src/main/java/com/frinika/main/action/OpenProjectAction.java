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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Action for opening project.
 *
 * @author peter
 */
public class OpenProjectAction extends AbstractAction {

    private final JFrame frame;

    public OpenProjectAction(@Nonnull JFrame frame) {
        this.frame = frame;
    }

    private final long serialVersionUID = 1L;
    private File selectedFile = null;

    public void setSelectedFile(@Nullable File file) {
        selectedFile = file;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(selectedFile);
        chooser.setDialogTitle(CurrentLocale.getMessage("project.menu.file.open_project.dialogtitle"));
        chooser.setFileFilter(new ProjectFileFilter());

        try {
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File projectFile = chooser.getSelectedFile();
                openProjectFile(projectFile);
            }
        } catch (Exception ex) {
            Logger.getLogger(OpenProjectAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void openProjectFile(@Nonnull File projectFile) throws Exception {
        FrinikaFrame projectFrame = new FrinikaFrame();
        ProgressOperation.openProjectFile(projectFrame, projectFile);
        FrinikaConfig.setLastProject(projectFile);
    }
}
