/*
 * Created on May 13, 2016
 *
 * Copyright (c) 2005-2016 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.audio.dummy;

import java.util.Arrays;
import java.util.List;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.server.AbstractAudioServer;
import uk.org.toot.audio.server.AudioLine;
import uk.org.toot.audio.server.ExtendedAudioServer;
import uk.org.toot.audio.server.IOAudioProcess;

/**
 * Dummy audio server for simulation of audio servers.
 *
 * @author peter
 */
public class DummyAudioServer extends AbstractAudioServer implements ExtendedAudioServer {

    public DummyAudioServer() throws Exception {
    }

    @Override
    protected void startImpl() {
    }

    @Override
    protected void stopImpl() {
    }

    @Override
    public boolean isRunning() {
        return true;
    }

    @Override
    public List<String> getAvailableOutputNames() {
        return Arrays.asList(new String[]{"DummyServer"});
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
        return 44100;
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
        return "DummyServer";
    }

    public static void main(String[] args) throws Exception {
        new DummyAudioServer().startImpl();

        System.in.read();
    }
}
