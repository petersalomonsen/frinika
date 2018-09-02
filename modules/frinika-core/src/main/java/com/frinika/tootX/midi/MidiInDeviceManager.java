/*
 * Created on 9.3.2007
 *
 * Copyright (c) 2007 Karl Helgason
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
package com.frinika.tootX.midi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;

/**
 * PJL extended to allow routing of multiple input devices. Sends messages from
 * all input devices with device Info to focus project.
 */
public class MidiInDeviceManager {

    static boolean isOpen = false;
    static List<DeviceReceiver> deviceReceivers = new ArrayList<DeviceReceiver>();
    static MidiConsumer project; // TODO make this an interface

    /**
     * Used to forward messages to the project with the associated device info
     */
    static public class DeviceReceiver implements Receiver {

        Info devInfo;
        private MidiDevice device;

        DeviceReceiver(MidiDevice device) {
            this.device = device;
            this.devInfo = device.getDeviceInfo();
        }

        @Override
        public void send(MidiMessage arg0, long arg1) {
            if (project == null) {
                return;
            }
            project.processMidiMessageFromDevice(devInfo, arg0, arg1);
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public MidiDevice getDevice() {
            return device;
        }
    }

    static public void setProject(MidiConsumer proj) {
        MidiInDeviceManager.project = proj;
    }

    // Call this method when recording is needed on some lane
    // Midi In devices aren't opened until they are needed!!!
    public static void open(Collection<String> openMidiIn) {
        if (isOpen) {
            return;
        }

        List<String> names = new ArrayList<>();

        for (String name : openMidiIn) { //FrinikaConfig.getMidiInDeviceList()) {
            names.add(name);
        }

        Info infos[] = MidiSystem.getMidiDeviceInfo();

        for (Info info : infos) {
            MidiDevice dev;
            String str = info.toString();
            if (names.contains(str)) {
                try {
                    dev = MidiSystem.getMidiDevice(info);
                    if (dev.getMaxTransmitters() != 0) {
                        {
                            DeviceReceiver recv = new DeviceReceiver(dev);
                            deviceReceivers.add(recv);

                            dev.open();
                            dev.getTransmitter().setReceiver(recv);
                            // active_midiindevices.add(dev);
                        }
                    }
                } catch (MidiUnavailableException e1) {
                    e1.printStackTrace();
                }
            }
        }

        isOpen = true;

    }

    // Reset Midi In Devices if they have been opened 
    public static void reset(Collection<String> names) {
        if (!isOpen) {
            return;
        }
        close();
        open(names);
    }

    // Close all midi in devices
    public static void close() {
        if (!isOpen) {
            return;
        }
        deviceReceivers.forEach((devRecv) -> {
            try {
                devRecv.getDevice().close();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        deviceReceivers.clear();
        isOpen = false;
    }

//    /**
//     * PJL just a hack to get a input mididevice ... please do not use this method
//     * @return
//     */
//    public static MidiDevice getMidiInDevice() {
//
//
//        String name = FrinikaConfig.getMidiInDeviceList().get(0);
//
//        Info infos[] = MidiSystem.getMidiDeviceInfo();
//
//        for (Info info : infos) {
//            MidiDevice dev;
//            String str = info.toString();
//            if (name.equals(str)) {
//                try {
//                    dev = MidiSystem.getMidiDevice(info);
//                    return dev;
//                } catch (Throwable e1) {
//                    e1.printStackTrace();
//                }
//            }
//        }
//
//        return null;
//
//
//    }
    static public List<DeviceReceiver> getOpenDeviceReceivers() {
        return deviceReceivers;
    }
}
