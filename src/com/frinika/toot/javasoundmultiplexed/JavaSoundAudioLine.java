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
import javax.sound.sampled.Mixer;

import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.server.AudioLine;

abstract class JavaSoundAudioLine implements AudioLine {

	protected AudioFormat format;

	protected Mixer.Info mixerInfo;

	protected String label;

	protected int latencyFrames = -1;

	protected ChannelFormat channelFormat;

	public JavaSoundAudioLine(AudioFormat format, Mixer.Info info, String label) {
		this.format = format;
		mixerInfo = info;
		this.label = label;
		switch (format.getChannels()) {
		case 1:
			channelFormat = ChannelFormat.MONO;
		case 2:
			channelFormat = ChannelFormat.STEREO;
		}
	}

	public String getName() {
		return label;
	}

	public ChannelFormat getChannelFormat() {
		return channelFormat;
	}

	public int getLatencyFrames() {
		return latencyFrames;
	}

	public abstract void start() throws Exception;

	public abstract void stop() throws Exception;

	public abstract boolean isActive();
}
