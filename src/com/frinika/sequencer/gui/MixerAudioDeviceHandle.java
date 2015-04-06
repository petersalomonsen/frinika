/*
 * Created on Jun 13, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.frinika.sequencer.gui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import uk.org.toot.audio.server.AudioServer;

public class MixerAudioDeviceHandle implements AudioDeviceHandle {


	private Mixer mixer;

	private AudioFormat af;

	private DataLine.Info info;

	private TargetDataLine line;

	private AudioServer server;

	private byte inBuffer[];
	
	public MixerAudioDeviceHandle(Mixer mixer, AudioFormat af,
			DataLine.Info info, AudioServer server) {
		this.mixer = mixer;
		this.af = af;
		this.info = info;
		this.line = null;
		this.server = server;
	
	}

	public String toString() {
		if (af.getChannels() == 1)
			return mixer.getMixerInfo().getName() + " (MONO)"; // +
																// af.toString();
		else if (af.getChannels() == 2)
			return mixer.getMixerInfo().getName() + " (STEREO)"; // +
																	// af.toString();
		else
			return mixer.getMixerInfo().getName() + "channels="
					+ af.getChannels(); // + af.toString();
	}

	/**
	 *@deprecated To be rpelaced with COnnections
	 */
	public TargetDataLine getLine() { // DataLine.Info infoIn) {
		try {
			System.out.println(this + " **** " + af);
			DataLine.Info infoIn = new DataLine.Info(TargetDataLine.class, af);

			// lineIn =
			// (TargetDataLine)AudioSystem.getMixer(mixers.get(0)).getLine(infoIn);

			line = (TargetDataLine) mixer.getLine(infoIn);
		

		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	public void fillBuffers(int nFrame) {

		
		// System.out.println(" Dev fill buffer ");
		try {


			// 2 channels out but 2 byte per in sample
			int nByte = af.getChannels()*nFrame*2;

			if (inBuffer == null || inBuffer.length != nByte ) inBuffer=new byte[nByte];
			
			// TODO: Why would nByte be 0 anyway? This is a quick fix
			if (line.available() >= nByte && nByte > 0) {
			
				int nread;
				int cnt = 0;

				/**
				 * If we have glitches in the output, this means that there will
				 * be more data in the input buffer than we are able to handle
				 * in this fillBuffer session. Unless we compensate here by
				 * skipping data from the input, the software monitoring would
				 * have introduced an extra delay. However we don't want to skip
				 * this data in the recording, so what we do is to put all this
				 * samples in the recordbuffer, but skip to send them to the
				 * preoscillator sample buffer.
				 */
				do {
					nread = line.read(inBuffer, 0, nByte);
					if (nread == 0)
						System.out.println("active :" + line.isActive()
								+ " available:" + line.available()
								+ " nByte: " + nByte + " inBuffersize: "
								+ inBuffer.length);
			
					
					//inputFramesReadCount += nByte / 2 / af.getChannels();

					cnt++;

					for (int n = 0; n < nByte / 2; n++) {
						short sample = (short) ((0xff & inBuffer[2 * n + 1]) + ((0xff & inBuffer[2 * n]) * 256));
						float val = sample / 32768f;
					}
	
				} while (line.available() > 2 * nByte);

				if (cnt != 1)
					System.out.println(" COUNT WAS " + cnt);
				
			} else {
				System.err.println(String.format(" GLITCH avail=%d actual=%d ",line.available(),nByte ));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @deprecated   TO be replaced by Connections
	 */
	public TargetDataLine getOpenLine() {
		if (isOpen())
			return line;
		// TODO Auto-generated method stub
		try {
			if (line == null)
				line = (TargetDataLine) mixer.getLine(info);
			line.open(af);
			return line;

		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

//	public void start() {
//		line.flush();
//		line.start();
//		
//	}
	
	public int getChannels() {
		return af.getChannels();
	}

	public AudioFormat getFormat() {
		return af;
	}

	public boolean isOpen() {
		if (line == null)
			return false;
		return line.isOpen();
	}

	/**
	 * open and start the line.
	 */
	public void open() {

		System.out.println(" Opening MixrerAudioDevice line");
		if (isOpen())
			return;
		
		try {
			if (line == null)
				line = (TargetDataLine) mixer.getLine(info);

			
			line.open(af);
			System.out.println("  . . ..  Open");

			line.start();
			System.out.println("  . . ..  Start  " + isOpen());

		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void close() {
		if (!isOpen())
			return;
		line.close();
	}

	public byte[] getBuffer() {
		// TODO Auto-generated method stub
		return inBuffer;
	}
	
}
