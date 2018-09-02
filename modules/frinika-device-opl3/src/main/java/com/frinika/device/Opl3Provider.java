/*
 * Created on Oct 01, 2006
 *
 * Copyright (c) 2006 Peter Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.device;

import com.frinika.voiceserver.VoiceServer;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.spi.MidiDeviceProvider;
import javax.swing.JFrame;

public class Opl3Provider extends MidiDeviceProvider {

    static Info deviceInfo = new Opl3Device.Opl3DeviceInfo();
    Info infos[];

    public Opl3Provider() {
        infos = new Info[1];
        infos[0] = deviceInfo;
    }

    @Override
    public Info[] getDeviceInfo() {
        return infos;
    }

    @Override
    public MidiDevice getDevice(Info arg0) {
        if (!(arg0 instanceof Opl3Device.Opl3DeviceInfo)) {
            return null;
        }

        return new Opl3Device(new VoiceServer() {
            @Override
            public void configureAudioOutput(JFrame frame) {
                // TODO Auto-generated method stub

            }
        });
        //return new SynthRack(FrinikaAudioSystem.getAudioServer(), null);
    }
}
