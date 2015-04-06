/*
 * Created on 15 Aug 2007
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

package com.frinika.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import uk.org.toot.audio.mixer.MixerControls;

import com.frinika.tootX.MixerControlsMidiStreamSnapshotAutomation;

public class TootMixerSerializer implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Do versioning the hard way
	int       version;
	ProjectContainer project;

	// transient MixerControls mixerControls;
	
	
	 private TootMixerSerializer() {
			
	}

	 TootMixerSerializer(ProjectContainer project) {
			this.project=project;
	 }
	
	
	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in)
			throws ClassNotFoundException, IOException {
		in.defaultReadObject();
//		System.out.println(" Mixer Save Version =" + version);
		loadMixer(in);	
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		version=1;
		out.defaultWriteObject();
		saveMixer(out);
	
	}

	
	private void loadMixer(InputStream in) {

	
	//	project.mixerControls = new MixerControls("Mixer");
		MixerControlsMidiStreamSnapshotAutomation snapshotAutomation = new MixerControlsMidiStreamSnapshotAutomation(
				project.mixerControls);

		snapshotAutomation.load(in);

	}

//	void setMixerControls(MixerControls mixerControls) {
//		this.mixerControls=mixerControls;
//	}
	
	

//	MixerControls getMixerControls() {
//		return mixerControls;
//	}
	
	
	private void saveMixer(OutputStream out) {
		MixerControlsMidiStreamSnapshotAutomation snapshotAutomation = new MixerControlsMidiStreamSnapshotAutomation(
				project.mixerControls);
		
		snapshotAutomation.store(out);
	}



}
