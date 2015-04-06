package com.frinika.codeexamples;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;



import com.frinika.voiceserver.AudioContext;
import com.frinika.global.FrinikaConfig;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.AudioLane;
import com.frinika.sequencer.model.MidiLane;

/*
 * Created on Mar 8, 2006
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
 * A simple quick launch program to use when playing a midi piano and recording from 
 * a microphone. Will use your default set up Midi/Audio in devices, and record a Midi (midi piano)
 * and audio (microphone - mono) track.
 * 
 * Frinika QuickSession is meant to be a "sketchpad" that launches quickly, and has 
 * a simple template for recording instant ideas.
 * 
 * In order to add more instruments to a sketch, it has to be aligned into a beat/tempo.
 * Rather than playing to a metronome, future features will be a possibility to mark
 * bar/beat points, so that a tempo curve will be resolved automatically based on the
 * marked points. This will then require the sequencer to support tempo changes.
 *  
 * @author Peter Johan Salomonsen
 */
public class QuickSession {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
			
		new AudioContext(); // Initialize the Audio system
		
		/**
		 * Create a project with an Audio lane and a Midi lane
		 */
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		ProjectContainer project = ProjectContainer.loadProject(new File(fmt.format(new Date())+".frinika"));
		AudioLane audioLane = project.createAudioLane();
		
		// TOOT_FIXME
		// audioLane.setAudioInDevice(AudioHub.getAudioInHandles().get(1));
		
		//Create a MidiLane and set the Midi out device
		
		MidiLane midiLane = project.createMidiLane();
		Vector<String> midiInList = FrinikaConfig.getMidiInDeviceList();
		// Use same midi out as midi in
		throw new  Exception("FIXME");
//		SynthWrapper midiOut = new SynthWrapper(null,MidiHub.getMidiOutDeviceByName(midiInList.get(0)));
//		midiOut.open();
//		project.addMidiOutDevice(midiOut);
//		midiLane.getTrack().setMidiDevice(midiOut);
//		
//		audioLane.setRecording(true);
//		midiLane.setRecording(true);
		
//		midiLane.setProgram(0, 0, 0);
//		new ProjectFrame(project);
		
		/*
		FrinikaSequencer sequencer = project.getSequencer();
		sequencer.startRecording();
		Thread.sleep(5000);
		sequencer.stopRecording();
		sequencer.setTickPosition(0);
		sequencer.start();*/
	}

}
