package com.frinika.codeexamples;


import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.VoiceInterrupt;
import com.frinika.voiceserver.VoiceServer;
import com.frinika.voiceserver.voicetemplate.SynchronizedVoice;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;

import com.frinika.sequencer.SongPositionListener;

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
 * Simple example of audio playback within Frinika. This is just meant to be a guidance of how to implement audio
 * playback. It's not intended that the audio playback voice should be constantly present, like it is in this example.
 * This is only for testing purposes.
 * 
 * Replace the audio clip file with a file of your own.
 * 
 * A good test case, is an export of a project to wav, and play it along with the metronome (remember to have the same tempo).
 * 
 * @author Peter Johan Salomonsen
 */
public class AudioPlayBackExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Create the audio context
		final VoiceServer voiceServer = new AudioContext().getVoiceServer();
		
//		 Set up a synchronized voice
		class AudioTestVoice extends SynchronizedVoice {
			
			AudioInputStream ais;
			
			boolean running = false;
			
			byte[] byteBuffer = null;
			
			public AudioTestVoice(VoiceServer voiceServer, int i) throws Exception {
				super(voiceServer,i);
				reopenStream();
			}

			/**
			 * Probably not the best way to reset the stream.
			 */
			void reopenStream() throws Exception
			{
				// Replace this with a clip of your own (NOTE: This example assumes a STEREO 16 bit wav)
				//ais = AudioSystem.getAudioInputStream(new File("/home/peter/mystudio/20060618.wav"));
				ais = AudioSystem.getAudioInputStream(new File("/home/pjl/samples/Sassy9.wav"));
			
			}
			
			/**
			 * Tell the voice whether to play or not (if the sequencer is running)
			 */
			public void setRunning(final boolean running)
			{
				// This has to be in a voiceinterrupt, so that it's changed according to the current latency
				voiceServer.interruptTransmitter(this, new VoiceInterrupt() {

					@Override
					public void doInterrupt() {
						AudioTestVoice.this.running = running;
						
					}});
				
			}
			
			@Override
			public void fillBufferSynchronized(int startBufferPos, int endBufferPos, float[] buffer) {
				if(!running)
					return;
				
				// Correct byte buffer size
				if(byteBuffer==null || byteBuffer.length!=buffer.length*2)
					byteBuffer = new byte[buffer.length*2];
				
				// Check for glitch
				if(getMissedFrames()!=0)
				{
					// Glitch correction goes here
					try
					{
						// Just reopen the stream (probably not a good solution), and skip to the correct framepos
						reopenStream();
						ais.skip(getFramePos()*4);
					} catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				
				
				try {
					// Read from the stream
					ais.read(byteBuffer,startBufferPos*2,(endBufferPos-startBufferPos)*2);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// Decode byte data and insert into voiceserver buffer
				for(int n=startBufferPos;n<endBufferPos;n++)
					buffer[n] += ((short)((0xff & byteBuffer[(n*2)+0]) + ((0xff & byteBuffer[(n*2)+1]) * 256)) / 32768f);
			}
		};

		final AudioTestVoice voice = new AudioTestVoice(voiceServer, 0);
		
		// Now add to the voiceServer
		voiceServer.addTransmitter(voice);

		
		// Create the project container
		final ProjectContainer proj = new ProjectContainer();
        
		
		// Sequencer synchronization goes here
		proj.getSequencer().addSongPositionListener(new SongPositionListener() {

			public void notifyTickPosition(long tick) {
				voice.setRunning(proj.getSequencer().isRunning());
				voice.setFramePos( (proj.getSequencer().getMicrosecondPosition()  * voiceServer.getSampleRate()) / 1000000);
			}

			public boolean requiresNotificationOnEachTick() {
				return false;
			}});
		
        // Show the project frame
        new ProjectFrame(proj);       
	}
}
