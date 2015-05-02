/*
 * Created on Apr 10, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

package com.frinika.toot.javasoundmultiplexed;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.DataLine.Info;

class JavaSoundInDevice extends JavaSoundDevice {
	private int latencyFrames;
	private boolean doFlush = false;
	private long framesRead = 0;
	
	public JavaSoundInDevice(Mixer mixer, AudioFormat af, Info info,int bufferSizeInFrames) {
		super(mixer, af, info,bufferSizeInFrames);
		framesRead = 0;
		doFlush = true;
	}

	public void fillBuffer() {

		
		// ------------------------------------------------
		if (line.available() < byteBuffer.length) {
			// buffer.makeSilence();
			System.out.println('_');

		} else if (doFlush) {
			doFlush = false;
			line.flush();

			// buffer.makeSilence();
			System.out.println(getName() + " flushed");
		} else {
			latencyFrames = (int) (line.getLongFramePosition() - framesRead);
			try {


				int nread = ((TargetDataLine)line).read(byteBuffer, 0, byteBuffer.length);

				framesRead += nread/2/af.getChannels(); //  bufferSizeInFrames;

				if (nread == 0)
					System.out.println("active :" + line.isActive()
							+ " available:" + line.available() + " nByte: "
							 + byteBuffer.length);

				// inputFramesReadCount += nByte / 2 / af.getChannels();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	
	public void flush() {
		doFlush = true;
	}

	public void start() throws Exception {
		framesRead = 0;
		open();
	}


	public void stop() throws Exception {
		close();
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

			((TargetDataLine)line).open(af);
			System.out.println("  . . ..  Open");
			line.flush();
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

	
	public boolean isActive() {
		if (line == null)
			return false;
		return line.isActive();
	}
	

    public int getLatencyFrames() {
        return latencyFrames;
    }
}
