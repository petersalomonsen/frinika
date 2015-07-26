/*
 * Created on Jul 26, 2015
 *
 * Copyright (c) 2004-2015 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.web.rest;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;


/**
 *
 * @author Peter Johan Salomonsen
 */
public class MidiDeviceInfo {
    MidiDevice.Info[] infos;

    public MidiDeviceInfo() {
	ArrayList<MidiDevice.Info> midiInDevices = new ArrayList<>();
	for(MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
	    try {
		MidiDevice dev = MidiSystem.getMidiDevice(info);
		if (dev.getMaxTransmitters() == 0 ) {
		    continue;
		}
		midiInDevices.add(info);
	    } catch (MidiUnavailableException ex) {
		Logger.getLogger(MidiDeviceInfo.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	infos = midiInDevices.toArray(new MidiDevice.Info[0]);
    }
    

    public MidiDevice.Info[] getInfos() {
	return infos;
    }

    public void setInfos(MidiDevice.Info[] infos) {
	this.infos = infos;
    }                
}