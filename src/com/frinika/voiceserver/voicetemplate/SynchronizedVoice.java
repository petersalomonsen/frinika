package com.frinika.voiceserver.voicetemplate;

import com.frinika.voiceserver.Voice;
import com.frinika.voiceserver.VoiceInterrupt;
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
import com.frinika.voiceserver.VoiceServer;

/**
 * A voice that can be synchronized from an outside timing source. Useful for e.g. audio playback to be synchronized
 * with a sequencer.
 * 
 * @author Peter Johan Salomonsen
 */
public abstract class SynchronizedVoice extends Voice {
	
	long framePos = 0;
	
	/**
	 * In case of a glitch - how many frames did we miss?
	 */
	int missedFrames = 0;
	
	/**
	 * Number of milliseconds of glitch before synchronization if enforced
	 * Windows recommendation: 50 ms, Linux 10-20 ms
	 * TODO: Make this configurable from audio devices GUI
	 */
	int missedFramesToleranceMillis = 50; // Default 50 msecs
	
	protected VoiceServer voiceServer;

	/**
	 * Construct a new Synchronized Voice. Note that in order for the initialFramePos to be valid, this voice
	 * should be added to the voiceServer not too long after it's constructed
	 * 
	 * Since glitches may "come and go" due to the missing accuracy in Java timing functions, synchronization is not
	 * applied until a tolerance threshold is exceeded.
	 * 
	 * @param voiceServer
	 * @param initialFramePos - the initial position of playback start - relative to audio clip start
	 */
	public SynchronizedVoice(VoiceServer voiceServer, long initialFramePos)
	{
		this.voiceServer = voiceServer;
		this.framePos = initialFramePos;
	}

	/**
	 * Number of milliseconds of glitch before synchronization if enforced
	 */
	public int getMissedFramesToleranceMillis() {
		return missedFramesToleranceMillis;
	}

	/**
	 * Set number of milliseconds of glitch before synchronization if enforced
	 * @param missedFramesToleranceMillis
	 */
	public void setMissedFramesToleranceMillis(int missedFramesToleranceMillis) {
		this.missedFramesToleranceMillis = missedFramesToleranceMillis;
	}

	/**
	 * The external timing source should regularly notify with the current frame position here
	 * @param framePos
	 */
	public void setFramePos(final long framePos)
	{
		voiceServer.interruptTransmitter(this, new VoiceInterrupt() {

			@Override
			public void doInterrupt() {
				// For a samplerate of 44100 the glitch must be at least 45 frames for a 1 msec glitch
				int glitchMS = ((int)(framePos - SynchronizedVoice.this.framePos) * 1000) / voiceServer.getSampleRate();
			
				// Only enfore synchronization if the glitch is beyond the tolerance
				if(Math.abs(glitchMS)>missedFramesToleranceMillis)
				{
					missedFrames = (int) (framePos - SynchronizedVoice.this.framePos);
					SynchronizedVoice.this.framePos = framePos;
				}
			}});
	}
	
	/**
	 * Call this method from fillBufferSynchronized to get the framePos according to the external timing source
	 * @return
	 */
	protected final long getFramePos()
	{
		return framePos; 
	}
	
	/**
	 * Call this method from fillBufferSynchronized to get the number of missing frames (glitch) after an external sync notification
	 * NOTE: Your tolerance on missed frames should not be too low - since timing functions like System.currentTimeMillis might slide
	 * up to 50 ms on some systems. Your number of missed frames tolerance should be thereafter before correcting your 
	 * framepos.
	 * 
	 * @return
	 */
	protected final int getMissedFrames()
	{
		return missedFrames; 
	}
	
	@Override
	public final void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
		fillBufferSynchronized(startBufferPos,endBufferPos,buffer);
		framePos += (endBufferPos - startBufferPos) / 2; // Update framePos according to number of requested samples to fillBuffer
		missedFrames = 0; // Reset missedframes until next notification
	}
	 
	public abstract void fillBufferSynchronized(int startBufferPos, int endBufferPos, float[] buffer);
}
