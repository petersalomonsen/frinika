/*
 * Created on Jun 3, 2007
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


import com.frinika.audio.io.*;
import java.io.IOException;
import uk.org.toot.audio.core.AudioBuffer;

import com.frinika.sequencer.model.AudioPart.Envelope;

public class EnvelopedAudioReader extends AudioReader {

	protected double gain = 1.0;

	protected long attackEnd;

	protected long decayStart;

	public EnvelopedAudioReader(RandomAccessFileIF fis,float Fs) throws IOException {
		super(fis,Fs);
	}


	public void setEvelope(Envelope e) {

		setBoundsInMicros(e.getTOn(),e.getTOff());
		gain = e.getGain();
		attackEnd = startByte + milliToByte(e.getTRise());
		decayStart = endByte - milliToByte(e.getTFall());
		if (attackEnd > decayStart) {
			long av=(attackEnd+decayStart)/2;
			attackEnd=decayStart= (av/(nChannels*2))*nChannels*2;
		}

	//	System.out.println(startByte + "-->" + endByte);

	}

	protected void processAudioImp(AudioBuffer buffer, int startChunk,
			int endChunk) {
		// Decode byte data and insert into voiceserver buffer
		// TODO duplicate code to save ops if no envelope etc.

		long fPtr1 = fPtrBytes + startChunk;
		long fPtr2 = fPtrBytes + endChunk;

		if (fPtr1 <= decayStart && fPtr2 >= attackEnd) { // all in constant
			fillConstantGain(buffer, startChunk, endChunk, gain);
		} else if (fPtr1 < attackEnd && fPtr2 >= attackEnd) { // rise constant
																// transition
			double gainNow = gain * (fPtr1 - startByte)
					/ (attackEnd - startByte);
			fillLinearInterpolate(buffer, startChunk, endChunk, gainNow, gain);
		} else if (fPtr1 < attackEnd) { // all in rise
			double gainNow = gain * (fPtr1 - startByte)
					/ (attackEnd - startByte);
			double gainNext = gain * (fPtr2 - startByte)
					/ (attackEnd - startByte);
			fillLinearInterpolate(buffer, startChunk, endChunk, gainNow,
					gainNext);
		} else if (fPtr1 <= decayStart && fPtr2 > decayStart) { // const - decay
			double gainNext = gain * (endByte - fPtr2) / (endByte - decayStart);
			fillLinearInterpolate(buffer, startChunk, endChunk, gain, gainNext);
		} else {
			assert (fPtr1 > decayStart);
			double gainNow = gain * (endByte - fPtr1) / (endByte - decayStart);
			double gainNext = gain * (endByte - fPtr2) / (endByte - decayStart);
			fillLinearInterpolate(buffer, startChunk, endChunk, gainNow,
					gainNext);
		}
	}

}