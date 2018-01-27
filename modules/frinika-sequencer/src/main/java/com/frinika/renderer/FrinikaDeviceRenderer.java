/*
 * Created on 5.3.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import javax.sound.midi.MidiDevice;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

public class FrinikaDeviceRenderer {

    MidiDevice dev;
    FrinikaRenderer renderer;
    FrinikaChannelRenderer[] channels = new FrinikaChannelRenderer[16];

    public FrinikaDeviceRenderer(FrinikaRenderer renderer, MidiDevice dev) {
        this.renderer = renderer;
        this.dev = dev;
    }

    public void addTrack(FrinikaTrackWrapper track) {
        int ch = track.getMidiChannel();
        if (channels[ch] == null) {
            channels[ch] = new FrinikaChannelRenderer(this, ch);
        }
        channels[ch].addTrack(track);
    }

    public MidiDevice getDevice() {
        return dev;
    }

    public void beforeStart() {
        for (FrinikaChannelRenderer channel : channels) {
            if (channel != null) {
                channel.beforeStart();
            }
        }
        for (FrinikaChannelRenderer channel : channels) {
            if (channel != null) {
                channel.beforeStart2();
            }
        }

    }

    AudioProcess renderProcess = new AudioProcess() {
        @Override
        public void open() {
        }

        @Override
        public int processAudio(AudioBuffer buffer) {

            int samplecount = buffer.getSampleCount();

            for (FrinikaChannelRenderer channel : channels) {
                if (channel != null) {
                    channel.processAudio(buffer);
                }
            }

            return 0;
        }

        @Override
        public void close() {
        }
    };

    public void start() {
        if (dev instanceof SynthWrapper) {
            ((SynthWrapper) dev).setRenderAudioProcess(renderProcess);
        }
    }

    public void stop() {
        if (dev instanceof SynthWrapper) {
            ((SynthWrapper) dev).setRenderAudioProcess(null);
        }

        for (FrinikaChannelRenderer channel : channels) {
            if (channel != null) {
                channel.stop();
            }
        }
    }
}
