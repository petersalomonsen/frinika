/*
 * Created on Jan 16, 2007
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

package com.frinika.sequencer.model.audio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFormat;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

/*
 * 
 * Random access wrapper for an audio wav file. seek(0) is the sequence start
 * 
 * Not for real time use.
 * 
 * Provides two interfaces DoubleDataSource and AudioProcess.
 * 
 * Probably not a good idea to use both at the same time.
 * 
 */
/**
 * @deprecated   TODO use AudioReader 
 */
public class AudioClipReader implements DoubleDataSource, AudioProcess {

	// long startFrame;

	DAudioReader reader;

	byte byteBuffer[];

	float bPtr[][] = new float[2][];

	private int nch;

	private long startFrame;

	/**
	 * 
	 * @param clipFile
	 *            file with audio
	 * @param startFrame
	 *            position in frames relative to start of sequence.
	 * @throws IOException
	 */
	public AudioClipReader(File clipFile, long startFrame) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(clipFile, "r");
		this.reader = new DAudioReader(raf);
		// this.startFrame = startFrames;
		reader.seekFrame(0);
		this.nch = reader.getChannels();
		System.out.println(" Channels = " + nch);
		this.startFrame = startFrame;
	}

	/**
	 * read from file into a double array.
	 * 
	 * @param buffer
	 *            double buffer
	 * @param offSet
	 *            start writing here
	 * @param nFrame
	 *            number of frames to read.
	 */
	public void readNextDouble(double buffer[], int offSet, int nFrame) {
//		nch = this.reader.getChannels();
		int nByte = nFrame * nch * 2; // assmume 2 byte
		if (this.byteBuffer == null || this.byteBuffer.length != nByte) {
			this.byteBuffer = new byte[nByte];
		}

		try {			
			this.reader.read(this.byteBuffer, 0, nByte);
			// Decode byte data and insert into voiceserver buffer
			if (nch == 2) {
				for (int n = 0; n < 2 * nFrame; n++) {
					buffer[offSet + n] = ((short) ((0xff & this.byteBuffer[(n * 2) + 0]) + ((0xff & this.byteBuffer[(n * 2) + 1]) * 256)) / 32768f);
				}
			} else {
				for (int n = 0; n < nFrame; n++) {
					double val = ((short) ((0xff & this.byteBuffer[2 * n]) + ((0xff & this.byteBuffer[2 * n + 1]) * 256)) / 32768f);
					buffer[offSet + n] = val;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * read from file into a double array.
	 * 
	 * @param buffer
	 *            double buffer
	 * @param offSet
	 *            start writing here
	 * @param nFrame
	 *            number of frames to read.
	 */
	public int processAudio(AudioBuffer buffer) {
	//	nch = this.reader.getChannels();
		int nFrame = buffer.getSampleCount();
		int nByte = nFrame * nch * 2; // assmume 2 byte

		if (this.byteBuffer == null || this.byteBuffer.length != nByte) {
			this.byteBuffer = new byte[nByte];
		}

		for (int i = 0; i < buffer.getChannelCount(); i++) {
			bPtr[i] = buffer.getChannel(i);
		}
		try {
			this.reader.read(this.byteBuffer, 0, nByte);
			for (int ch = 0; ch < nch; ch++) {
				for (int n = 0; n < nFrame; n++) {
					int ptr = (n * nch + ch) * 2;

					bPtr[ch][n] = ((short) ((0xff & this.byteBuffer[ptr + 0]) + ((0xff & this.byteBuffer[ptr + 1]) * 256)) / 32768f);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return AUDIO_OK;
	}

	
	/**
	 * 
	 * @param pos position relative to start of clip
	 */

	public void seekFrameInClip(long pos) {
		try {
			reader.seekFrame(pos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Seek frame absolute frame postion pos-startFrame
	 */
	public void seekFrame(long pos) {
		try {
			reader.seekFrame(pos - startFrame);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * true if ptr is after the last data in the file.
	 */
	public boolean endOfFile() {
		return reader.eof();
	}

	public int getChannels() {
		return nch;
	}

	public AudioFormat getFormat() {
		return reader.getFormat();

	}

	public void open() {
		// TODO Auto-generated method stub

	}

	public void close() {
		// TODO Auto-generated method stub

	}
	
	public long getCurrentFrame() {
		return reader.getCurrentFrame();// (fPtrBytes-audioDataStartBytePtr)/nChannels/2;		
	}

	public long getLengthInFrames() {
		// TODO Auto-generated method stub
		return reader.getLengthInFrames();
	}
}
