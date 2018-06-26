/*
 * Copyright (c) 2004-2011 Peter Johan Salomonsen (http://www.petersalomonsen.com) - Licensed under GNU LGPL
 */
package com.frinika.jvstsynth;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class FindJVSTSynthMidiDeviceTest {

    @Test
    public void findJVSTSynthMidiDevice() throws Exception {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        boolean foundJVSTSynthMidiDevice = false;
        for (MidiDevice.Info info : infos) {
            if (info.getClass().equals(FrinikaJVSTSynthProvider.FrinikaJVSTSynthProviderInfo.class)) {
                foundJVSTSynthMidiDevice = true;
                FrinikaJVSTSynth synth = (FrinikaJVSTSynth) MidiSystem.getMidiDevice(info);

            }
        }
        assertTrue(foundJVSTSynthMidiDevice);
    }
}
