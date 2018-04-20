/*
 * Created on Sep 21, 2010
 *
 * Copyright (c) 2010 Frinika
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
package com.frinika.sequencer.gui;

import com.frinika.global.ProjectFrameIntf;
import com.frinika.sequencer.gui.partview.VoicePartViewSplitPane;
import com.frinika.sequencer.project.AbstractProjectContainer;
import com.frinika.sequencer.project.mididevices.gui.MidiDevicesPanel;
import com.frinika.tootX.midi.MidiLearnIF;
import java.awt.Component;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;

/**
 * This is basic interface derived from FrinikaFrame.
 */
public interface ProjectFrame extends ProjectFrameIntf {

    /**
     * File-filter for midi standard files. To be used with promptFile().
     *
     * @see promptFile
     */
    public static final String[][] FILE_FILTER_MIDIFILES = new String[][]{
        {"mid", "Midi standard files"}
    };

    MidiLearnIF getMidiLearnIF();

    void resetViews();

    @Override
    void repaintViews();

    void repaintPartView();

    void initViews();

    void addMidiDevices(JComponent menu, List<MidiDevice.Info> infos,
            List<Icon> icons);

    void addMidiDevices(JComponent menu);

    MidiDevice selectMidiDevice();

    AbstractProjectContainer getProjectContainer();
    
    boolean hasChanges();

    /**
     *
     * @param string
     * @deprecated
     */
    void infoMessage(String string);

    MidiDevicesPanel getMidiDevicesPanel();

    VoicePartViewSplitPane getVoicePartViewSplitPane();

    void setStatusBarMessage(String msg);

    void showRightButtonPartPopup(Component invoker, int x, int y);

    /**
     * Get swing frame if available
     */
    @Override
    JFrame getFrame();

    JPopupMenu getNewLaneMenu();

    /**
     * @return the partViewEditor
     */
    VoicePartViewSplitPane getPartViewEditor();
}
