/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://petersalomonsen.com) - Licensed under GNU LGPL
 */

package com.frinika.codesynth;


import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.spi.MidiDeviceProvider;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class CodeSynthProvider extends MidiDeviceProvider {

    static final class CodeSynthProviderInfo extends Info {

        public CodeSynthProviderInfo() {
            super("CodeSynth","frinika.com",
                    "Javasound mididevice provider for CodeSynth","0.1");
        }

    }
    static final Info[] deviceInfo = new Info[] {
        new CodeSynthProviderInfo()
    };
    
    @Override
    public Info[] getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public MidiDevice getDevice(Info info) {
        if(info.getClass().equals(CodeSynthProvider.CodeSynthProviderInfo.class))
        {
            return new CodeSynth();
        }
        return null;
    }

    @Override
    public boolean isDeviceSupported(Info info) {
        if(info.getClass().equals(CodeSynthProvider.CodeSynthProviderInfo.class))
        {
            return true;
        }
        return false;
    }




}
