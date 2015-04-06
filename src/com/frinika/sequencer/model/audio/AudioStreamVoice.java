/*
 * Created on Jul 19, 2006
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

package com.frinika.sequencer.model.audio;

import com.frinika.audio.io.AudioReader;
import com.frinika.audio.toot.SynchronizedAudioProcess;
import java.io.IOException;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.server.AudioServer;


import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.SongPositionListener;

public class AudioStreamVoice extends SynchronizedAudioProcess {

	AudioReader ais;

	boolean running = false;

	byte[] byteBuffer = null;

	int nChannel;

	long clipStartPositionInMillis;

	long clipStartPositionInFrames;

	long framePos = -1; // current frame in file

	private float sampleRate;

	/**
	 * Construct a DAAudioStreamVoice. This is an extension of the
	 * SynchronizedVoice which uses a sequencer as synchronization source.
	 * 
	 * The extended SynchronizedVoice class requires that we initially provide
	 * an offset for where in the clip to start (initialFramePos parameter), but
	 * the synchronization will then correct the position as the clip is
	 * playing. Thus we'll use the same formula as the synchronization to
	 * calculate the initialFramePos.
	 * 
	 * @param voiceServer -
	 *            The voice server we're playing in
	 * @param sequencer -
	 *            the sequencer that we are playing in
	 * @param ais -
	 *            The audio clip input stream
	 * @param clipStartTimePosition -
	 *            The start time in microseconds relative to Start time relative
	 *            to sequencer zero time
	 * @param modulator
	 *            Evelope for the audio (can be null)
	 * @throws Exception
	 */
	public AudioStreamVoice(final AudioServer audioServer,
			final FrinikaSequencer sequencer, final AudioReader ais,
			final long clipStartTimePosition1)
			throws Exception {
		super(audioServer, 0); 
		//getFramePos(sequencer, audioServer,clipStartTimePosition1));
		
//		try{
//		throw new Exception(" FIXME ME" );
//		}catch(Exception  e){
//			e.printStackTrace();
//		}
		
		this.sampleRate=audioServer.getSampleRate();
	
		this.ais = ais;
		setRealStartTime(clipStartTimePosition1);
		nChannel = ais.getFormat().getChannels();

		sequencer.addSongPositionListener(new SongPositionListener() {
			public void notifyTickPosition(long tick) {
				setRunning(sequencer.isRunning());
				setFramePos(getFramePos(sequencer, audioServer,
						clipStartPositionInMillis));
			}

			public boolean requiresNotificationOnEachTick() {
				return false;
			}
		});
	}

	long milliToFrame(double t) {
		return (long) ((t *  sampleRate) / 1000000);
	}
	
	private static long getFramePos(FrinikaSequencer sequencer,
			AudioServer audioServer, long clipStartTimePosition) {
		return (long) (((sequencer.getMicrosecondPosition() - clipStartTimePosition) * audioServer
				.getSampleRate()) / 1000000);
	}

	/**
	 * Tell the voice whether to play or not (if the sequencer is running)
	 */
	public void setRunning(final boolean running) {
		AudioStreamVoice.this.running = running;
	}

	@Override
	public void processAudioSynchronized(AudioBuffer buffer) {

		if (!running)
			return;

		boolean realTime = buffer.isRealTime();
		// Correct byte buffer size
		if (byteBuffer == null
				|| byteBuffer.length != buffer.getSampleCount() * 2 * nChannel)
			byteBuffer = new byte[buffer.getSampleCount() * 2 * nChannel];

		long seekPos = getFramePos();

		if (seekPos != framePos) {
			// S ystem.out.println(" Reposition file pointer ");
			try {
				ais.seekFrame(seekPos, realTime);
				framePos = seekPos;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		ais.processAudio(buffer);
		
		if (ais.getChannels() == 1) {
			buffer.copyChannel(0, 1);
		}
		
		framePos += buffer.getSampleCount();
				
	}

	public void setRealStartTime(long realStartTime) {
		clipStartPositionInMillis = realStartTime;
		clipStartPositionInFrames = (long) ((clipStartPositionInMillis * sampleRate) / 1000000);
	}

	public void close() {
	
	}

	public void open() {
	
	}

}
