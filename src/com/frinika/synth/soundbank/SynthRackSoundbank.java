/*
 * Created on Jul 23, 2007
 *
 * Copyright (c) 2004-2007 Peter Johan Salomonsen
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

package com.frinika.synth.soundbank;

import java.util.HashMap;
import java.util.Map;

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

import com.frinika.synth.Synth;

/**
 * 
 * @author Peter Johan Salomonsen
 *
 */
public class SynthRackSoundbank implements Soundbank {

	Map<Integer,Instrument> instruments = new HashMap<Integer,Instrument>();
	
	public static int getInstrumentMapKey(Patch patch)
	{
		return patch.getBank()*128+patch.getProgram();
	}
	
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public Instrument getInstrument(Patch patch) {
		return instruments.get(getInstrumentMapKey(patch));
	}

	public Instrument[] getInstruments() {
		return instruments.values().toArray(new Instrument[instruments.size()]);
	}

	public String getName() {
		return "Frinika Synthrack soundbank";
	}

	public SoundbankResource[] getResources() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVendor() {
		return "petersalomonsen.com";
	}

	public String getVersion() {
		return "0.5";
	}

	public void createAndRegisterInstrument(Patch patch, Synth synth) {
		instruments.put(getInstrumentMapKey(patch),new SynthRackInstrument(this,synth,patch));
	}

}
