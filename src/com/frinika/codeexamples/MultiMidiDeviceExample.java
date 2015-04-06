package com.frinika.codeexamples;

import com.frinika.voiceserver.AudioContext;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.synth.SynthRack;
import com.frinika.synth.synths.Analogika;

/*
 * Created on Mar 9, 2006
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
 * Example of using an external MIDI device along with the buildt in softsynth.
 * 
 * Note: Frinika softsynths will not coexist with the default Javasound softsynth.
 * 
 * @author Peter Johan Salomonsen
 */
public class MultiMidiDeviceExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception  {
		// Create the audio context
		new AudioContext();
		// Create the project container
		ProjectContainer proj = new ProjectContainer();
		// Create a lane
		MidiLane lane = proj.createMidiLane();
		
	
        MidiPart part=new MidiPart(lane);
        
        // Add some notes
        part.add(new NoteEvent(part, 0,60, 100, 0, 128));
        part.add(new NoteEvent(part, 128,61, 100, 0, 128));
        part.add(new NoteEvent(part, 256,62, 100, 0, 128));
        part.add(new NoteEvent(part, 512,63, 100, 0, 128));
        part.add(new NoteEvent(part, 768,64, 100, 0, 128));
        
        // Initialize the Analogika softsynth on the mySynth stack
        // TODO: Rename mySynth to FrinikaSynthStack or similar
        SynthRack synthRack = new SynthRack(AudioContext.getDefaultAudioContext().getVoiceServer());
        synthRack.setSynth(0,new Analogika(synthRack));

        lane.getTrack().setMidiDevice(synthRack);
        // Create a second Lane for another MIDI device
		// Create a lane
		lane = proj.createMidiLane();

		// Create a MultiEventGroup and MidiPart
	
        part =new MidiPart(lane);
        
        // Add some notes (Some more than last time :) )
        part.add(new NoteEvent(part, 0,60, 100, 0, 128));
        part.add(new NoteEvent(part, 64,72, 100, 0, 128));
        part.add(new NoteEvent(part, 128,61, 100, 0, 128));
        part.add(new NoteEvent(part, 192,73, 100, 0, 128));
        part.add(new NoteEvent(part, 256,62, 100, 0, 128));
        part.add(new NoteEvent(part, 512,63, 100, 0, 128));
        part.add(new NoteEvent(part, 768,64, 100, 0, 128));

        // Create a transmitter for another MIDI device
        
        /*
         * On my system the MIDI IN port is index 0 and MIDI out is index 1
         * Change the index corresponding to your MIDI out device
         */
        	//   int index = 2;
        	//  System.out.println("Connecting to: "+MidiSystem.getMidiDeviceInfo()[index].getName());
         	// MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[index]);
     
        /* 
         * On my system I have a few midiout devices so I have added this code to select the one to use (PJL)
         */
        
        /** FIXME    
        MidiDeviceHandle recv[]=MidiHub.getMidiOutHandles();     
        MidiDeviceHandle r=(MidiDeviceHandle)JOptionPane.showInputDialog(null,"Select","midiout device",JOptionPane.INFORMATION_MESSAGE,null,recv,recv[0]);
        final MidiDevice externMidi = r.getMidiDevice();           
        externMidi.open();

        proj.getSequencer().addMidiOutDevice(externMidi);
        	// Set up the track to use the transmitter created 
        lane.getTrack().setMidiDevice(externMidi);
        
        // Start playing sequence (Uncomment the following line)
        // proj.getSequencer().start();
        
        // Show the project frame (you can comment out this - if you only want to play)
        new ProjectFrame(proj);
		END FIXME */
	}

}
