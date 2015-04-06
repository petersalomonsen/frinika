package com.frinika.codeexamples;

import com.frinika.project.FrinikaAudioServer;
import java.io.File;

import com.frinika.project.ProjectContainer;

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
import com.frinika.tools.MyMidiRenderer;
import com.frinika.project.FrinikaAudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import com.frinika.global.FrinikaConfig;
import com.frinika.sequencer.FrinikaSequencer;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioSystem;

/**
 * Example of loading a Frinika project and playing it without opening the Frinika gui.
 * 
 * TODO: Threads should be able to stop without doing System.exit() (Check BufferedRandomAccessFileManager)
 * TODO: Test wav export with audio tracks
 * TODO: How to obtain end tick in song
 * 
 * @author Peter Johan Salomonsen
 */
public class StandaloneProjectWavExport {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Load a project
            FrinikaAudioSystem.usePhysicalAudioOutput = false;
            ProjectContainer project = ProjectContainer.loadProject(new File("/home/peter/mystudio/faro.frinika"));
            long startTick = 0;
            long endTick = 128*16;
            FrinikaAudioServer audioServer = (FrinikaAudioServer)project.getAudioServer();
            
            MyMidiRenderer midiRenderer = new MyMidiRenderer(project.getMixer(),project.getSequencer(),startTick,(int)(endTick-startTick),project.getAudioServer().getSampleRate());
            audioServer.setRealTime(false);
            long numberOfSamples = midiRenderer.available()/4;

            byte[] buffer = new byte[1024];
            
            Type type = Type.WAVE;
            File outputFile = new File("/home/peter/mystudio/faro.wav");
            
            try
            {
        	AudioInputStream ais = new AudioInputStream(midiRenderer,new AudioFormat((float) FrinikaConfig.sampleRate,16,2,true,true),numberOfSamples);
                FrinikaSequencer sequencer = project.getSequencer();
       
                sequencer.setRealtime(false);
                sequencer.start();
                System.out.println("Writing");
                AudioSystem.write(ais,type,outputFile);
                System.out.println("Done writing");
                sequencer.stop();
                project.close();
                System.out.println("Done with all");
            } catch(Exception e)
            {
                e.printStackTrace();
            }
            
        }
}
