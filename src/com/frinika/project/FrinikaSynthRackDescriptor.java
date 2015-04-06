package com.frinika.project;

/*
 * Created on Sep 22, 2006
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

import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;

import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.synth.SynthRack;
import com.frinika.synth.settings.SynthSettings;

/**
 * @author Peter Johan Salomonsen
 */
public class FrinikaSynthRackDescriptor extends MidiDeviceDescriptor implements SoundBankNameHolder {
	private static final long serialVersionUID = 1L;

	SynthSettings synthSetup;
	String soundBankFileName;


	public FrinikaSynthRackDescriptor(SynthWrapper midiDevice) {
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
    
	/**
	 * Fix the lane program change event for older projects
	 */
	public static void fixLaneProgramChange(ProjectContainer project, MidiDevice midiDevice)
	{
		for(Lane lane : project.getLanes())
		{
			if(MidiLane.class.isInstance(lane))
			{
				MidiLane midiLane = ((MidiLane)lane);
				System.out.println("MDCH: "+midiLane.getMidiDeviceIndex()+" "+midiLane.getStoredMidiChannel());
				if(midiLane.getMidiDeviceIndex()!=null && midiLane.getStoredMidiChannel()>-1 && project.getSequencer().listMidiOutDevices()
						.get(midiLane.getMidiDeviceIndex())==midiDevice)
					midiLane.setProgram(midiLane.getStoredMidiChannel(),0,0);
			}
		}
	}
	
	@Override
	protected void installImp(ProjectContainer project) {
		midiDevice = new SynthWrapper(project,new SynthRack(null));
		((SynthRack)((SynthWrapper)midiDevice).getRealDevice()).loadSynthSetup(synthSetup);
		try {
			project.loadMidiOutDevice(this);
			/**
			 * Fix the lane program change event for older projects
			 */
			if(!synthSetup.hasProgramChangeEvent())
				fixLaneProgramChange(project,midiDevice);
			
		} catch (MidiUnavailableException e) {
			System.out.println("Unable to install Frinika synthRack");
			e.printStackTrace();
		}
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		/**
		 * Retrieve synth settings just before save
		 */
		synthSetup = ((SynthRack)((SynthWrapper)midiDevice).getRealDevice()).getSynthSetup();
		out.defaultWriteObject();
	}
}
