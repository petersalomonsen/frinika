package com.frinika.codeexamples;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.VoiceServer;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.audio.DAudioReader;

/*
 * Created on Mar 8, 2006
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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

/**
 * Simple example of audio playback within Frinika. This is just meant to be a
 * guidance of how to implement audio playback. It's not intended that the audio
 * playback voice should be constantly present, like it is in this example. This
 * is only for testing purposes.
 * 
 * Replace the audio clip file with a file of your own.
 * 
 * A good test case, is an export of a project to wav, and play it along with
 * the metronome (remember to have the same tempo).
 * 
 * @author Peter Johan Salomonsen
 */
public class AudioPlayBackExampleDA {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// Create the audio context
		final VoiceServer voiceServer = new AudioContext().getVoiceServer();

		// Create the project container
		final ProjectContainer proj = new ProjectContainer();

		String nl = System.getProperty("line.separator");
		String fileSeparator = System.getProperty("file.separator");
		String selectFileDir;
		String selectFile;
		RandomAccessFile fis = null;

		Frame myFrame = new Frame("Parent Frame");
		FileDialog myFD;
		myFrame.setSize(200, 200);
		// myFrame.show() ;
		myFrame.setVisible(false);

		myFD = new FileDialog(myFrame, "Open a Wav");
		myFD.setVisible(true); // this blocks main program thread until select.

		selectFileDir = myFD.getDirectory(); // determine is file directory

		if (selectFileDir.charAt(selectFileDir.length() - 1) != fileSeparator
				.charAt(0))
			selectFileDir += fileSeparator;

		selectFile = selectFileDir + myFD.getFile();
		
		try {
			fis = new RandomAccessFile(selectFile, "r");

			DAudioReader dar = new DAudioReader(fis);

			// PJS: This is broken since this is now using Toot
/*			final DAAudioStreamVoice voice = new DAAudioStreamVoice(
					voiceServer, (FrinikaSequencer) (proj.getSequencer()),
			*/
			new ProjectFrame(proj);
		} catch (IOException ie) {

		} finally {
			myFrame.dispose();
			// fis.close();
		}

	}
}
