package com.frinika.codeexamples;

import javax.sound.sampled.TargetDataLine;

import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.AudioInput;
import com.frinika.voiceserver.Voice;
import com.frinika.voiceserver.VoiceServer;
/*
* Created on Oct 23, 2006
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
import com.frinika.global.FrinikaConfig;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFrame;

/**
 * Simple program that will just capture your audio input and monitor it to the output
 * @author Peter Salomonsen
 *
 */
public class AudioMonitor {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		VoiceServer voiceServer = new AudioContext().getVoiceServer();
		
                voiceServer.configureAudioOutput(new JFrame());
		/**
		 * JACK doesn't require any input line - provide your own line if not using jack
		 */
		final AudioInput input = new AudioInput(AudioSystem.getTargetDataLine(new AudioFormat((float) FrinikaConfig.sampleRate,16,2,true,true)),FrinikaConfig.sampleRate);
		
		input.start();
		input.getLine().start();
		
		voiceServer.addTransmitter(new Voice() {
			byte[] inBuffer = null;
			@Override
			public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
				if(inBuffer == null || inBuffer.length!=buffer.length*2)
					inBuffer = new byte[buffer.length * 2];
				
				int numOfBytes = (endBufferPos-startBufferPos)*2;
				
				input.getLine().read(inBuffer,0,numOfBytes);
				
				TargetDataLine line = input.getLine();
				
				int n=0;
				for(int i=startBufferPos;i<endBufferPos;i++)
				{
					short sample = (short)((0xff & inBuffer[n+1]) + ((0xff & inBuffer[n+0]) * 256));
					buffer[i] = sample / 32768f;
					n+=2;
				}
				
			}
			
		});
		// otherwise we terminate
		Thread.sleep(100000);
	}

}
