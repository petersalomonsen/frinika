/*
 * Created on 23-Jun-2006
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
package com.frinika.sequencer.gui.menu;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.AudioLane;
import com.frinika.sequencer.model.AudioPart;
import com.frinika.sequencer.model.Lane;

public class ImportAudioAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private ProjectFrame project;

    public ImportAudioAction(ProjectFrame project) {
        super(getMessage("sequencer.project.import_audio"));
        this.project = project;
    }

    public void actionPerformed(ActionEvent arg0) {
        // project.
        Lane lane = project.getProjectContainer().getLaneSelection().getFocus();
        boolean allowMulti = false;

        if (!(lane instanceof AudioLane)) {
            project.infoMessage("Creating new AudioLane before importing!");
            allowMulti = true;  // false if lane is null
        }


        try {

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(getMessage("project.menu.file.import_audio.dialogtitle"));
            chooser.setFileFilter(new WavFileFilter());

            if (!allowMulti) {

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    File wavFile = chooser.getSelectedFile();
                    long milliSecPos = project.getProjectContainer().getSequencer().getMicrosecondPosition();
                    System.out.println(" milli sec pos = " + milliSecPos);

                    if (wavFile.exists()) {
                        new AudioPart(lane, wavFile, milliSecPos);
                    }
                }
            } else {
                chooser.setMultiSelectionEnabled(allowMulti);
                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    final File wavFiles[] = chooser.getSelectedFiles();
                    final long milliSecPos = project.getProjectContainer().getSequencer().getMicrosecondPosition();
                    System.out.println(" milli sec pos = " + milliSecPos);

//					Thread t=new Thread(new Runnable(){
//
//						public void run() {
//	

                    for (File wavFile : wavFiles) {
                        System.out.println(" Importing:" + wavFile);
                        if (wavFile.exists()) {
                            project.getProjectContainer().getEditHistoryContainer().mark(
                                    getMessage("sequencer.project.import_audio"));
                            Lane lane1 = project.getProjectContainer().createAudioLane();
                            System.out.println(" created lane  ");
                            new AudioPart(lane1, wavFile, milliSecPos);
//									try {
//										Thread.sleep(100);
//									} catch (InterruptedException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
                            String name = wavFile.getName();

                            int dotInd = name.lastIndexOf('.');

                            if (dotInd > 0) {
                                name = name.substring(0, dotInd);
                            }
                            lane1.setName(name);
                            project.getProjectContainer().getEditHistoryContainer().notifyEditHistoryListeners();

                        }
                        System.out.println(" OK ");
                    }

//						}
//					});
//					
//					t.start();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        // TODO in here

    }
}
