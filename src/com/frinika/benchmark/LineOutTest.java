/*
 * Created on Jul 5, 2006
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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class LineOutTest {

	static Mixer.Info currentMixer = AudioSystem.getMixerInfo()[0];

	static final float sampleRate = 44100.0f;

	static AudioFormat format = new AudioFormat(sampleRate, 16, 2, true, true);

	static DataLine.Info infoOut = new DataLine.Info(SourceDataLine.class,
			format);

	static int frameSize = format.getFrameSize();

	static SourceDataLine lineOut;



	public static void main(String args[]) {


		long deltaSizeFrames = 512 ;

		long deltaNanos = (long) ((1e9 * deltaSizeFrames) / sampleRate );

		System.out.println(" delta ="+nf(deltaNanos));
		
		try {
			lineOut = (SourceDataLine) AudioSystem.getMixer(currentMixer).getLine(
					infoOut);
			lineOut.open(format);
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lineOut.start();

		int bufferSize=lineOut.getBufferSize();
		byte buf[] = new byte[(int) bufferSize];

		lineOut.write(buf, 0, bufferSize);

		long t1 = System.nanoTime();
		long nanosTarget = t1 +deltaNanos;
		long availLast = lineOut.available();
	
		do {		

			long sleepNanos = nanosTarget-System.nanoTime();
			
			if (sleepNanos > 0 ) {
				try {
					Thread.sleep(sleepNanos / 1000000, (int) (sleepNanos % 1000000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println(" Missed a block " + sleepNanos);
			}
			long t2 = System.nanoTime();
			
			long nanoGlitch = t2-nanosTarget; 
			long availNow = lineOut.available();
			long deltaAvail=availNow-availLast;
			System.out.println(nf(nanoGlitch) + " " + deltaAvail + " ! " + (long)(((t2-t1)*sampleRate*frameSize*2)/1e9) );
			
			nanosTarget += deltaNanos;
			availLast=availNow;
			t1=t2;
		} while (true);

	}

	static String nf(long nanos) {
		return String.format(" %6.3f mS",nanos/1e6);		
		
	}
}
