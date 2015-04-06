/*
 * Created on May 8, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.tracker.filedialogs;

import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.frinika.global.FrinikaConfig;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.model.AudioLane;
import com.frinika.sequencer.model.AudioPart;
import com.frinika.tools.MyMidiRenderer;
import com.frinika.tools.ProgressBarInputStream;

/**
 * Dialog for export to wav, and monitoring progress
 * @author Peter Johan Salomonsen
 *
 */
public class BounceToLane extends JDialog implements Runnable {
    private static final long serialVersionUID = 1L;

    JProgressBar progressBar;
    MyMidiRenderer midiRenderer;
    
    ProjectContainer project;
    File file;
    AudioLane lane;
    int numberOfSamples;
    
    public BounceToLane(JFrame frame,
            ProjectContainer project, 
            File file,
            long startTick, 
            long endTick,
            AudioLane lane)
    {
        super(frame,true);
        this.lane=lane;
        this.project = project;
        this.setResizable(false);
        this.setUndecorated(true);
        try
        {
            midiRenderer = new MyMidiRenderer(project.getMixer(),project.getSequencer(),startTick,(int)(endTick-startTick),project.getAudioServer().getSampleRate());
            numberOfSamples = midiRenderer.available()/4;
            progressBar = new JProgressBar(0,midiRenderer.available());
            progressBar.setStringPainted(true);
            
            setLayout(new GridLayout(0,1));
            
            JLabel lb = new JLabel("Exporting section to "+file.getName());
            lb.setFont(new Font(lb.getFont().getName(),Font.BOLD,lb.getFont().getSize()*2));
            add(lb);
            add(progressBar);

            this.file = file;
    
            new Thread(this).start();
            
            this.setSize(getPreferredSize());
            
            this.setLocationRelativeTo(frame);
            this.setVisible(true);
        } catch(Exception e) {}
    }
    
    public void run() {
    	// Stop audio server
    	project.getAudioServer().stop();

    	try
        {
        	AudioInputStream ais = new AudioInputStream(new ProgressBarInputStream(progressBar,midiRenderer),new AudioFormat((float) FrinikaConfig.sampleRate,16,2,true,true),numberOfSamples);
            FrinikaSequencer sequencer = project.getSequencer();
            sequencer.setRealtime(false);
            sequencer.start();
            AudioSystem.write(ais,AudioFileFormat.Type.WAVE,file);
            sequencer.stop();
            sequencer.setRealtime(true);
            BounceToLane.this.dispose();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        // Restore output process (mixer) from project and restart audio server
        
        project.getMixer().getMainBus().setOutputProcess(project.getOutputProcess());
        project.getAudioServer().start();
 
 		AudioPart part;
		try {
			part = new AudioPart(lane, file, (long) midiRenderer.getStartTimeInMicros());
			part.onLoad();		
			// TODO where is the mark ?  EDIT _HISTORY IS NOT MULTITHREADED
			project.getEditHistoryContainer().notifyEditHistoryListeners();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
    }
}
