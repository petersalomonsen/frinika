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

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.server.AudioLine;

class StereoOutConnection implements AudioLine {

	private JavaSoundOutDevice dev;

	private int chan[];

	ChannelFormat chanFormat;

	boolean isBigEndian;
	/*
	 *  
	 */
	public StereoOutConnection(JavaSoundOutDevice dev, int chan[]) {
		this.dev = dev;
		this.chan = new int[chan.length];
		for (int i = 0; i < chan.length; i++)
			this.chan[i] = chan[i];

		isBigEndian=dev.getFormat().isBigEndian();
	}

//	public void open() {
//		System.out.println(" JavaSoundConnection OPEN" + dev);
//		dev.open();
//	}

	public int processAudio(AudioBuffer buffer) {

		byte bytes[] = dev.getBuffer();

		assert(bytes != null);
		

		// System.out.println(" proc buff");

		int n = buffer.getSampleCount();
		int nchan = dev.getChannels();

		for (int chPtr = 0; chPtr < chan.length; chPtr++) {
			int ch = chan[chPtr];

			float out[] = buffer.getChannel(chPtr);

			if (isBigEndian) {
				for (int i = 0; i < n; i++) {
					int ib = i * 2 * nchan + ch * 2;

					short sample = (short) (out[i] * 32768f);

					bytes[ib + 1] = (byte) (0xff & sample);
					bytes[ib] = (byte) (0xff & (sample >> 8));

				}
			} else {
				for (int i = 0; i < n; i++) {
					int ib = i * 2 * nchan + ch * 2;
					short sample = (short) (out[i] * 32768f);
					bytes[ib + 1] = (byte) (0xff & sample>>8);
					bytes[ib] = (byte) (0xff & (sample ));

				}
				
				
			}
		}
		return AUDIO_OK;

	}

	public void close() {
		// TODO Auto-generated method stub

	}

	// JavaSoundDevice getDeviceHandle() {
	// return dev;
	// }

	// public String toString() {
	// return dev.toString() + ":" + String.valueOf(chan);
	// }

	public float getLatencyMilliseconds() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getName() {
		return "out " + (chan[0] + 1) + "/" + (chan[1] + 1);
	}

	public int getLatencyFrames() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ChannelFormat getChannelFormat() {
		// TODO Auto-generated method stub
		return ChannelFormat.MONO;
	}

	public void open() {
		// TODO Auto-generated method stub
		
	}

}
