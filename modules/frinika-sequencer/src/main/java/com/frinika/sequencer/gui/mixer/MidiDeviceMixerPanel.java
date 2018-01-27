/*
 * Created on Apr 15, 2006
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
package com.frinika.sequencer.gui.mixer;

import com.frinika.localization.CurrentLocale;
import com.frinika.sequencer.project.SynthesizerDescriptorIntf;
import com.frinika.sequencer.project.mididevices.gui.MidiDevicesPanel;
import com.frinika.synth.importers.soundfont.SoundFontFileFilter;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author Peter Johan Salomonsen
 */
public class MidiDeviceMixerPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public MidiChannelMixerSlot[] mixerSlots = new MidiChannelMixerSlot[16];

    public MidiDeviceMixerPanel(final MidiDevicesPanel panel, final SynthWrapper synthWrapper) {
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();

        if (synthWrapper.getRealDevice() instanceof Synthesizer) {
            JButton loadSoundbankButton = new JButton(CurrentLocale.getMessage("mididevices.loadsoundbank"));
            loadSoundbankButton.addMouseListener(new MouseAdapter() {
                /* (non-Javadoc)
                 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                 */
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setDialogTitle("Open soundfont");
                        chooser.setFileFilter(new SoundFontFileFilter());
                        if (chooser.showOpenDialog(null)
                                == JFileChooser.APPROVE_OPTION) {
                            File soundFontFile = chooser.getSelectedFile();
                            Soundbank soundbank = synthWrapper.getSoundbank(soundFontFile);
                            synthWrapper.loadAllInstruments(soundbank);
                            System.out.println("Soundbank loaded");
                            ((SynthesizerDescriptorIntf) panel.getProject().getMidiDeviceDescriptor(synthWrapper)).setSoundBankFileName(soundFontFile.getAbsolutePath());
                        };
                    } catch (HeadlessException | IOException | InvalidMidiDataException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            add(loadSoundbankButton, gc);

            MidiDevice dev = synthWrapper.getRealDevice();
            try {
                Method method = dev.getClass().getMethod("show");

                JButton showSettingsButton = new JButton(CurrentLocale.getMessage("mididevices.show"));
                showSettingsButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {

                        MidiDevice dev = synthWrapper.getRealDevice();
                        Method method;
                        try {
                            method = dev.getClass().getMethod("show");
                            method.invoke(dev);
                        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                add(showSettingsButton, gc);
            } catch (SecurityException | NoSuchMethodException e1) {
            }
        }

        JButton renameDeviceButton = new JButton(CurrentLocale.getMessage("mididevices.rename"));
        renameDeviceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String value = JOptionPane.showInputDialog(CurrentLocale.getMessage("mididevices.entername"));
                if (value == null) {
                    return;
                }
                panel.getProject().getMidiDeviceDescriptor(synthWrapper).setProjectName(value);
                panel.updateDeviceTabs();
            }

        });
        add(renameDeviceButton, gc);
        gc.gridwidth = GridBagConstraints.REMAINDER;

        JButton removeDeviceButton = new JButton(CurrentLocale.getMessage("mididevices.removedevice"));
        removeDeviceButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                panel.remove(synthWrapper);

            }
        });
        add(removeDeviceButton, gc);

        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.VERTICAL;
        gc.weighty = 1.0;
        for (int n = 0; n < mixerSlots.length; n++) {
            mixerSlots[n] = new MidiChannelMixerSlot(synthWrapper, synthWrapper.getChannels()[n]);
            add(mixerSlots[n], gc);
        }
        synthWrapper.gui = this;
    }
}
