/*
 * Created on Jul 18, 2006
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

package com.frinika.benchmark;

/*
 * riffread class reads existing RIFF format file and determines (1) if valid
 * RIFF file (2) chunk structure (3) details of fmt chunk by M. Gallant 2/14/97
 */

import java.io.*;
import java.awt.*;

public class AudioReader {

	public static void main(String args[]) throws IOException {
		String nl = System.getProperty("line.separator");
		String fileSeparator = System.getProperty("file.separator");
		String selectFileDir;
		String selectFile;
		FileInputStream fis = null;
		FileOutputStream fos = null;

		Frame myFrame = new Frame("Parent Frame");
		FileDialog myFD;
		myFrame.resize(200, 200);
		// myFrame.show() ;
		myFrame.hide();

		myFD = new FileDialog(myFrame, "Open a Text File");
		myFD.show(); // this blocks main program thread until select.

		// System.out.println("Chosen File" +myFD.getFile() );
		// System.out.println("Chosen Directory" +myFD.getDirectory() ) ;

		selectFileDir = myFD.getDirectory(); // determine is file directory
												// ends properly.
		if (selectFileDir.charAt(selectFileDir.length() - 1) != fileSeparator
				.charAt(0))
			selectFileDir += fileSeparator;

		selectFile = selectFileDir + myFD.getFile();
		// System.out.println("Input File Location " + selectFile) ;

		try {
			fis = new FileInputStream(selectFile);
		} catch (IOException ie) {
		}

		DataInputStream dis = new DataInputStream(fis);

		try {

			int riffdata = 0; // size of RIFF data chunk.
			int chunkSize = 0, bytecount = 0;
			String sfield = "";
			String sp = "     "; // spacer string.
			String indent = sp + "     ";
			long filesize = (new File(selectFile)).length(); // get file
																// length.
			System.out.println(" ******  FILE:  " + selectFile
					+ "    LENGTH:  " + filesize + " bytes ****** \n");

			/* -------- Get RIFF chunk header --------- */
			for (int i = 1; i <= 4; i++)
				sfield += (char) dis.readByte();
			if (!sfield.equals("RIFF")) {
				System.out.println(" ****  Not a valid RIFF file  ****");
				return;
			}

			for (int i = 0; i < 4; i++)
				chunkSize += dis.readUnsignedByte() * (int) Math.pow(256, i);
			System.out.println(sp + sfield + "    ----- data size: "
					+ chunkSize + " bytes");

			sfield = "";
			for (int i = 1; i <= 4; i++)
				sfield += (char) dis.readByte();
			System.out
					.println(sp + "        ----- form type: " + sfield + "\n");

			riffdata = chunkSize;
			/* --------------------------------------------- */

			bytecount = 4; // initialize bytecount to include RIFF form-type
							// bytes.

			while (bytecount < riffdata) { // check for chunks inside RIFF data
											// area.
				sfield = "";
				for (int i = 1; i <= 4; i++)
					sfield += (char) dis.readByte();

				chunkSize = 0;
				for (int i = 0; i < 4; i++)
					chunkSize += dis.readUnsignedByte()
							* (int) Math.pow(256, i);
				bytecount += (8 + chunkSize);
				System.out.println("\n" + sp + sfield + "    ----- data size: "
						+ chunkSize + " bytes");

				if (sfield.equals("fmt ")) { // extract info from "format"
												// chunk.
					if (chunkSize < 16) {
						System.out
								.println(" ****  Not a valid fmt chunk  ****");
						return;
					}
					int wFormatTag = dis.readUnsignedByte();
					dis.skipBytes(1);
					if (wFormatTag == 1)
						System.out.println(indent
								+ "wFormatTag:  MS PCM format");
					else
						System.out.println(indent
								+ "wFormatTag:  non-PCM format");
					int nChannels = dis.readUnsignedByte();
					dis.skipBytes(1);
					System.out.println(indent + "nChannels:  " + nChannels);
					int nSamplesPerSec = 0;
					for (int i = 0; i < 4; i++)
						nSamplesPerSec += dis.readUnsignedByte()
								* (int) Math.pow(256, i);
					System.out.println(indent + "nSamplesPerSec:  "
							+ nSamplesPerSec);
					int nAvgBytesPerSec = 0;
					for (int i = 0; i < 4; i++)
						nAvgBytesPerSec += dis.readUnsignedByte()
								* (int) Math.pow(256, i);
					System.out.println(indent + "nAvgBytesPerSec:  "
							+ nAvgBytesPerSec);
					int nBlockAlign = 0;
					for (int i = 0; i < 2; i++)
						nBlockAlign += dis.readUnsignedByte()
								* (int) Math.pow(256, i);
					System.out.println(indent + "nBlockAlign:  " + nBlockAlign);
					if (wFormatTag == 1) { // if MS PCM format
						int nBitsPerSample = dis.readUnsignedByte();
						dis.skipBytes(1);
						System.out.println(indent + "nBitsPerSample:  "
								+ nBitsPerSample);
					} else
						dis.skipBytes(2);
					dis.skipBytes(chunkSize - 16); // skip over any extra bytes
													// in format specific field.
				} else
					// if NOT the fmt chunk.
					dis.skipBytes(chunkSize);

			} // end while.

			System.out.println("\n" + sp + "Final RIFF data bytecount: "
					+ bytecount);
			if ((8 + bytecount) != (int) filesize)
				System.out.println(sp
						+ "!!!!!!! Problem with file structure  !!!!!!!!! ");
			else
				System.out.println(sp
						+ "File chunk structure consistent with valid RIFF ");
			System.out
					.println(" -------------------------------------------------------");
		} // end try.

		finally {
			myFrame.dispose();
			dis.close(); // close all streams.
			fis.close();
		}
	}

}
