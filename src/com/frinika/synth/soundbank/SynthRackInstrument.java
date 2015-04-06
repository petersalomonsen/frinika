/*
 * Created on Jul 30, 2007
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

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;

import com.frinika.synth.Synth;

public class SynthRackInstrument extends Instrument {

	Synth synth;
	
	protected SynthRackInstrument(SynthRackSoundbank soundbank, Synth synth,Patch patch) {
		super(soundbank, patch, null,null);
		this.synth = synth;
	}

	@Override
	public Object getData() {
		return (SynthRackInstrumentIF)synth.getSettings();
	}
	
	@Override
	public String getName() {
		return ((SynthRackInstrumentIF)synth.getSettings()).getInstrumentName();
	}
	
	@Override
	public Class<?> getDataClass() {
		return synth.getSettings().getClass();
	}
	
}
