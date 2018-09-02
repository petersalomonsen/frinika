/*
 * Created on 06-Jun-2006
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
package com.frinika.sequencer.gui;

import com.frinika.global.FrinikaConfig;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.tootX.midi.MidiInDeviceManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MidiInDevicePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    List<JCheckBox> boxes = new ArrayList<>();

    public MidiInDevicePanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // MidiDeviceHandle [] list= MidiHub.getMidiInHandles();
        Info infos[] = MidiSystem.getMidiDeviceInfo();

        List<String> names = new ArrayList<>();

        for (String name : FrinikaGlobalProperties.MIDIIN_DEVICES_LIST.getStringList()) {
            names.add(name);
        }

        ActionListener act = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> list = new ArrayList<>();
                for (JCheckBox box : boxes) {
                    if (box.isSelected()) {
                        list.add(box.getText());
                        // System.out.println(box.getText());
                    }
                }
                FrinikaGlobalProperties.MIDIIN_DEVICES_LIST.setStringList(list);
                FrinikaConfig.store();
                midiInDeviceChange();
            }
        };

        for (Info info : infos) {
            MidiDevice dev;
            try {
                dev = MidiSystem.getMidiDevice(info);

                if (dev.getMaxTransmitters() != 0) {
                    String str = dev.getDeviceInfo().toString();
                    JCheckBox box = new JCheckBox(str, names.contains(str));
                    boxes.add(box);
                    box.addActionListener(act);
                    add(box);
                }
            } catch (MidiUnavailableException e1) {
                // TODO Auto-generated catch block
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error opening mididevice " + info.getName(), e1);
            }
        }

        //			
        //			
        // for(final MidiDeviceHandle d:list){
        // if (d.getMidiDevice() == null) continue;
        // JCheckBox box=new
        // JCheckBox(d.toString(),names.contains(d.toString()));
        // boxes.add(box);
        // box.addActionListener(act);
        // add(box);
        // }
    }

    public static void main(String args[]) {

        JFrame f = new JFrame();
        f.setContentPane(new MidiInDevicePanel());
        f.pack();
        f.setVisible(true);
    }

    public static void midiInDeviceChange() {
        System.out.println("MIDIIN CHANGER");
        MidiInDeviceManager.reset(FrinikaGlobalProperties.MIDIIN_DEVICES_LIST.getStringList());
    }
}
