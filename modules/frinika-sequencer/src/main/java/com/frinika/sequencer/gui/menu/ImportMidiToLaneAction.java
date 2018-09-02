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

import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.gui.ProjectFrame;
import com.frinika.tools.MidiFileFilter;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

public class ImportMidiToLaneAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private ProjectFrame project;

    public ImportMidiToLaneAction(ProjectFrame project) {
        super(CurrentLocale.getMessage("sequencer.project.import_midi_to_lanes"));
        this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        File midiFile = null;
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(CurrentLocale.getMessage("project.menu.file.import_midi"));
            chooser.setFileFilter(new MidiFileFilter());
            if (midiFile != null) {
                chooser.setSelectedFile(midiFile);
            }

            if (chooser.showOpenDialog(project.getFrame()) == JFileChooser.APPROVE_OPTION) {
                File newMidiFile = chooser.getSelectedFile();

                MidiDevice mididdevice = project.selectMidiDevice();

                project.getProjectContainer().createMidiLanesFromSequence(MidiSystem.getSequence(newMidiFile), mididdevice);

                midiFile = newMidiFile;
            }
        } catch (HeadlessException | IOException | InvalidMidiDataException ex) {
            ex.printStackTrace();
        }
    }
}
