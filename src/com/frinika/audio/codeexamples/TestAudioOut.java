
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
package com.frinika.audio.codeexamples;

import com.frinika.toot.javasoundmultiplexed.MultiplexedJavaSoundAudioServer;
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
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.server.AudioServer;
import uk.org.toot.swingui.audioui.serverui.AudioServerChooser;

/**
 *
 * @author pjl
 */
public class TestAudioOut {

    private static AudioServer audioServer;
    private static JFrame frame;
    private static AudioBuffer chunk;
    private static AudioServerConfiguration serverConfig;

    public static void main(String args[]) throws Exception {


        String serverName = AudioServerChooser.showDialog("server");


        //   Class<AudioServer> clazz=new Class<AudioServer>();
        // AudioServerServices.printServiceDescriptors(null);

        audioServer = AudioServerServices.createServer(serverName); // new MultiIOJavaSoundAudioServer();

        if (audioServer instanceof MultiplexedJavaSoundAudioServer) {
            MultiplexedJavaSoundAudioServer s = (MultiplexedJavaSoundAudioServer) audioServer;


            List<String> list = s.getOutDeviceList();
            Object a[] = new Object[list.size()];
            a = list.toArray(a);
            Object selectedValue = JOptionPane.showInputDialog(null,
                    "device", "output",
                    JOptionPane.INFORMATION_MESSAGE, null, a, a[0]);
            //	System.out.println("|" + configDev + "|" + selectedValue + "|");
            s.setOutDevice((String) selectedValue);
        }


        serverConfig = AudioServerServices.createServerConfiguration(audioServer);



        List<String> list = audioServer.getAvailableOutputNames();
        Object a[] = new Object[list.size()];
        a = list.toArray(a);

        frame = new JFrame();

        Object selectedValue = JOptionPane.showInputDialog(frame,
                "audio_output", "OUTPUT", JOptionPane.INFORMATION_MESSAGE,
                null, a, a[0]);

        final IOAudioProcess output = audioServer.openAudioOutput((String) selectedValue,
                "output");


        chunk = audioServer.createAudioBuffer("default");
        chunk.setRealTime(true);
        final Synth synth = new Synth();

        audioServer.setClient(new AudioClient() {

            public void work(int arg0) {


                synth.processAudio(chunk);

                output.processAudio(chunk);

            }

            public void setEnabled(boolean arg0) {
                //  throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        audioServer.start();
        configure();


    }

    static class Synth implements AudioProcess {

        long count = 0;
        private double freq = 440;

        public void open() throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public int processAudio(AudioBuffer buffer) {
            int n = buffer.getSampleCount();
            int nchan = buffer.getChannelCount();
            float Fs = buffer.getSampleRate();

            for (int i = 0; i < n; i++) {
                float val = (float) Math.sin((2 * Math.PI * freq * i) / Fs);
                for (int chan = 0; chan < nchan; chan++) {
                    buffer.getChannel(chan)[i] = val;
                }
            }
            return AUDIO_OK;
        }

        public void close() throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
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


        frame.setAlwaysOnTop(true);
        frame.setContentPane(content);
        frame.pack();
        frame.setVisible(true);
    }
}