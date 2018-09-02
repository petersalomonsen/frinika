/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://petersalomonsen.com) - Licensed under GNU LGPL
 */
package com.frinika.jvstsynth;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.spi.MidiDeviceProvider;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class FrinikaJVSTSynthProvider extends MidiDeviceProvider {

    static final class FrinikaJVSTSynthProviderInfo extends Info {

        public FrinikaJVSTSynthProviderInfo() {
            super("Frinika JVST Synth Provider", "petersalomonsen.com",
                    "Javasound mididevice provider for jvsthost", "0.1");
        }

    }
    static final Info[] deviceInfo = new Info[]{
        new FrinikaJVSTSynthProviderInfo()
    };

    @Override
    public Info[] getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public MidiDevice getDevice(Info info) {
        if (info.getClass().equals(FrinikaJVSTSynthProvider.FrinikaJVSTSynthProviderInfo.class)) {
            return new FrinikaJVSTSynth();
        }
        return null;
    }

    @Override
    public boolean isDeviceSupported(Info info) {
        if (info.getClass().equals(FrinikaJVSTSynthProvider.FrinikaJVSTSynthProviderInfo.class)) {
            return true;
        }
        return false;
    }

}
