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
import javax.sound.sampled.DataLine;
//import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
//import javax.sound.sampled.TargetDataLine;

class JavaSoundDevice {

	protected byte byteBuffer[];
	
	protected Mixer mixer;

	protected AudioFormat af;

	protected DataLine.Info info;

	protected DataLine line;

	protected long bufferSizeInFrames;
	int bytesPerFrame;
	
	public JavaSoundDevice(Mixer mixer, AudioFormat af, DataLine.Info info,int bufferSizeInFrames) {
		this.mixer = mixer;
		this.af = af;
		this.info = info;
		this.line = null;
		bytesPerFrame = 2*af.getChannels();
		byteBuffer=new byte[bufferSizeInFrames*bytesPerFrame];
	}

	public String toString() {
		if (af.getChannels() == 1)
			return mixer.getMixerInfo().getName() + " (MONO)"; // +
		// af.toString();
		else if (af.getChannels() == 2)
			return mixer.getMixerInfo().getName() + " (STEREO)"; // +
		// af.toString();
		else
			return mixer.getMixerInfo().getName() + "(" + af.getChannels()
					+ ")"; // + af.toString();
	}

	
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

	
	public String getName() {
		return toString(); // mixer.getMixerInfo().getName()+ af;
	}

	public boolean isActive() {
		if (line == null)
			return false;
		return line.isActive();
	}
	
	public byte[] getBuffer() {
		return byteBuffer;
	}

}
