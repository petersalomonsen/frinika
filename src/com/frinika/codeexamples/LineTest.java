package com.frinika.codeexamples;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

public class LineTest {

	public static void main(String args[]) {

		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();

		System.out.println("Available Mixers: " + mixerInfos.length);

		for (int i = 0; i < mixerInfos.length; i++) {
			System.out.println("********************\n Mixer " + i + ": "
					+ mixerInfos[i].getName() + " desc: "
					+ mixerInfos[i].getDescription() + " vend: "
					+ mixerInfos[i].getVendor() + " ver: "
					+ mixerInfos[i].getVersion());
			Mixer mixer = AudioSystem.getMixer(mixerInfos[i]);

			try {
				mixer.open();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
			Line.Info[] sourceLines = mixer.getSourceLineInfo();
			System.out
					.println(" ---------------SOURCE -------------------------------- " + sourceLines.length);
			for (Line.Info info : sourceLines) {
				Line line = null;
				try {
					line = mixer.getLine(info);
					System.out.println(info + " | " + line);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
				if (info instanceof DataLine.Info) {
					DataLine.Info dinfo = (DataLine.Info) info;
					for (AudioFormat af : dinfo.getFormats()) {
						System.out.println(af);

					}
				}
			}
			
			Line.Info[] targetLines = mixer.getTargetLineInfo();
			System.out
			.println(" --------------- TARGET -------------------------------- " + targetLines.length);
			for (Line.Info info : targetLines) {
				Line line = null;
				try {
					line = mixer.getLine(info);
					System.out.println(info + " | " + line);
				} catch (LineUnavailableException e) {
					e.printStackTrace();
				}
				if (info instanceof DataLine.Info) {
					DataLine.Info dinfo = (DataLine.Info) info;
					for (AudioFormat af : dinfo.getFormats()) {
						System.out.println(af);

					}
				}

			}
			mixer.close();
		}
	}
}