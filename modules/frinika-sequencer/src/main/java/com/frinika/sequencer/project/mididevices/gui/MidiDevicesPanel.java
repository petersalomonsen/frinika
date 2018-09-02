/*
 * Created on Mar 26, 2006
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.sequencer.project.mididevices.gui;

import com.frinika.sequencer.gui.mixer.MidiDeviceMixerPanel;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.project.AbstractProjectContainer;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.annotation.Nonnull;
import javax.sound.midi.MidiDevice;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

/**
 * @author Peter Johan Salomonsen
 */
@SuppressWarnings("serial")
public class MidiDevicesPanel extends JPanel {

    JTabbedPane deviceTabs;
    AbstractProjectContainer project;

    public MidiDevicesPanel(@Nonnull final AbstractProjectContainer project, @Nonnull final MidiDevicesHandler midiDevicesHandler) {
        this.project = project;

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();

        final JButton but = new JButton("New MIDI Out Device");
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JPopupMenu popup = new JPopupMenu();
                midiDevicesHandler.addMidiDevices(popup);
                popup.show(but, 0, 0);
            }

        });

        topPanel.add(but);

        add(topPanel, BorderLayout.NORTH);

        deviceTabs = new JTabbedPane();
        updateDeviceTabs();
        add(deviceTabs, BorderLayout.CENTER);
    }

    public void updateDeviceTabs() {
        deviceTabs.removeAll();
        for (MidiDevice midiDevice : project.getSequencer().listMidiOutDevices()) {
            String deviceTabName = project.getMidiDeviceDescriptor(midiDevice).getProjectName();
            deviceTabs.addTab(deviceTabName, ((SynthWrapper) midiDevice).getIcon(), new MidiDeviceMixerPanel(this, (SynthWrapper) midiDevice));
        }
    }

    /**
     * Remove a mididevice from the midi devices panel and the project
     *
     * @param synthWrapper
     */
    public void remove(MidiDevice synthWrapper) {
        project.removeMidiOutDevice(synthWrapper);
        updateDeviceTabs();
    }

    public AbstractProjectContainer getProject() {
        return project;
    }
    
    public interface MidiDevicesHandler {
        void addMidiDevices(@Nonnull JPopupMenu popup);
    }
}
