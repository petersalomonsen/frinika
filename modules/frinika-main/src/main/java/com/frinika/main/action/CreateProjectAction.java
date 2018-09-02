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

import com.frinika.main.FrinikaFrame;
import com.frinika.project.FrinikaProjectContainer;
import com.frinika.sequencer.gui.ProjectFrame;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;

public class CreateProjectAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    ProjectFrame frame;

    @Override
    public void actionPerformed(ActionEvent event) {

        try {
            frame = new FrinikaFrame();
            ((FrinikaFrame) frame).setProject(new FrinikaProjectContainer());
        } catch (Exception ex) {
            Logger.getLogger(OpenProjectAction.class.getName()).log(Level.SEVERE, null, ex);
        }

//        if (true) {
//            return;
//        }
//
////        File newProject = new File("New.frinika");
////        if (newProject.exists()) {
////            newProject.delete();
////            try {
////                newProject.createNewFile();
////            } catch (IOException e1) {
////                // TODO Auto-generated catch block
////                e1.printStackTrace();
////            }
////        }
//
//        try {
//            frame = new FrinikaFrame();
//            ((FrinikaFrame) frame).setProject(new FrinikaProjectContainer());
//        } catch (Exception ex) {
//            Logger.getLogger(OpenProjectAction.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        if (true) {
//            return;
//        }
//
//        try {
//            JFileChooser chooser = new JFileChooser();
//            chooser.setDialogTitle(CurrentLocale.getMessage("project.menu.file.new_project.dialogtitle"));
//            chooser.setFileFilter(new ProjectFileFilter());
//            //		if (project.getProjectFile() != null)
//            //			chooser.setSelectedFile(project.getProjectFile());
//            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                File projectFile = chooser.getSelectedFile();
//
//                if (chooser.getFileFilter() instanceof ProjectFileFilter) {
//                    if (!chooser.getFileFilter().accept(projectFile)) {
//                        projectFile = new File(projectFile.getPath() + ".frinika");
//                    }
//                }
//
//                frame = new FrinikaFrame();
//                ((FrinikaFrame) frame).setProject(FrinikaProjectContainer.loadProject(projectFile, null));
//                FrinikaConfig.setLastProject(projectFile);
//            }
//
//        } catch (Exception ex) {
//            Logger.getLogger(OpenProjectAction.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public ProjectFrame getProjectFrame() {
        return frame;
    }
}
