
/*
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
package com.frinika.codeexamples;

import com.frinika.audio.toot.AudioPeakMonitor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.server.AudioClient;
import uk.org.toot.audio.server.AudioServerConfiguration;
import uk.org.toot.audio.server.AudioServerServices;
import uk.org.toot.audio.server.IOAudioProcess;
import uk.org.toot.audio.server.MultiIOJavaSoundAudioServer;
import uk.org.toot.swingui.audioui.serverui.AudioServerUIServices;
import javax.swing.Timer;
import com.frinika.audio.analysis.CycliclyBufferedAudio;
import com.frinika.audio.analysis.gui.CyclicBufferFFTAnalysisPanel;
import com.frinika.global.FrinikaConfig;

/**
 *
 * @author pjl
 */
public class AudioInOut {

    private static MultiIOJavaSoundAudioServer audioServer;
    private static JFrame frame;
    private static AudioBuffer chunk;
    private static AudioServerConfiguration serverConfig;
    private static AudioPeakMonitor peakIn;
    private static MeterPanel meterPanel;
    private static CycliclyBufferedAudio buffer;
    private static int cacheSize=100000;
    private static CyclicBufferFFTAnalysisPanel fftpanel;

    public static void main(String args[]) throws Exception {

        buffer=new CycliclyBufferedAudio(cacheSize,FrinikaConfig.sampleRate);
        fftpanel=new CyclicBufferFFTAnalysisPanel(buffer);

        audioServer = new MultiIOJavaSoundAudioServer();

        serverConfig = AudioServerServices.createServerConfiguration(audioServer);

        peakIn = new AudioPeakMonitor();

        List<String> list = audioServer.getAvailableOutputNames();
        Object a[] = new Object[list.size()];
        a = list.toArray(a);

        frame = new JFrame();
        Object selectedValue = JOptionPane.showInputDialog(frame,
                "audio_output", "OUTPUT", JOptionPane.INFORMATION_MESSAGE,
                null, a, a[0]);

        final IOAudioProcess output = audioServer.openAudioOutput((String) selectedValue,
                "output");

        list = audioServer.getAvailableInputNames();
        a = new Object[list.size()];
        a = list.toArray(a);

        selectedValue = JOptionPane.showInputDialog(frame,
                "audio_input", "INPUT", JOptionPane.INFORMATION_MESSAGE,
                null, a, a[0]);

        final IOAudioProcess input = audioServer.openAudioInput((String) selectedValue,
                "output");

        chunk = audioServer.createAudioBuffer("default");
        chunk.setRealTime(true);

        audioServer.setClient(new AudioClient() {

            public void work(int arg0) {
                chunk.makeSilence();
                input.processAudio(chunk);
                peakIn.processAudio(chunk);
                output.processAudio(chunk);
                buffer.in.processAudio(chunk);
            }

            public void setEnabled(boolean arg0) {
                //  throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        audioServer.start();
        configure();

        Timer timer = new Timer(50, new ActionListener() {

            public void actionPerformed(ActionEvent ae) {

                updateMeters();
            }
        });
        timer.start();



    }

    private static void updateMeters() {
        double val = peakIn.getPeak();
        if (val > .99) {
            meterPanel.updateMeter(val, Color.RED);
        } else {
            meterPanel.updateMeter(val, Color.GREEN);
        }

    }

    public static void configure() {



        final JComponent ui = AudioServerUIServices.createServerUI(audioServer,
                serverConfig);

        if (ui == null) {
            return; // no server ui
        }

        frame = new JFrame();
        JPanel content = new JPanel();
        content.add(ui);

        meterPanel = new MeterPanel();
        content.add(meterPanel);
        content.add(fftpanel);
        frame.setAlwaysOnTop(true);
        frame.setContentPane(content);
        frame.pack();
        frame.setVisible(true);
    }
}


class MeterPanel extends JPanel {

    double val = 0.0;
    Color color = null;
    int redcount = 0;

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(10, 100);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(10, 100);
    }

    void updateMeter(double val, Color col) {
        this.val = val;
        if (color == null || col == Color.RED) {
            color = col;
        }
        repaint();

    }

    @Override
    public void paintComponent(Graphics g) {

        int w = getWidth();
        int h = getHeight();

        if (val <= 0.0) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, w, h);
        } else {
            int h2 = (int) ((1.0 - val) * h);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, w, h2);

            if ((redcount + 1) % 4 != 0) {
                g.setColor(color);
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillRect(0, h2, w, h);
        }
        if (color == Color.RED) {
            redcount++;
            if (redcount > 20) {
                color = null;
                redcount = 0;
            }
        } else {
            color = null;
        }
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, w - 1, h - 1);
    }
}
