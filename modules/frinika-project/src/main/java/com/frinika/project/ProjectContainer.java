/*
 * Created on Mar 6, 2006
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
package com.frinika.project;

import com.frinika.audio.toot.AudioInjector;
import com.frinika.base.FrinikaAudioServer;
import com.frinika.base.FrinikaAudioSystem;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.project.scripting.FrinikaScriptingEngine;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.TempoChangeListener;
import com.frinika.sequencer.model.ProjectLane;
import com.frinika.sequencer.model.tempo.TempoList;
import com.frinika.sequencer.model.timesignature.TimeSignatureList;
import com.frinika.sequencer.project.SequencerProjectSerializer;
import com.frinika.synth.settings.SynthSettings;
import com.frinika.tootX.midi.MidiRouterSerialization;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import uk.org.toot.audio.core.AudioControlsChain;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.Taps;
import uk.org.toot.audio.mixer.AudioMixer;
import uk.org.toot.audio.mixer.AudioMixerStrip;
import uk.org.toot.audio.mixer.MixControls;
import uk.org.toot.audio.mixer.MixerControls;
import uk.org.toot.audio.mixer.MixerControlsFactory;
import uk.org.toot.audio.mixer.MixerControlsIds;
import uk.org.toot.misc.Tempo;

/**
 * Use to load Frinika projects.
 *
 * This class links together all components of a Frinika project, and provides
 * all operations and features - including a Frinika sequencer instance.
 *
 * Information about Midi Devices - naming and how to reopen them is contained
 * using the MidiDeviceDescriptors.
 *
 * Audio files are stored in a folder named audio which is created in the same
 * folder as where the project is. Thus a good convention is to have one folder
 * per project.
 *
 * This class is now used only for serialization to preserve compatibility with
 * previous version.
 *
 * @author Peter Johan Salomonsen
 */
public class ProjectContainer implements Serializable, SequencerProjectSerializer {

    private static final long serialVersionUID = 1L;

    public TootMixerSerializer mixerSerializer;
    public ProjectLane projectLane;
    public String title = null;
    public File projectFile = null;
    public File audioDir = null;
    public float tempo = 100;
    public FrinikaScriptingEngine scriptingEngine; // Jens

    /**
     * Keep for backwards compatibility. Replacement is now the
     * midiDeviceDescriptors.
     */
    @Deprecated
    public List<SynthSettings> synthSettings;

    /**
     * Keep for backwards compatibility. Replacement is now the
     * midiDeviceDescriptors.
     */
    @Deprecated
    public Vector<String> externalMidiDevices;

    /**
     * Information about the midi devices used in this project
     */
    List<MidiDeviceDescriptor> midiDeviceDescriptors = new ArrayList<>();

    TimeSignatureList timeSignitureList;

    /**
     * The resolution of the sequence. If you import a midi track they may have
     * a different resolution - and when saving to a project - the sequence
     * created when reloading will have the resolution stored here.
     */
    public int ticksPerQuarterNote = FrinikaGlobalProperties.TICKS_PER_QUARTER.getValue();
    public TempoList tempoList;
    public double pianoRollSnapQuantization = 0;
    public double partViewSnapQuantization = 0;
    public boolean isPianoRollSnapQuantized = true;
    public boolean isPartViewSnapQuantized = true;
    /**
     * Whether to embed externally referenced data or not (e.g. soundfonts)
     */
    public boolean saveReferencedData;
    public long endTick = 100000;    // static boolean dynam = true;
    public long loopStartPoint;
    public long loopEndPoint;
    public int loopCount;
    public MidiRouterSerialization midiRouterSerialization;
    public String genres;
    public Long dataBaseID;

    public transient AudioMixer mixer;
    public transient int count = 1;
    public transient MixerControls mixerControls;
    public transient FrinikaAudioServer audioServer;
    public transient FrinikaSequence sequence = null;
    public transient FrinikaSequencer sequencer;

    public ProjectContainer() {
        mixerSerializer = new TootMixerSerializer(this);
        sequencer = new FrinikaSequencer();
        try {
            sequencer.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public FrinikaSequence getSequence() {
        return sequence;
    }

    @Override
    public FrinikaSequencer getSequencer() {
        return sequencer;
    }

    /**
     * Creates a sequence based on the resolution defined in
     * ticksPerQuarterNote.
     */
    @Override
    public void createSequence() {
        if (sequence == null) {
            try {
                if (ticksPerQuarterNote == 0) {
                    ticksPerQuarterNote = FrinikaGlobalProperties.TICKS_PER_QUARTER.getValue();
                }
                sequence = new FrinikaSequence(Sequence.PPQ, ticksPerQuarterNote, 1);
                sequencer.setSequence(sequence);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException, Exception {
        scriptingEngine = new FrinikaScriptingEngine(); // Jens
        // defaults to having one main bus
        // create 2 aux send busses for effects and 1 aux monitor bus
        // because if you don't have any aux busses you can't insert
        // effects :(
        mixerSerializer = new TootMixerSerializer(this);
        sequencer = new FrinikaSequencer();
        try {
            sequencer.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        attachTootNotifications();

        mixerControls = new MixerControls("Mixer");
        MixerControlsFactory.createBusses(mixerControls, 2, 1);
        MixerControlsFactory.createBusStrips(mixerControls);
        audioServer = FrinikaAudioSystem.getAudioServer();
        try {
            mixer = new AudioMixer(mixerControls, audioServer);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.err.println(" \n Sorry but I do not want to go on without an audio output device. \n Bye bye . . .  ");
            System.exit(1);
        }

        String outDev = FrinikaAudioSystem.configureServerOutput();
        if (outDev != null) {
            AudioInjector outputProcess = new AudioInjector(audioServer.openAudioOutput(outDev, "output"));
            System.out.println("Using " + outDev + " as audio out device");
            mixer.getMainBus().setOutputProcess(outputProcess);
        } else {
            // message(" No output devices found ");
        }

        in.defaultReadObject();
    }

    @Override
    public MixControls addMixerInput(AudioProcess audioProcess, String stripName) {

        MixControls mixControls = null;
        try {
            AudioMixerStrip strip = mixer.getStrip(stripName);
            AudioControlsChain controls;

            // If it exists we have loaded a mixer with the strip so no need to
            // create it
            if (strip == null) {
                controls = mixerControls.createStripControls(
                        MixerControlsIds.CHANNEL_STRIP, count++, stripName);
                strip = mixer.getStrip(stripName);
            } else {
                controls = mixerControls.getStripControls(stripName);
            }
            strip.setInputProcess(audioProcess);
            mixControls = (MixControls) controls.find(mixerControls.getMainBusControls().getName());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return mixControls;
    }

    private void attachTootNotifications() {
        Taps.setAudioServer(audioServer);
        sequencer.addTempoChangeListener(new TempoChangeListener() {
            @Override
            public void notifyTempoChange(float bpm) {
                Tempo.setTempo(bpm);
            }
        });
    }
}
