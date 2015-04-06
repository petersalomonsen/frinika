package com.frinika.codeexamples;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.sound.midi.ShortMessage;

import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.JavaSoundVoiceServer;
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
public class RecordAndPlay {

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
        
        sampler.recordMode = MySampler.RECORDMODE_SINGLE_KEY;
        
        if(AudioContext.getDefaultAudioContext().getVoiceServer() instanceof JavaSoundVoiceServer)
        {
            // Start software monitoring
            // Set the index to the audio in device you'll use
            int audioDeviceIndex = 2;
          
            throw new Exception("FIXME");
//        	sampler.samplerOscillator.startMonitor(AudioHub.getAudioInHandles().get(audioDeviceIndex).getLine(),
//        	AudioHub.getAudioInHandles().get(audioDeviceIndex).getFormat().getChannels()==2 ? true : false	);
        }
        else
        	sampler.samplerOscillator.startMonitor(null,true);
        
        System.out.println("Your input is being monitored");
		
        BufferedReader rd = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.println("Press enter to start recording");
        rd.readLine();
        
        // Start recording
		ShortMessage shm = new ShortMessage();
		shm.setMessage(ShortMessage.NOTE_ON,0,36,100);
		synthRack.getReceiver().send(shm, -1);

		System.out.println("Press enter to stop recording");
        rd.readLine();

        // Stop recording
		shm = new ShortMessage();
		shm.setMessage(ShortMessage.NOTE_ON,0,36,0);
		synthRack.getReceiver().send(shm, -1);

		System.out.println("Waiting one second to start playback");
		Thread.sleep(1000);
		System.out.println("Now playing back");
        // Play back
		shm = new ShortMessage();
		shm.setMessage(ShortMessage.NOTE_ON,0,36,100);
		synthRack.getReceiver().send(shm, -1);

	}

}
