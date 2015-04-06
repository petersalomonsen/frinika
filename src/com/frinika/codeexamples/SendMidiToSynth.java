package com.frinika.codeexamples;

import javax.sound.midi.ShortMessage;

import com.frinika.voiceserver.AudioContext;
import com.frinika.synth.SynthRack;
import com.frinika.synth.synths.Analogika;

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
 * Example of creating a standalone soft synth instance and sending midi events to it directly
 * @author Peter Johan Salomonsen
 */
public class SendMidiToSynth {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
			
		new AudioContext(); // Initialize the Audio system
		
		// Load a project - we will use the instruments from an example project
        SynthRack synthRack = new SynthRack(AudioContext.getDefaultAudioContext().getVoiceServer());
        synthRack.setSynth(0,new Analogika(synthRack));

		
		// Play notes 30-90
		for(int n=30;n<90;n++)
		{
			ShortMessage shm = new ShortMessage();
			shm.setMessage(ShortMessage.NOTE_ON,0,n,100);
			synthRack.getReceiver().send(shm, -1);
			
			shm = new ShortMessage();
			// Send Note OFF to previous note (NOTE_ON with vel 0)
			shm.setMessage(ShortMessage.NOTE_ON,0,n-1,0);
			synthRack.getReceiver().send(shm, -1);

			Thread.sleep(1000);
		};
	}

}
