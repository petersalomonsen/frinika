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

import com.frinika.global.FrinikaConfig;
import com.frinika.project.FrinikaAudioServer;
import com.frinika.project.FrinikaAudioSystem;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.tools.MyMidiRenderer;
import com.frinika.tools.ProgressBarInputStream;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * Dialog for export to wav, and monitoring progress
 * @author Peter Johan Salomonsen
 *
 */
public class ExportWavDialog extends JDialog implements Runnable {
    private static final long serialVersionUID = 1L;

    JProgressBar progressBar;
    MyMidiRenderer midiRenderer;
    
    ProjectContainer project;
    File file;
    javax.sound.sampled.AudioFileFormat.Type type;
    
    int numberOfSamples;
    
    public ExportWavDialog(JFrame frame,
            ProjectContainer project, 
            javax.sound.sampled.AudioFileFormat.Type type,
            File file,
            long startTick, 
            long endTick)
    {
        super(frame,true);
         
        this.project = project;
        this.setResizable(false);
        this.setUndecorated(true);
        try
        {
        	FrinikaAudioServer audioServer=FrinikaAudioSystem.getAudioServer();
     
           	project.getAudioServer().stop();
            
        	audioServer.setRealTime(false);
      
        	
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
            this.type = type;
            Thread.sleep(100);
            new Thread(this).start();
            
            this.setSize(getPreferredSize());
            
            this.setLocationRelativeTo(frame);
            this.setVisible(true);
        } catch(Exception e) {}
    }
    
    public void run() {
    	// Stop audio server
    

   
    	try
        {
        	AudioInputStream ais = new AudioInputStream(new ProgressBarInputStream(progressBar,midiRenderer),new AudioFormat((float) FrinikaConfig.sampleRate,16,2,true,true),numberOfSamples);
            FrinikaSequencer sequencer = project.getSequencer();
            sequencer.setRealtime(false);
            sequencer.start();
            AudioSystem.write(ais,type,file);
            sequencer.stop();
            sequencer.setRealtime(true);
            ExportWavDialog.this.dispose();
        } catch(Exception e) {
        	e.printStackTrace();
        }
        // Restore output process (mixer) from project and restart audio server
        
        project.getMixer().getMainBus().setOutputProcess(project.getOutputProcess());
    	FrinikaAudioServer audioServer=FrinikaAudioSystem.getAudioServer();
    	audioServer.setRealTime(true);
        project.getAudioServer().start();
    }
}
