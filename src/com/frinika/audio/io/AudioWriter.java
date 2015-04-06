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


import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

/*
 * Uses a RandomAccessFile to write a wav file. Remember to close it . . . to
 * write the length of data into the correct field.
 * 
 */
public class AudioWriter extends BasicAudioWriter implements AudioProcess  {

 public AudioWriter(File file, AudioFormat format) throws IOException {
     super(file,format);
 }
	public int processAudio(AudioBuffer buffer) {

		float[] left;
		float[] right;

		// Correct byte buffer size
		if (byteBuffer == null
				|| byteBuffer.length != buffer.getSampleCount() * 2 * nChannel)
			byteBuffer = new byte[buffer.getSampleCount() * 2 * nChannel];

		int nSamp = buffer.getSampleCount();
		// Decode byte data and insert into voiceserver buffer
		int count = 0;

		if (nChannel == 2) {
			left = buffer.getChannel(0);
			right = buffer.getChannel(1);

			for (int n = 0; n < nSamp; n++) {
				short leftI = (short) (left[n] * 32768f);
				short rightI = (short) (right[n] * 32768f);
				byteBuffer[count++] = (byte) (0xff & leftI);
				byteBuffer[count++] = (byte) (0xff & (leftI >> 8));
				byteBuffer[count++] = (byte) (0xff & rightI);
				byteBuffer[count++] = (byte) (0xff & (rightI >> 8));
			}
		} else {

			left = buffer.getChannel(0);
			for (int n = 0; n < nSamp; n++) {
				short leftI = (short) (left[n] * 32768f);
				byteBuffer[count++] = (byte) (0xff & leftI);
				byteBuffer[count++] = (byte) (0xff & (leftI >> 8));
			}
		}
		try {
			write(byteBuffer, 0, count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return AudioProcess.AUDIO_DISCONNECT;
		}

		return AUDIO_OK;
	}

}
