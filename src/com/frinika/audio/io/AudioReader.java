/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
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
/*
 * Created on Jul 18, 2006
 *
 *
 * Based on code from
 * http://www.jensign.com/JavaScience/www/mmedia/riffread/riffread.java
 * by M. Gallant   2/14/97
 *
 *Mods by pjl
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

package com.frinika.audio.io;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

/*
 * Wraps up a wav file and allows access using a RandomAccessFileIF. Provides a
 * view of the file that allows reads before the start and after the end of the
 * file you'll just get zeros returned.
 * 
 * Implements reading of 16 signed 1 or 2 channel littleendian wav files.
 * 
 * Job of initializing the file is done by AudioWavReader super class
 */
public class AudioReader extends AudioWavReader implements BlockableAudioProcess,AudioProcess,LimitedAudioReader {

	RandomAccessFileIF bfis;

	protected long startByte;

	protected long endByte;

    long startFrame;
    
	byte byteBuff[];

	// Used to point to current read position
	// bytes from the start of audio data in the file
	// NOT THE START OF THE FILE (see audioDataStartBytePtr)
	protected long fPtrBytes;

	double sampleRate;

	private boolean closed;
	

	public AudioReader(RandomAccessFileIF fisIF,float Fs) throws IOException {
		super(fisIF.getRandomAccessFile());

		sampleRate = format.getSampleRate();
        if (sampleRate != Fs) {
            try {
                throw new Exception(" audio file is not correct sample rate." +sampleRate+" should be" + Fs);
            } catch (Exception ex) {
                Logger.getLogger(AudioReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
		startByte = 0;
        startFrame =0;
		endByte = audioDataByteLength;
		
		closed = endByte!=0;
		bfis = fisIF;
		fisIF.seek(audioDataStartBytePtr, false); // non real time seek fills
		// the buffers
	}

	public final long milliToByte(double milli) {
		return nChannels * 2 * (long) (milli * sampleRate / 1000000.0);
	}

	public void seekTimeInMicros(double micros, boolean realTime)
			throws IOException {
		long framePos = (long) (micros * sampleRate / 1000000.0);
		seekFrame(framePos, realTime);
	}

	/**
	 * 
	 * @param framePos
	 *            frame postition reltive to start of audio. e.g. zero is start
	 *            of audio.
	 * 
	 * @throws IOException
	 */
	public void seekFrame(long framePos, boolean realTime) throws IOException {

		// pointer into audio data section
		fPtrBytes = framePos * 2 * nChannels;

		if (fPtrBytes >= startByte) {
			// Offset the seek into audio section
			if (fPtrBytes < audioDataByteLength && fPtrBytes < endByte)
				bfis.seek(fPtrBytes + audioDataStartBytePtr, realTime);
		} else {

			// Seek to the start of audio section
			bfis.seek(audioDataStartBytePtr + startByte, realTime);
		}

	}

	public boolean eof() {
		try {
			return fPtrBytes - audioDataStartBytePtr >= bfis.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	public void setBoundsInMicros(double start,double end) {
		
		assert(start<=end);
		startByte = Math.max(0, milliToByte(start));
        startFrame=startByte/2/nChannels;
		endByte = Math.min(audioDataByteLength,milliToByte(end));
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void open() {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * 
	 * this version will block if the file is being written to and there is not enough
	 * data to fill the buffer
	 * 
	 * @param buffer
	 * @return
	 * @throws IOException 
	 */
	public void processAudioBlock(AudioBuffer buffer) throws Exception {
		
		if (closed) {
			processAudio(buffer);
			return;
		}
		
		int n=buffer.getSampleCount();
		
		// wait for data of file closed
		while(n+fPtrBytes - audioDataStartBytePtr >= bfis.length()) {
			if (getLengthInFrames() > 0 ) {
				closed=true;
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		int nBytes = nChannels * 2 * buffer.getSampleCount();
		
		boolean realTime = buffer.isRealTime();
	
		if (byteBuff == null || byteBuff.length != nBytes) {
			byteBuff = new byte[nBytes];
		}
		
		int nread = bfis.read(byteBuff, 0, nBytes, false);
		
		fill(buffer, 0, n);
		
		
	//	return AUDIO_OK; 
	}
	/**
	 * 
	 * 
	 * Read from file into byte buffer and advance the fPtrBytes pointer it is
	 * OK to read before/after start/end of the file you'll just get zeros.
	 * fPtrBytes is advanced by appropriate byte count.
	 * 
	 * @param byteBuffer
	 *            buffer to fill
	 * @param offSet
	 *            offset into byteBuffer
	 * @param n
	 *            number of bytes to be read
	 * @throws IOException
	 */
	public int processAudio(AudioBuffer buffer) {
	
		int nBytes = nChannels * 2 * buffer.getSampleCount();
	
		boolean realTime = buffer.isRealTime();
	
		if (byteBuff == null || byteBuff.length != nBytes) {
			byteBuff = new byte[nBytes];
		}
	
		// valid limits for the chunk to be read.
		int startChunk = 0; // first valid byte of data
		int endChunk = nBytes; // last byte + 1
	
		long minEndByte = Math.min(endByte, audioDataByteLength);
	
		try {
			if (fPtrBytes < startByte) {
	
				int nRead = (int) (nBytes + fPtrBytes - startByte); // bytes of
																	// this read
				// after
				// start of audioData
	
				if (nRead > 0) {
					startChunk = nBytes - nRead; // read into the last
													// portion of
					bfis.read(byteBuff, startChunk, nRead, realTime);
				} else {
					fPtrBytes += nBytes;
					return AUDIO_OK;
				}
	
			} else if (fPtrBytes <= minEndByte) {
	
				// Zeros after the end of the data
				int nExtra = (int) (fPtrBytes + nBytes - minEndByte);
	
				if (nExtra > 0) {
	
					// Some bytes after end of the audio section ?
	
					endChunk = nBytes - nExtra;
					bfis.read(byteBuff, 0, endChunk, realTime);
				} else {
	
					// all data in audio section
					int nread = bfis.read(byteBuff, 0, nBytes, realTime);
					if (nread != nBytes)
						try {
							throw new Exception(" Ooops only read " + nread
									+ " out of " + nBytes);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			} else {
				fPtrBytes += nBytes;
				return AUDIO_OK;
			}

			processAudioImp(buffer,startChunk,endChunk);
		
	
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		fPtrBytes += nBytes;
	
		return AUDIO_OK;
	
	}

	

	protected void processAudioImp(AudioBuffer buffer, int startChunk,
			int endChunk) {
			fill(buffer, startChunk, endChunk);
	}
	
	
	/**
	 * 
	 * 
	 * @param buffer
	 * @param startChunk
	 * @param endChunk
	 * @param gain1
	 * @param gain2
	 */
	
	
	protected void fillLinearInterpolate(AudioBuffer buffer, int startChunk,
			int endChunk, double gain1, double gain2) {

		double dG = (gain2 - gain1) / (endChunk - startChunk) / nChannels / 2.0;
		if (nChannels == 2) {
			float[] left = buffer.getChannel(0);

			float[] right = buffer.getChannel(1);
			for (int n = startChunk / 2; n < endChunk / 2; n++) {
				float sample = ((short) ((0xff & byteBuff[(n * 2) + 0]) + ((0xff & byteBuff[(n * 2) + 1]) * 256)) / 32768f);
				sample *= gain1;
				if (n % 2 == 0)
					left[n / 2] += sample;
				else
					right[n / 2] += sample;
				gain1 += dG;
			}
		} else {
			float[] left = buffer.getChannel(0);

			for (int n = startChunk; n < endChunk; n += 2) {
				float val = ((short) ((0xff & byteBuff[n]) + ((0xff & byteBuff[n + 1]) * 256)) / 32768f);
				left[n / 2] += val * gain1;
				gain1 += dG;
			}
		}
	}

	protected void fillConstantGain(AudioBuffer buffer, int startChunk,
			int endChunk, double gain) {
		if (nChannels == 2) {
			float[] left = buffer.getChannel(0);

			float[] right = buffer.getChannel(1);
			for (int n = startChunk / 2; n < endChunk / 2; n++) {
				float sample = ((short) ((0xff & byteBuff[(n * 2) + 0]) + ((0xff & byteBuff[(n * 2) + 1]) * 256)) / 32768f);
				sample *= gain;
				if (n % 2 == 0)
					left[n / 2] += sample;
				else
					right[n / 2] += sample;
			}
		} else {
			float[] left = buffer.getChannel(0);

			for (int n = startChunk; n < endChunk; n += 2) {
				float val = ((short) ((0xff & byteBuff[n]) + ((0xff & byteBuff[n + 1]) * 256)) / 32768f);
				left[n / 2] += val * gain;
			}
		}
	}

	protected void fill(AudioBuffer buffer, int startChunk,int endChunk) {
		if (nChannels == 2) {
			float[] left = buffer.getChannel(0);

			float[] right = buffer.getChannel(1);
			for (int n = startChunk / 2; n < endChunk / 2; n++) {
				float sample = ((short) ((0xff & byteBuff[(n * 2) + 0]) + ((0xff & byteBuff[(n * 2) + 1]) * 256)) / 32768f);
				if (n % 2 == 0)
					left[n / 2] += sample;
				else
					right[n / 2] += sample;
			}
		} else {
			float[] left = buffer.getChannel(0);

			for (int n = startChunk; n < endChunk; n += 2) {
				float val = ((short) ((0xff & byteBuff[n]) + ((0xff & byteBuff[n + 1]) * 256)) / 32768f);
				left[n / 2] += val ;
			}
		}
	}

    public int getEnvelopedLengthInFrames() {
        return (int) ((endByte - startByte) / nChannels / 2);
    }

   

    public void seekEnvelopeStart(boolean b) throws IOException {
         bfis.seek(audioDataStartBytePtr + startByte, b);
            	// pointer into audio data section
	     fPtrBytes=	startByte;
    }

    public void seekFrameInEnvelope(long framePtr, boolean b) throws IOException {
        seekFrame(startFrame+framePtr,b);
    }

    public double getSampleRate() {
        return sampleRate;
    }

	
}
