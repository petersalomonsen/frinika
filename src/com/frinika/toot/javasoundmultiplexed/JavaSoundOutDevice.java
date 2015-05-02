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

import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine.Info;

import uk.org.toot.audio.server.AudioSyncLine;

class JavaSoundOutDevice extends JavaSoundDevice implements AudioSyncLine {

	
	protected long framesWritten = 0;

	boolean hasStarted = false;

	private int latencyFrames;

	public JavaSoundOutDevice(Mixer mixer, AudioFormat af, Info info,int bufferSizeInFrames) {
		super(mixer, af, info, bufferSizeInFrames);
		
		
	}

//	public String getName() {
//		// TODO Auto-generated method stub
//		return mixer.toString();
//	}

	int cnt=0;
	
	public void writeBuffer() {

		// int nbytes = buffer.convertToByteArray(sharedByteBuffer, 0, format);
		if (line.available() > byteBuffer.length) {
			int nnn=((SourceDataLine) line).write(byteBuffer, 0, byteBuffer.length);
			framesWritten += nnn/bytesPerFrame; // bufferSizeInFrames;
		} else {
			if (cnt++ %50 == 0) System.out.println(" XXXRUUNNN ");
		}
		/*
		 * long endNanos = System.nanoTime(); long elapsedMillis = (endNanos -
		 * beginNanos) / 1000000; if ( elapsedMillis > 10 ) {
		 * System.out.println(label+" took "+elapsedMillis+"ms"); }
		 */
		latencyFrames = (int) (framesWritten - line.getLongFramePosition());
	}

	public void start() throws Exception {
		framesWritten = 0;
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
				line = (SourceDataLine) mixer.getLine(info);

			((SourceDataLine)line).open(af);
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

	
	public int getLatencyFrames() {
		return latencyFrames;
	}

	
	/* clear buffer */
	
	public void zeroBuffer() {
		Arrays.fill(byteBuffer,(byte)0);	
	}

}
