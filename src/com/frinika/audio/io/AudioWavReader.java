/*
 * Created on May 31, 2007
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

package com.frinika.audio.io;

import java.io.IOException;
import java.io.RandomAccessFile;

import javax.sound.sampled.AudioFormat;

public class AudioWavReader {

	protected static String sp = "     ";

	static String indent = sp + "     ";

	protected long audioDataStartBytePtr;

	protected int audioDataByteLength;

	protected int lengthInFrames=0;

	protected int bytecount = 0;

	protected int riffdata = 0;

	protected AudioFormat format;

	protected int nChannels;

	private RandomAccessFile fis;

	public AudioWavReader(RandomAccessFile fis) throws IOException {
		String sfield = "";
		this.fis = fis;
		fis.seek(0);

		long filesize = (fis.length()); // get file

		readChunkHeader(fis);

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
				// S ystem.out.println(" Audio offset " +
				// audioDataStartBytePtr);
				audioDataByteLength = (int) (filesize - audioDataStartBytePtr);
			}

			bytecount += (8 + chunkSize);

			if (sfield.equals("fmt ")) { // extract info from "format"

				readFormat(fis, chunkSize);
			} else
				// if NOT the fmt chunk.
				fis.skipBytes(chunkSize);

			lengthInFrames = chunkSize / format.getFrameSize();

			// S ystem.out.println(lengthInFrames + " " + audioDataByteLength
			// + " " + format.getFrameSize());
		} // end while.

		if ((8 + bytecount) != (int) filesize)
			System.out.println(sp
					+ "!!!!!!! Problem with file structure  !!!!!!!!! ");

	}

	/**
	 * 
	 * read the data size. this will be zero until it is closed.
	 * 
	 * @return
	 * @throws IOException
	 */
	public int getDataSize() throws IOException {
		long ptr = fis.getFilePointer();
		fis.seek(40);

		int chunkSize = 0;

		for (int i = 0; i < 4; i++)
			chunkSize += fis.readUnsignedByte() * (int) Math.pow(256, i);

		riffdata = chunkSize;
		fis.seek(ptr);
		lengthInFrames = chunkSize / format.getFrameSize();
		
		System.out.println(" GET DATA SIZE " + lengthInFrames);
		return chunkSize;
	}

	protected void readChunkHeader(RandomAccessFile fis) throws IOException {
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
		// S ystem.out.println(sp + " ----- form type: " + sfield + "\n");

		riffdata = chunkSize;
		/* --------------------------------------------- */

		bytecount = 4; // initialize bytecount to include RIFF form-type
		// bytes.

	}

	public int getLengthInFrames() {
		return lengthInFrames;
	}

	protected void readFormat(RandomAccessFile fis, int chunkSize)
			throws IOException {
		// chunk.

		if (chunkSize < 16) {
			System.out.println(" ****  Not a valid fmt chunk  ****");
			return;
		}
		int wFormatTag = fis.readUnsignedByte();
		fis.skipBytes(1);
		// if (wFormatTag == 1)
		// System. out.println(indent + "wFormatTag: MS PCM format");
		// else
		// System. out.println(indent + "wFormatTag: non-PCM format");
		nChannels = fis.readUnsignedByte();
		fis.skipBytes(1);
		// System out.println(indent + "nChannels: " + nChannels);
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

	public int getChannels() {
		return nChannels;
	}

}
