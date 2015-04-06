/*
 * Created on 02-Jul-2006
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
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class OutputBufferTest implements Runnable {

	Mixer.Info currentMixer;

	AudioFormat format = new AudioFormat(44100.0f, 16, 2, true, true);

	DataLine.Info infoOut = new DataLine.Info(SourceDataLine.class, format);

	SourceDataLine lineOut;

	boolean isRunning = false;

	boolean hasStopped = false;

	// 512 frames by default
	int bufferSize = 2048;

	/**
	 * Ultra low latency mode can be used for small buffer sizes to obtain
	 * better latency - BUT it eats all your CPU.
	 */
	protected boolean ultraLowLatency = false;

	/**
	 * Use Java standard way of latency control (blocking on sdl.write)
	 */
	protected boolean standardLatency = false;

	/**
	 * Use Frinika estimated frame pos (using System.nanoTime) or
	 * SDL.getLongFramePosition
	 */
	private boolean useEstimatedFramePos = true;

	public OutputBufferTest(int bufferSize) {
		if (System.getProperty("os.name").equals("Mac OS X")) {
			System.out
					.println("Detected Mac OS X. Automatically tuning audio device settings. ");
			// These are the settings working best on a G5 iMac 2Ghz
			useEstimatedFramePos = false;
			ultraLowLatency = true;
		}
		currentMixer = AudioSystem.getMixerInfo()[0];
		this.bufferSize = 4 * bufferSize;
		startAudioOutput();
	}

	public void startAudioOutput() {

		try {
			lineOut = (SourceDataLine) AudioSystem.getMixer(currentMixer)
					.getLine(infoOut);
			if (standardLatency)
				lineOut.open(format, bufferSize);
			else
				lineOut.open(format);

			lineOut.start();
			System.out.println("Buffersize: " + bufferSize + " / "
					+ lineOut.getBufferSize());
		} catch (Exception e) {
			lineOut = null;
			System.out
					.println("No audio output available. Use Audio Devices dialog to reconfigure.");
		}

		Thread thread = new Thread(this);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	public void stopAudioOutput() throws Exception {
		isRunning = false;
		while (!hasStopped)
			Thread.yield();
		hasStopped = false;
		if (lineOut != null) {
			lineOut.drain();
			lineOut.stop();
			lineOut.close();
		}
	}

	public void run() {
		try {
			isRunning = true;
			byte[] outBuffer = new byte[bufferSize];
			float[] floatBuffer = new float[bufferSize / 2];

			long totalTimeNanos = (long) ((bufferSize / 4f) / (44100.f * 1000000000f));
			// nanoTime when buffer expires
			long expireNanos = 0;
			long framesWritten = 0;

			while (isRunning) {
				long startTimeNanos = System.nanoTime();
				for (int n = 0; n < (floatBuffer.length); n++)
					floatBuffer[n] = 0;

				long endTimeNanos = System.nanoTime();

				if (lineOut != null) {
					System.out.println(lineOut.getBufferSize()
							- lineOut.available());
					lineOut.write(outBuffer, 0, outBuffer.length);
				}

					if (expireNanos < System.nanoTime())
						expireNanos = System.nanoTime() + totalTimeNanos;
					else
						expireNanos += totalTimeNanos;

					long sleepNanos = expireNanos - totalTimeNanos
							- System.nanoTime();
					if (sleepNanos > 0)
						Thread.sleep(sleepNanos / 1000000,
								(int) (sleepNanos % 1000000));
				}
				// This is used when not using the Frinika estimated position
				framesWritten += (outBuffer.length / 4);

				// cpuMeter.setCpuPercent((int)(((float)(endTimeNanos -
				// startTimeNanos) / (float)totalTimeNanos) * 100));
		} catch (Exception e) {
			e.printStackTrace();
		}
		hasStopped = true;
	}

	public void setBufferSize(int len) throws Exception {
		stopAudioOutput();
		bufferSize = len * 4;

		startAudioOutput();
	}
}
