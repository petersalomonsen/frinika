/*
 * Created on Oct 4, 2015
 *
 * Copyright (c) 2004-2015 Peter Johan Salomonsen (www.petersalomonsen.com)
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
package com.frinika.audio.jnajack;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jaudiolibs.audioservers.AudioClient;
import org.jaudiolibs.audioservers.AudioConfiguration;
import org.jaudiolibs.audioservers.AudioServerProvider;
import org.jaudiolibs.audioservers.ext.ClientID;
import org.jaudiolibs.audioservers.ext.Connections;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.server.AbstractAudioServer;
import uk.org.toot.audio.server.AudioLine;
import uk.org.toot.audio.server.ExtendedAudioServer;
import uk.org.toot.audio.server.IOAudioProcess;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class JackTootAudioServer extends AbstractAudioServer implements AudioClient, ExtendedAudioServer {

    AudioBuffer audioOut;
    AudioConfiguration audioConfiguration;

    FrinikaJackAudioServer server;

    boolean running = false;
    Thread runner;

    public JackTootAudioServer() throws Exception {
        String lib = "JACK";

        AudioServerProvider provider = null;
        for (AudioServerProvider p : ServiceLoader.load(AudioServerProvider.class)) {
            if (lib.equals(p.getLibraryName())) {
                provider = p;
                break;
            }
        }
        if (provider == null) {
            throw new NullPointerException("No AudioServer found that matches : " + lib);
        }

        /* Create an instance of our client - see methods in the implementation 
         * below for more information.
         */
        AudioConfiguration config = new AudioConfiguration(
                44100.0f, //sample rate
                0, // input channels
                2, // output channels
                256, //buffer size
                // extensions
                new ClientID("Frinika"),
                Connections.OUTPUT);


        /* Use the AudioServerProvider to create an AudioServer for the client. 
         */
        server = (FrinikaJackAudioServer) provider.createServer(config, this);
        ((FrinikaJackAudioServer) server).init();

        audioConfiguration = server.getAudioContext();
        bufferFrames = audioConfiguration.getMaxBufferSize();

        System.out.println("Jack buffer frames = " + bufferFrames);

    }

    @Override
    protected void startImpl() {
        running = true;

        if (runner != null) {

            return;
        }

        /* Create a Thread to run our server. All servers require a Thread to run in.
         */
        runner = new Thread(new Runnable() {
            @Override
            public void run() {
                // The server's run method can throw an Exception so we need to wrap it
                try {
                    server.run();

                } catch (Exception ex) {
                    Logger.getLogger(JackTootAudioServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        // set the Thread priority as high as possible.
        runner.setPriority(Thread.MAX_PRIORITY);
        // and start processing audio - you'll have to kill the program manually!
        runner.start();

    }

    @Override
    protected void stopImpl() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public List<String> getAvailableOutputNames() {
        return Arrays.asList(new String[]{"Jack"});
    }

    @Override
    public List<String> getAvailableInputNames() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IOAudioProcess openAudioOutput(String name, String label) throws Exception {
        AudioLine line = new AudioLine() {

            @Override
            public ChannelFormat getChannelFormat() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public String getName() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void open() throws Exception {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int processAudio(AudioBuffer ab) {
                if (!ab.isRealTime()) {
                    return AudioProcess.AUDIO_OK;
                }

                audioOut = ab;

                return AudioProcess.AUDIO_OK;
            }

            @Override
            public void close() throws Exception {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public int getLatencyFrames() {
                return bufferFrames;
            }
        };
        return line;
    }

    @Override
    public IOAudioProcess openAudioInput(String name, String label) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeAudioOutput(IOAudioProcess output) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeAudioInput(IOAudioProcess input) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getSampleRate() {
        if (audioConfiguration == null) {
            return 44100f;
        } else {
            return audioConfiguration.getSampleRate();
        }
    }

    @Override
    public int getInputLatencyFrames() {
        return bufferFrames;
    }

    @Override
    public int getOutputLatencyFrames() {
        return bufferFrames;
    }

    @Override
    public int getTotalLatencyFrames() {
        return getInputLatencyFrames() + getOutputLatencyFrames();
    }

    @Override
    public void configure(AudioConfiguration ac) throws Exception {
        this.audioConfiguration = ac;
    }

    @Override
    public boolean process(long ts, List<FloatBuffer> inputs, List<FloatBuffer> outputs, int nframes) {

        work();
        final int length = nframes;
        final int channels = outputs.size();

        if (audioOut != null && audioOut.getSampleCount() == length) {
            final int sampleCount = audioOut.getSampleCount();

            for (int c = 0; c < channels; c++) {
                for (int i = 0; i < sampleCount; i++) {
                    outputs.get(c).put(audioOut.getChannel(c)[i]);
                }
            }
        } else {
            for (int c = 0; c < channels; c++) {
                for (int i = 0; i < nframes; i++) {
                    outputs.get(c).put(0);
                }
            }
        }
        return true;
    }

    @Override
    public void shutdown() {
        running = false;
    }

    @Override
    public int getSampleSizeInBits() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getBufferUnderRuns() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getLowestLatencyMilliseconds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getActualLatencyMilliseconds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLatencyMilliseconds(float latencyMilliseconds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getMinimumLatencyMilliseconds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getMaximumLatencyMilliseconds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getBufferMilliseconds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setBufferMilliseconds(float bufferMilliseconds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getLatencyMilliseconds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resetMetrics(boolean resetUnderruns) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<AudioLine> getOutputs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<AudioLine> getInputs() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getConfigKey() {
        return "Jack";
    }
}
