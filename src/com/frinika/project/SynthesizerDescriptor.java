package com.frinika.project;

import java.io.File;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

import com.frinika.sequencer.gui.mixer.SynthWrapper;
/*
 * Created on Sep 19, 2006
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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


/**
 * A serializable representation of a Synthesizer Midi Device used in a Frinika project. Contains
 * resources neccesary for re-opening the correct midi device.
 * @author Peter Johan Salomonsen
 */
public class SynthesizerDescriptor extends MidiDeviceDescriptor implements SoundBankNameHolder {
	
	private static final long serialVersionUID = 1L;

	String soundBankFileName;
	
	public SynthesizerDescriptor(Synthesizer midiDevice) {
		super(midiDevice);	
	}

	/**
	 * Get the filename for the loaded soundbank
	 * @return
	 */
	public String getSoundBankFileName() {
		return soundBankFileName;
	}
	
	/**
	 * Set the filename for the loaded soundbank
	 * @param soundBankFileName
	 */
	public void setSoundBankFileName(String soundBankFileName) {
		this.soundBankFileName = soundBankFileName;
	}
	
	@Override
	protected void installImp(ProjectContainer project) {
		super.installImp(project);
		if(soundBankFileName != null)
		try
		{			
			Soundbank soundbank;
			if(midiDevice instanceof SynthWrapper)
				soundbank = ((SynthWrapper)midiDevice).getSoundbank(new File(soundBankFileName));
			else
				soundbank = MidiSystem.getSoundbank(new File(soundBankFileName));			
			((Synthesizer)midiDevice).loadAllInstruments(soundbank);
			System.out.println("Soundbank loaded");
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
