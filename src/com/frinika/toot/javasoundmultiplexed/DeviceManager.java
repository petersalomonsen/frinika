/*
 * Created on Apr 10, 2007
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

package com.frinika.toot.javasoundmultiplexed;

import java.util.List;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

// import javax.sound.sampled.TargetDataLine;

/**
 * Utillity for this package finds the widest line on the mixers and constructs
 * a list of Targets and Sources
 * 
 * @author pjl
 * 
 */
class DeviceManager {

	private static int sampleRate = 44100; // !!! !!!

	private static boolean bigEndian = false; // !!! !!!

	static AudioFormat monoFormat = new AudioFormat((float) sampleRate, 16, 1,
			true, bigEndian);

	static AudioFormat stereoFormat = new AudioFormat((float) sampleRate, 16,
			2, true, bigEndian);

	static AudioFormat format = new AudioFormat((float) sampleRate, 16, 0,
			true, bigEndian);

	/* list of available connections */
	// Vector<AudioConnection> connections = new Vector<AudioConnection>();
	/* list of JavaSoundSound devices that can be used to make connections */
	Vector<JavaSoundOutDevice> outDevices = new Vector<JavaSoundOutDevice>();

	/* list of JavaSoundSound devices that can be used to make connections */
	Vector<JavaSoundInDevice> inDevices = new Vector<JavaSoundInDevice>();

	static DeviceManager the;

	class MyException extends Exception {
		MyException(String str) {
			super(str);
		}

	};

	public DeviceManager(int bufferSize) throws MyException {

		if (the != null)
			throw new MyException(
					" JavaSoundConnectionManager is a singleton. ");
		the = this;
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		// System.out.println("Available Mixers: " + mixerInfos.length);

		for (int i = 0; i < mixerInfos.length; i++) {
			System.out.println("********************\n Mixer " + i + ": "
					+ mixerInfos[i].getName() + " desc: "
					+ mixerInfos[i].getDescription() + " vend: "
					+ mixerInfos[i].getVendor() + " ver: "
					+ mixerInfos[i].getVersion());

			if (mixerInfos[i].getName().startsWith("Port "))
				continue;

			Mixer mixer = AudioSystem.getMixer(mixerInfos[i]);

			if (mixer.isOpen()) {
				System.out.println(" It's open already ");
				// continue;
			} else {
				try {
					mixer.open();
				} catch (LineUnavailableException e) {
					System.out.println(" Unavailable");
					continue;
				}
			}

			Line.Info[] targetLines = mixer.getTargetLineInfo();
			// System.out
			// .println(" --------------- INPUT DEVICES
			// --------------------------------");
			//					

			for (Line.Info info : targetLines) {

				try {
					@SuppressWarnings("unused")
					Line line = mixer.getLine(info);
					// System.out.println(info + " | " + line);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}

				if (info instanceof DataLine.Info) {
					DataLine.Info dinfo = (DataLine.Info) info;
					AudioFormat widest = null;
					int frameSize = 0;

					for (AudioFormat af : dinfo.getFormats()) {

						if (af.getEncoding() != format.getEncoding())
							continue;
						if (af.isBigEndian() != format.isBigEndian())
							continue;
						if (af.getSampleSizeInBits() != format
								.getSampleSizeInBits())
							continue;
						if (af.getFrameSize() > frameSize) {
							widest = af;
							frameSize = af.getFrameSize();
						}

						// System.out.println(af);

					}

					if (widest != null) {
						JavaSoundInDevice device = new JavaSoundInDevice(mixer,
								new AudioFormat(sampleRate, 16, widest
										.getChannels(), true, bigEndian),
								dinfo, bufferSize);
						System.out.println(" IN: " + device.getName());
						inDevices.add(device);
					}
				}
			}

			Line.Info[] sourceLines = mixer.getSourceLineInfo();
			// System.out
			// .println(" --------------- OUTPUT DEVICES
			// --------------------------------");
			for (Line.Info info : sourceLines) {
				Line line = null;
				try {
					line = mixer.getLine(info);
					if (!(line instanceof SourceDataLine))
						continue;
					System.out.println(info + " | " + line);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}

				if (info instanceof DataLine.Info) {
					DataLine.Info dinfo = (DataLine.Info) info;
					AudioFormat widest = null;
					int frameSize = 0;

					for (AudioFormat af : dinfo.getFormats()) {

						if (af.getEncoding() != format.getEncoding())
							continue;
						if (af.isBigEndian() != format.isBigEndian())
							continue;
						if (af.getSampleSizeInBits() != format
								.getSampleSizeInBits())
							continue;
						if (af.getFrameSize() > frameSize) {
							widest = af;
							frameSize = af.getFrameSize();
						}

						// System.out.println(af);

					}
					if (widest != null) {
						JavaSoundOutDevice device = new JavaSoundOutDevice(
								mixer, new AudioFormat(sampleRate, 16, widest
										.getChannels(), true, bigEndian),
								dinfo, bufferSize);
						System.out.println(" OUT: " + device.getName());
						outDevices.add(device);
					}
				}
			}

			mixer.close();

		}
	}

	JavaSoundOutDevice getOutDevice(String name) {
		for (JavaSoundOutDevice dev : outDevices) {
			if (name.equals(dev.getName()))
				return dev;
		}
		return null;
	}

	JavaSoundInDevice getInDevice(String name) {
		for (JavaSoundInDevice dev : inDevices) {
			if (name.equals(dev.getName()))
				return dev;
		}
		return null;
	}

	List<String> getInDeviceList() {
		Vector<String> list = new Vector<String>();
		for (JavaSoundInDevice in : inDevices) {
			list.add(in.getName());
		}
		return list;
	}

	List<String> getOutDeviceList() {
		Vector<String> list = new Vector<String>();
		for (JavaSoundOutDevice in : outDevices) {
			list.add(in.getName());
		}
		return list;
	}
}
