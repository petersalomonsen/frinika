/*
 * Created on Jun 20, 2005
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
package com.frinika.synth.synths.sampler;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * The RecorderGUI class is providing a user interface for monitoring sampler recording progress.
 * @author Peter Johan Salomonsen
 *
 */

public class RecorderGUI extends JFrame implements RecordProgressListener{
    private static final long serialVersionUID = 1L;
    private JProgressBar progressBar;
    private JLabel stageLabel;
    private int stage = 0;
    private SamplerOscillator samplerOscillator;
    
    public RecorderGUI(SamplerOscillator samplerOscillator)
    {
        this.samplerOscillator = samplerOscillator;
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx=GridBagConstraints.REMAINDER;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        stageLabel = new JLabel("Waiting for MIDI note on to start recording");
        new Thread()
        {
            public void run()
            {
                while(stage==0)
                {
                    if(stage==0) // In case of concurrent modifications 
                        stageLabel.setForeground(Color.BLACK);
                    try { Thread.sleep(500); } catch(Exception e ) {}
                    if(stage==0) // In case of concurrent modifications
                        stageLabel.setForeground(stageLabel.getBackground());
                    try { Thread.sleep(500); } catch(Exception e ) {}
                }
            }
        }.start();
        
        add(stageLabel,gc);
        add(new JLabel(" "),gc);
        add(new JLabel("Memory usage:"),gc);
        progressBar = new JProgressBar(0,samplerOscillator.available());
        progressBar.setStringPainted(true);
        add(progressBar,gc);
        samplerOscillator.setRecordProgressListener(this);
        setAlwaysOnTop(true);
        setVisible(true);
        setSize(getPreferredSize());
        validate();
    }

    public void updateProgress(int samplesRecorded) {
        if(samplesRecorded>0 && stage == 0)
        {
            stage = 1;
            stageLabel.setForeground(Color.RED);
            stageLabel.setText("Recording until MIDI note off");
            
        }
        progressBar.setValue(samplesRecorded);
    }

    public void finished() {
        stageLabel.setForeground(Color.GREEN);
        stageLabel.setText("Finished recording");
        stage = 2;
        samplerOscillator.setRecordProgressListener(null);        
    }    
}
