/*
 * Copyright (c) 2004-2011 Peter Johan Salomonsen (http://www.petersalomonsen.com) - Licensed under GNU LGPL
 */
package com.frinika.jvstsynth;

import javax.sound.midi.ShortMessage;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteOrder;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class JVSTSynthMidiDeviceTest {

    @Test
    public void testOpenMidiDevice() throws Exception {
        FrinikaJVSTSynth synth = (FrinikaJVSTSynth) MidiSystem.getMidiDevice(new FrinikaJVSTSynthProvider.FrinikaJVSTSynthProviderInfo());
        final TargetDataLine line = (TargetDataLine) ((Mixer) synth).getLine(new Line.Info(TargetDataLine.class));
        AudioFormat.Encoding PCM_FLOAT = new AudioFormat.Encoding("PCM_FLOAT");
        AudioFormat format = new AudioFormat(PCM_FLOAT, 44100, 32, 2, 4 * 2, 44100, ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN));
        line.open(format);

        AudioInputStream ais = new AudioInputStream(line);
        assertTrue(AudioSystem.isConversionSupported(Encoding.PCM_SIGNED, ais.getFormat()));

        AudioInputStream convertedAis = AudioSystem.getAudioInputStream(Encoding.PCM_SIGNED, ais);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(convertedAis.getFormat());
        sdl.open();
        sdl.start();
        byte[] buf = new byte[16384];
        ShortMessage shm = new ShortMessage();
        shm.setMessage(ShortMessage.NOTE_ON, 1, 40, 127);
        synth.getReceiver().send(shm, -1);
        for (int n = 0; n < 20; n++) {
            int read = convertedAis.read(buf);
            sdl.write(buf, 0, read);
        }
    }
}
