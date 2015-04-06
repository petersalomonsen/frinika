/*
 * Created on Feb 12, 2006
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.sequencer.converter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.sound.midi.MidiSystem;

import com.frinika.voiceserver.AudioContext;
import com.frinika.project.settings.ProjectSettings;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.synth.SynthRack;

/**
 * A test program for converting a Frinika single-track sequence into a multitrack sequence and playing it
 * @author Peter Johan Salomonsen
 */
public class MultiTrackPlayTest {

    public static void main(String[] args) throws Exception
    {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File("/home/peter/mystudio/20050421_no_vox_quant.frinika")));

        ProjectSettings project = (ProjectSettings)in.readObject();
        ByteArrayInputStream sequenceInputStream = new ByteArrayInputStream(project.getSequence());

        FrinikaSequencer sequencer = new FrinikaSequencer();
        sequencer.open();

        sequencer.setSequence(new FrinikaSequence(MidiSequenceConverter.splitChannelsToMultiTrack(MidiSystem.getSequence(sequenceInputStream))));

        AudioContext audioContext = new AudioContext();
        SynthRack synth = new SynthRack(audioContext.getVoiceServer());
        synth.open();
        sequencer.getTransmitter().setReceiver(synth.getReceiver());
        synth.loadSynthSetup(project.getSynthSettings());
        sequencer.start();
        while(true)
            Thread.sleep(1);
    }
}
