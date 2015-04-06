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

package com.frinika.sequencer.model.audio;

import java.io.IOException;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFormat;

/**
 * Wraps up a wav file and allows access using a RandomAccessFile.
 * provides a view of the file that allows reads before the start and after the end of the file
 * you'll just get zeros returned.
 * 
 * @deprecated
 */
public class DAudioReader {

	static String sp = "     "; // spacer string.

	static String indent = sp + "     ";

	private long audioDataStartBytePtr; // start of audio data in bytes

	private int audioDataByteLength; // number of bytes of audio

	private int lengthInFrames;

	private RandomAccessFile fis;

	private int bytecount = 0;

	private int riffdata = 0; // size of RIFF data chunk.

	private AudioFormat format;

	int nChannels;

	// Used to point to current read position
	// bytes from the start of audio datat in the file
	// NOT THE START OF THE FILE (see audioDataStartBytePtr)
	private long fPtrBytes;

	public DAudioReader(RandomAccessFile fis) throws IOException {
		String sfield = "";
		this.fis = fis;

		long filesize = (fis.length()); // get file

		readChunkHeader();

		while (bytecount < riffdata) { // check for chunks inside RIFF data
			// area.
			sfield = "";
			for (int i = 1; i <= 4; i++)
				sfield += (char) fis.readByte();

			int chunkSize = 0;
			for (int i = 0; i < 4; i++)
				chunkSize += fis.readUnsignedByte() * (int) Math.pow(256, i);

			if (sfield.equals("data")) {

				audioDataStartBytePtr = fis.getFilePointer();
			//	System. out.println(" Audio offset " + audioDataStartBytePtr);
				audioDataByteLength = (int) (filesize - audioDataStartBytePtr);
			}

			bytecount += (8 + chunkSize);

			if (sfield.equals("fmt ")) { // extract info from "format"

				readFormat(chunkSize);
			} else
				// if NOT the fmt chunk.
				fis.skipBytes(chunkSize);

			lengthInFrames = chunkSize / format.getFrameSize();

//			System .out.println(lengthInFrames + "  " + audioDataByteLength
//					+ " " + format.getFrameSize());
		} // end while.

		if ((8 + bytecount) != (int) filesize)
			System.out.println(sp
					+ "!!!!!!! Problem with file structure  !!!!!!!!! ");

	} // end try.


		
	private void readChunkHeader() throws IOException {
		String sfield = "";

		int chunkSize = 0;

		/* -------- Get RIFF chunk header --------- */
		for (int i = 1; i <= 4; i++)
			sfield += (char) fis.readByte();
		if (!sfield.equals("RIFF")) {
			System.out.println(" ****  Not a valid RIFF file  ****");
			return;
		}

		for (int i = 0; i < 4; i++)
			chunkSize += fis.readUnsignedByte() * (int) Math.pow(256, i);
		sfield = "";
		for (int i = 1; i <= 4; i++)
			sfield += (char) fis.readByte();
		// System. out.println(sp + " ----- form type: " + sfield + "\n");

		riffdata = chunkSize;
		/* --------------------------------------------- */

		bytecount = 4; // initialize bytecount to include RIFF form-type
		// bytes.

	}

	/*
	 * This returns the number of frames (my frame all the data for a sample)
	 */
	public int getLengthInFrames() {
		return lengthInFrames;
	}

	private void readFormat(int chunkSize) throws IOException {
		// chunk.

		if (chunkSize < 16) {
			System.out.println(" ****  Not a valid fmt chunk  ****");
			return;
		}
		int wFormatTag = fis.readUnsignedByte();
		fis.skipBytes(1);
		// if (wFormatTag == 1)
		// System.out.println(indent + "wFormatTag: MS PCM format");
		// else
		// System.out.println(indent + "wFormatTag: non-PCM format");
		nChannels = fis.readUnsignedByte();
		fis.skipBytes(1);
		// System.out.println(indent + "nChannels: " + nChannels);
		int nSamplesPerSec = 0;
		for (int i = 0; i < 4; i++)
			nSamplesPerSec += fis.readUnsignedByte() * (int) Math.pow(256, i);
		// System.out.println(indent + "nSamplesPerSec: " + nSamplesPerSec);
		int nAvgBytesPerSec = 0;
		for (int i = 0; i < 4; i++)
			nAvgBytesPerSec += fis.readUnsignedByte() * (int) Math.pow(256, i);
		// System.out.println(indent + "nAvgBytesPerSec: " + nAvgBytesPerSec);
		int nBlockAlign = 0;
		for (int i = 0; i < 2; i++)
			nBlockAlign += fis.readUnsignedByte() * (int) Math.pow(256, i);
		// System.out.println(indent + "nBlockAlign: " + nBlockAlign);
		int nBitsPerSample = 0;
		if (wFormatTag == 1) { // if MS PCM format
			nBitsPerSample = fis.readUnsignedByte();
			fis.skipBytes(1);
			// System.out.println(indent + "nBitsPerSample: " + nBitsPerSample);
		} else
			fis.skipBytes(2);
		fis.skipBytes(chunkSize - 16); // skip over any extra bytes
		// in format specific field.

		// Assume 16 bit signed little endian.
		format = new AudioFormat(nSamplesPerSec, nBitsPerSample, nChannels,
				true, false);
	}

	public AudioFormat getFormat() {
		return format;
	}

	/**
	 * 

	 * 
	 * @param framePos
	 *            frame postition reltive to start of audio. e.g. zero is start of audio.
	 *             
	 * @throws IOException
	 */
	public void seekFrame(long framePos) throws IOException {

		// pointer into audio data section
		fPtrBytes = framePos * 2 * nChannels;

		if (fPtrBytes >= 0) {
			// Offset the seek into audio section
			if (fPtrBytes < audioDataByteLength)
				fis.seek(fPtrBytes + audioDataStartBytePtr);
		} else {

			// Seek to the start of audio section
			fis.seek(audioDataStartBytePtr);
		}

	}

	/**
	 * 
	 * 
	 * Read from file into byte buffer and advance the fPtrBytes pointer
	 * it is OK to read before/after start/end of the file you'll just get zeros.
	 * 
	 * @param byteBuffer
	 *            buffer to fill
	 * @param offSet
	 *            offset into byteBuffer
	 * @param n
	 *            number of bytes to be read
	 * @throws IOException
	 */
	public void read(byte[] byteBuffer, int offSet, int n) throws IOException {

		if (fPtrBytes < 0) {

			// If before start of data then only read

			int nRead = (int) (n + fPtrBytes); // bytes of this read after
												// start of audioData

			if (nRead > 0) {
				int nFill = n - nRead; // read into the last portion of the
										// array
				for (int i = 0; i < nFill; i++)
					byteBuffer[i + offSet] = 0; // TODO array copy
				fis.read(byteBuffer, offSet + nFill, nRead);
			} else {
				for (int i = 0; i < n; i++)
					byteBuffer[i + offSet] = 0; // TODO array copy
			}

		} else if (fPtrBytes > audioDataByteLength) {

			// After end of audio data

			for (int i = 0; i < n; i++)
				byteBuffer[i + offSet] = 0; // TODO array copy
		} else {

			int nExtra = (int) (fPtrBytes + n - audioDataByteLength);

			if (nExtra > 0) {

				// Some bytes after end of the audio section ?

				int nRead = n - nExtra;
				fis.read(byteBuffer, offSet, nRead);
				for (int i = nRead; i < n ; i++)
					byteBuffer[i + offSet] = 0; // TODO array copy
			} else {

				// all data in audio section
				int nread=fis.read(byteBuffer, offSet, n);
				if (nread != n)
					try {
						throw new Exception(" Ooops only read " + nread + " out of " + n + "  " + offSet );
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
		}
		fPtrBytes += n;

	}
	
	public int getChannels() { return nChannels;}
	
	public boolean eof() {
		try {
			return fPtrBytes-audioDataStartBytePtr >= fis.length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
		
	}
	
	public long getCurrentFrame() {
		return (fPtrBytes-audioDataStartBytePtr)/nChannels/2;		
	}
	
}
