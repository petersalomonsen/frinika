/*
 * Created on 29 Dec 2007
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

package com.frinika.audio.analysis;

import com.frinika.audio.analysis.dft.AudioFlags;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
/**
 *
 * Implementation of a cyclic buffer.
 *
 * This buffer is fed using the in.processAudio(buff);
 *   this returns OVERFLOW if the buffer is full
 *
 * You read the buffer using out.processAudio(buff);
 *   this will block if no data is ready
 *
 *
 * @author pjl
 */
public class CycliclyBufferedAudio {

	public final static int OVERFLOW = -1;

	float buff[];

	int cacheSize;

	long inPtr = 0;

	long outPtr = 0;

	Thread outBlockingThread = null;

	public final AudioProcess in = new In();

	public final AudioProcess out = new Out();

	private int required;

	private boolean itWasMe = false;

	// private boolean disconnected = false;

	int overflowCount=0;

	private boolean blocking=false;
    private float Fs;
	
	public CycliclyBufferedAudio(int cacheSize,float Fs) {
		this.cacheSize = cacheSize;
        this.Fs=Fs;
		buff = new float[cacheSize];
	}

    public float getSampleRate() {
        return Fs;
    }

	class In implements AudioProcess {

		int cnt;

		public void close() throws Exception {
			// TODO Auto-generated method stub

		}

		public void open() throws Exception {
			// TODO Auto-generated method stub

		}

		public int processAudio(AudioBuffer buffer) {

			int n = buffer.getSampleCount();

			if (inPtr + n - outPtr > cacheSize) {
				System.out.println(" OVERFLOW " + overflowCount++);
				return OVERFLOW;
			}

	//		float a[] = buffer.getChannel(0);
	//		for (int i = 0; i < n; i++) {
	//			a[i] = (float) Math.sin(cnt++ * 2500.0 / 44100.0);
	//		}

			int inCy0 = (int) (inPtr % cacheSize);
			int inCy1 = (int) ((inPtr + n) % cacheSize);

			if (inCy1 > inCy0) {
				System.arraycopy(buffer.getChannel(0), 0, buff, inCy0, n);
			} else {
				System.arraycopy(buffer.getChannel(0), 0, buff, inCy0, n
						- inCy1);
				System.arraycopy(buffer.getChannel(0), n - inCy1, buff, 0,
						inCy1);
			}

			inPtr += n;

			if (blocking) { 
				if (inPtr - outPtr > required) {
					itWasMe = true;
					outBlockingThread.interrupt();   // Ha ha this can be null !!!!
				}
			}
			return AUDIO_OK;
		}
	}

	class Out implements AudioProcess {

		synchronized public int processAudio(AudioBuffer buffer) {

			required = buffer.getSampleCount();
			
			if (inPtr - outPtr < required) {
				
				outBlockingThread = Thread.currentThread();
				try {
					// clear any pending interupts
					while(Thread.interrupted()){}
					blocking=true;
					wait();
				} catch (InterruptedException e) {
			
					blocking=false;
					if (itWasMe) {
				//		outBlockingThread = null;
						itWasMe = false;
					} else {
						e.printStackTrace();
						e.getCause().printStackTrace();
		//				outBlockingThread = null;
						inPtr=0;
						outPtr=0;
						itWasMe=false;
						return AudioFlags.INTERRUPTED;
					}

					// System.out.println(" Buffer unblocked ");
				}
				assert (inPtr - outPtr >= required);
			}

			int outCy0 = (int) (outPtr % cacheSize);
			int outCy1 = (int) ((outPtr + required) % cacheSize);

			if (outCy1 > outCy0) {
				System.arraycopy(buff, outCy0, buffer.getChannel(0), 0,
						required);
				// buffer.getChannel(0), 0, buff, inCy0, n);
			} else {
				System.arraycopy(buff, outCy0, buffer.getChannel(0), 0,
						required - outCy1);
				System.arraycopy(buff, 0, buffer.getChannel(0), required
						- outCy1, outCy1);

				//				
				// System.arraycopy(buffer.getChannel(0), 0, buff, inCy0,
				// n-inCy1);
				// System.arraycopy(buffer.getChannel(0), n-inCy1, buff, 0,
				// inCy1);
			}

			outPtr += required;

			return AUDIO_OK;

		}

		public void close() throws Exception {
			// TODO Auto-generated method stub

		}

		public void open() throws Exception {
			// TODO Auto-generated method stub

		}

	}

	class OverflowException extends Exception {
        @Override
		public String toString() {
			return " BufferedAuioProcess:  Overflow ";
		}
	}

	public int getLag() {
		return (int) (inPtr-outPtr);
	}
}
