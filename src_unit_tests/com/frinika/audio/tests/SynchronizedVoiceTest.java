package com.frinika.audio.tests;

import com.frinika.voiceserver.VoiceServer;
import com.frinika.voiceserver.voicetemplate.SynchronizedVoice;
import javax.swing.JFrame;

import junit.framework.TestCase;
/*
 * Created on Jul 17, 2006
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
 * This test will verify the synchronization mechanisms of a SynchronizedVoice
 * @author Peter Johan Salomonsen
 */
public class SynchronizedVoiceTest extends TestCase {
	VoiceServer voiceServer;
	
	/**
	 * Set up the test
	 */
	protected void setUp() throws Exception
	{
		this.voiceServer = new VoiceServer() {

            @Override
            public void configureAudioOutput(JFrame frame) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
	}
	
	public void testSynchronizedVoice() throws Exception
	{
		// Set up a synchronized voice
		SynchronizedVoice voice = new SynchronizedVoice(voiceServer, 0) {
			
			@Override
			public void fillBufferSynchronized(int startBufferPos, int endBufferPos, float[] buffer) {
				if(getMissedFrames()!=0)
					System.out.println("GLITCH! : FramePos "+getFramePos()+" Missed frames: "+getMissedFrames());
				else
					System.out.println("Everything OK. FramePos "+getFramePos()+" Missed frames: "+getMissedFrames());
			}
		};

		// Now add to the voiceServer
		voiceServer.addTransmitter(voice);
		
		// The start time - for framePos reference
		long startTime = System.currentTimeMillis();

		// Run the test for 20 seconds - notify every second

		for(int n=0;n<20;n++)
		{
			voice.setFramePos( ((System.currentTimeMillis() - startTime) * voiceServer.getSampleRate()) / 1000);
			Thread.sleep(1000);
		}
	}
}
