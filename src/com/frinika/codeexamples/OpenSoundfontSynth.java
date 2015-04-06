package com.frinika.codeexamples;

import java.io.File;

import javax.sound.midi.ShortMessage;

import com.frinika.voiceserver.AudioContext;
import com.frinika.synth.SynthRack;
import com.frinika.synth.synths.MySampler;

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
 * Example of creating a standalone soft synth instance and sending midi events to it directly. This will open the soundfont Club.SF2 and
 * play a beat
 * 
 * @author Peter Johan Salomonsen
 */
public class OpenSoundfontSynth {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
			
		new AudioContext(); // Initialize the Audio system
		
		// Initialize a synth (MySampler)
        SynthRack synthRack = new SynthRack(AudioContext.getDefaultAudioContext().getVoiceServer());
        MySampler sampler = new MySampler(synthRack);
        synthRack.setSynth(0,sampler);
        
        /**
         * Open a drumkit soundfont
         */
        sampler.getImporter().getSoundFont(new File("soundfonts/Club.SF2"));
        sampler.getImporter().getInstrument(0);

        boolean evenBeat = true;
		
        // Add some reverb
        
        ShortMessage shm = new ShortMessage();
		shm.setMessage(ShortMessage.CONTROL_CHANGE,0,91,30);
		synthRack.getReceiver().send(shm, -1);
		
        /**
         * Play the beat
         */
        while(true)
		{
            // Kick
			shm = new ShortMessage();
			shm.setMessage(ShortMessage.NOTE_ON,0,36,100);
			synthRack.getReceiver().send(shm, -1);

            // Hat
            shm = new ShortMessage();
            shm.setMessage(ShortMessage.NOTE_ON,0,42,100);
            synthRack.getReceiver().send(shm, -1);

			Thread.sleep(300);

            // Open Hat
            shm = new ShortMessage();
            shm.setMessage(ShortMessage.NOTE_ON,0,46,100);
            synthRack.getReceiver().send(shm, -1);


            Thread.sleep(300);

            // Snare
            shm = new ShortMessage();
            shm.setMessage(ShortMessage.NOTE_ON,0,40,100);
            synthRack.getReceiver().send(shm, -1);

            // Hat
            shm = new ShortMessage();
            shm.setMessage(ShortMessage.NOTE_ON,0,42,100);
            synthRack.getReceiver().send(shm, -1);

            Thread.sleep(300);

            // Open Hat
            shm = new ShortMessage();
            shm.setMessage(ShortMessage.NOTE_ON,0,46,100);
            synthRack.getReceiver().send(shm, -1);

            Thread.sleep(150);
            
            // On even beats put in a kick for variation
            if(evenBeat)
            {
                // Kick
                shm = new ShortMessage();
                shm.setMessage(ShortMessage.NOTE_ON,0,36,70);
                synthRack.getReceiver().send(shm, -1);
            }
            evenBeat = !evenBeat;
            
            Thread.sleep(150);

		}
	}

}
