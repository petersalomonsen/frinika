package com.frinika.benchmark;

import java.util.Vector;

import javax.sound.midi.ShortMessage;

import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.JavaSoundVoiceServer;
import com.frinika.voiceserver.Voice;
import com.frinika.voiceserver.VoiceServer;
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
public class RecordInputRoundTripLatencyTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		VoiceServer voiceServer = new AudioContext().getVoiceServer(); // Initialize the Audio system

		// Initialize a synth (MySampler)
        SynthRack synthRack = new SynthRack(voiceServer);
        MySampler sampler = new MySampler(synthRack);
        synthRack.setSynth(0,sampler);
        
        sampler.recordMode = MySampler.RECORDMODE_SINGLE_KEY;
        
        // Send one pulse out on the output that we will look for in the input
		class PulseVoice extends Voice {

			boolean first = true;
			@Override
			public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
				if(first)
					buffer[startBufferPos] = 0.2f;
				first = false;
			}
		};

		// Latencies for testing
		int[] latencies = {512,1024,2048,4096,8192};
		Vector<String> report = new Vector<String>();
		
		for(int latency : latencies)
		{
			if(AudioContext.getDefaultAudioContext().getVoiceServer() instanceof JavaSoundVoiceServer)
	        {
				((JavaSoundVoiceServer)voiceServer).setBufferSize(latency);
				System.out.println("Voiceserver latency: "+voiceServer.getTotalLatencyAsFrames()+" frames "+(voiceServer.getTotalLatency()/1000)+" ms");
	        }			

			// Run the test 3 times on the same line
			for(int count=0;count<3;count++)
			{
				if(AudioContext.getDefaultAudioContext().getVoiceServer() instanceof JavaSoundVoiceServer)
		        {
					// Start software monitoring
		            // Set the index to the audio in device you'll use
		            int audioDeviceIndex = 0;
		            throw new Exception("FIXME");
//		        	sampler.samplerOscillator.startMonitor(AudioHub.getAudioInHandles().get(audioDeviceIndex).getLine(),
//		        	AudioHub.getAudioInHandles().get(audioDeviceIndex).getFormat().getChannels()==2 ? true : false	);
		        }
		        else
		        	sampler.samplerOscillator.startMonitor(null,true);
		        
		        System.out.println("Your input is being monitored");
		        Thread.sleep(2000);
			        
		        long t1 = System.currentTimeMillis();
		        // Start recording
				ShortMessage shm = new ShortMessage();
				shm.setMessage(ShortMessage.NOTE_ON,0,36,100);
				synthRack.getReceiver().send(shm, -1);
				
				// Send pulse
				PulseVoice voice = new PulseVoice();
				voiceServer.addTransmitter(voice);
				System.out.println("record and pulse init time "+(System.currentTimeMillis()-t1));
				
				System.out.println("Recording");
				Thread.sleep(1000);
				
				
		        // Stop recording
				shm = new ShortMessage();
				shm.setMessage(ShortMessage.NOTE_ON,0,36,0);
				synthRack.getReceiver().send(shm, -1);
		
				System.out.println("Wait");
				
				Thread.sleep(2000);
		
				System.out.println("Calculate results");
				
				short[] samples = sampler.sampledSounds[36][0].getLeftSamples();
				int delta =0;
				delta = samples[0]-delta;
				int pos = -44100;
				for(int n = 0;n<samples.length;n++)
				{
					delta = samples[n]-delta;
					if(delta>1000)
					{
						System.out.println(n+" "+delta);
						pos = n;
						break;
					}
					
				}
				int roundtripLatency = (pos*1000 / voiceServer.getSampleRate());
				report.add("Voiceserver latency\t"+voiceServer.getTotalLatencyAsFrames()+"\tframes\t"+(voiceServer.getTotalLatency()/1000)+"\tms\t"+
				"roundtrip latency\t"+roundtripLatency+"\tmsecs\trtatency/vslatency\t"+(roundtripLatency*1000/(float)voiceServer.getTotalLatency())
				);
			}
		}
		System.out.println("\n\n--------------------------------------------------------\n Test Results:\n" +
				"Note that you must turn of latency compensation in SamplerOscillator.java to get correct roundtrip latency");
		for(String r : report)
		{
			System.out.println(r);
		}
		System.exit(0);
	}

}
