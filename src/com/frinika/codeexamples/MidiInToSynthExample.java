package com.frinika.codeexamples;

import java.io.File;

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
 * Example of creating a standalone soft synth instance and connecting ot to a midi in device.
 * play a beat
 * 
 * @author pjl
 * @author Peter Johan Salomonsen
 */
public class MidiInToSynthExample {

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
         * Open a drumkit soundfont DRUMS located at C3 D3 E3 and round about
         */
        sampler.getImporter().getSoundFont(new File("soundfonts/Club.SF2"));
        sampler.getImporter().getInstrument(0);

  
        throw new Exception("FIXME");
//    	MidiDeviceHandle  [] list= MidiHub.getMidiInHandles();
// 
//    	MidiDeviceHandle midiInHandle = (MidiDeviceHandle)JOptionPane.showInputDialog(null,"Select MIDI IN","select",JOptionPane.INFORMATION_MESSAGE,null,list,list[0]);
//    	
//    	MidiDevice dev =midiInHandle.getMidiDevice();
//    	
//    	dev.open();
//    	Transmitter trans = dev.getTransmitter();
//    	
//    	trans.setReceiver(synthRack.getReceiver());
//    	
//    	Thread.sleep(200);

	}

}
