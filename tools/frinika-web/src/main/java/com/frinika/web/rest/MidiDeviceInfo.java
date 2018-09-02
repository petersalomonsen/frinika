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

import com.frinika.global.FrinikaConfig;
import java.util.Arrays;
import java.util.Vector;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class MidiDeviceInfo {
    public String[] getAvailableMidiInDevices() {
	return FrinikaConfig.getAvailableMidiInDevices().toArray(new String[0]);
    }

    public void setAvailableMidiInDevices(String[] infos) {
	
    }          
    
    public String[] getCurrentMidiInDevices() {
	return FrinikaConfig.getMidiInDeviceList().toArray(new String[0]);
    }
    
    public void setCurrentMidiInDevices(String[] midiInDevices) {
	FrinikaConfig.setMidiInDeviceList(new Vector<String>(Arrays.asList(midiInDevices)));
    }
}