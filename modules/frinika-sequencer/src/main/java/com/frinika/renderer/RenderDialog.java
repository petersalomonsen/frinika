/*
 * Created on May 8, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 *               2007 Karl Helgason
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
package com.frinika.renderer;

import com.frinika.base.FrinikaAudioServer;
import com.frinika.base.FrinikaAudioSystem;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.project.AbstractProjectContainer;
import com.frinika.sequencer.tools.MyMidiRenderer;
import com.frinika.tools.ProgressInputStream;
import com.frinika.tools.ProgressObserver;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class RenderDialog extends JDialog implements Runnable {

    private static final long serialVersionUID = 1L;

    JProgressBar progressBar;
    MyMidiRenderer midiRenderer;

    AbstractProjectContainer project;

    int numberOfSamples;

    public RenderDialog(JFrame frame,
            AbstractProjectContainer project,
            long startTick,
            long endTick) {
        super(frame, true);

        this.project = project;
        this.setResizable(false);
        this.setUndecorated(true);
        try {
            midiRenderer = new MyMidiRenderer(project.getMixer(), project.getSequencer(), startTick, (int) (endTick - startTick), project.getAudioServer().getSampleRate());
            numberOfSamples = midiRenderer.available() / 4;
            progressBar = new JProgressBar(0, midiRenderer.available());
            progressBar.setStringPainted(true);

            setLayout(new GridLayout(0, 1));

            JLabel lb = new JLabel("Render section");
            lb.setFont(new Font(lb.getFont().getName(), Font.BOLD, lb.getFont().getSize() * 2));
            add(lb);
            add(progressBar);

            new Thread(this).start();

            this.setSize(getPreferredSize());

            this.setLocationRelativeTo(frame);
            this.setVisible(true);
        } catch (IOException e) {
        }
    }

    byte[] buffer = new byte[1024];

    @Override
    public void run() {
        // Stop audio server
        FrinikaAudioServer audioServer = FrinikaAudioSystem.getAudioServer();

        project.getAudioServer().stop();
        audioServer.setRealTime(false);

        try {
            ProgressObserver observer = new ProgressObserver() {
                @Override
                public void setGoal(long maximumProgress) {
                    progressBar.setMaximum((int) maximumProgress);
                }

                @Override
                public void progress(long progress) {
                    progressBar.setValue((int) progress);
                }

                @Override
                public void finished() {
                }

                @Override
                public void fail(Exception ex) {
                    // TODO
                }
            };
            AudioInputStream ais = new AudioInputStream(new ProgressInputStream(observer, midiRenderer), new AudioFormat((float) FrinikaGlobalProperties.getSampleRate(), 16, 2, true, true), numberOfSamples);
            FrinikaSequencer sequencer = project.getSequencer();

            sequencer.setRealtime(false);
            sequencer.start();
            project.getMixer().setEnabled(true);
            int ret = 0;
            while (ret != -1) {
                ret = ais.read(buffer);
            }
            sequencer.stop();
            sequencer.setRealtime(true);

            RenderDialog.this.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Restore output process (mixer) from project and restart audio server

        project.getMixer().getMainBus().setOutputProcess(project.getOutputProcess());
        audioServer.setRealTime(true);
        project.getAudioServer().start();
    }
}
