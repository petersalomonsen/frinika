/*
 * Created on Jan 30, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

import com.frinika.sequencer.gui.mixer.MidiDeviceMixerPanel;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.synth.envelope.MidiVolume;
import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.Voice;
import com.frinika.voiceserver.VoiceInterrupt;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.JFrame;

/**
 * Create a custom softsynth mididevice and attach it to the mididevice mixer
 *
 * @author Peter Johan Salomonsen
 */
public class SoftMidiMixerTest {

    /**
     * @param args
     * @throws Exceptio{};n
     */
    public static void main(String[] args) throws Exception {
        new AudioContext();
        SynthWrapper sw = new SynthWrapper(null, new MidiDevice() {

            double level = MidiVolume.midiVolumeToAmplitudeRatio(100);
            Voice voice;

            {
                voice = new Voice() {
                    double degree = 0.0;

                    @Override
                    public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
                        //System.out.println(startBufferPos+" "+endBufferPos+" "+level);
                        for (int n = startBufferPos; n < endBufferPos; n += 2) {
                            float val = (float) ((Math.sin((degree += ((440.0 * Math.PI * 2) / 44100)) % (Math.PI * 2)) * level) / 2.0);
                            buffer[n] = val;
                            buffer[n + 1] = val;
                        }

                    }
                };
                AudioContext.getDefaultAudioContext().getVoiceServer().addTransmitter(voice);
            }

            @Override
            public void close() {
                // TODO Auto-generated method stub

            }

            @Override
            public Info getDeviceInfo() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getMaxReceivers() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public int getMaxTransmitters() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getMicrosecondPosition() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public Receiver getReceiver() throws MidiUnavailableException {
                // TODO Auto-generated method stub
                return new Receiver() {

                    @Override
                    public void close() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void send(MidiMessage message, long timeStamp) {
                        if (message instanceof ShortMessage) {
                            final ShortMessage shm = (ShortMessage) message;
                            if (shm.getCommand() == ShortMessage.CONTROL_CHANGE && shm.getData1() == 7) {
                                AudioContext.getDefaultAudioContext().getVoiceServer().interruptTransmitter(voice, new VoiceInterrupt() {

                                    @Override
                                    public void doInterrupt() {
                                        level = MidiVolume.midiVolumeToAmplitudeRatio(shm.getData2());
                                    }
                                });
                            }
                        }

                    }
                };
            }

            @Override
            public List<Receiver> getReceivers() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Transmitter getTransmitter() throws MidiUnavailableException {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public List<Transmitter> getTransmitters() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isOpen() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void open() throws MidiUnavailableException {
                // TODO Auto-generated method stub

            }
        });
        JFrame frame = new JFrame();
        frame.add(new MidiDeviceMixerPanel(null, sw));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        frame.setVisible(true);
    }
}
