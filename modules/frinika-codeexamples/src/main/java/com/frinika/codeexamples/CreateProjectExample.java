package com.frinika.codeexamples;

import com.frinika.main.FrinikaFrame;
import com.frinika.project.FrinikaProjectContainer;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.synth.SynthRack;
import com.frinika.synth.synths.Analogika;
import com.frinika.voiceserver.VoiceServer;
import javax.swing.JFrame;

/**
 * Example of creating a project programatically
 *
 * @author Peter Johan Salomonsen
 */
public class CreateProjectExample {

    public static void main(String[] args) throws Exception {
        // Create the project container
        FrinikaProjectContainer proj = new FrinikaProjectContainer();
        proj.getAudioServer().start();
        // Create a lane
        com.frinika.sequencer.model.MidiLane lane = proj.createMidiLane();

        // Create a MidiPart
        MidiPart part = new MidiPart(lane);

        // Add some notes
        part.add(new NoteEvent(part, 0, 60, 100, 0, 128));
        part.add(new NoteEvent(part, 128, 61, 100, 0, 128));
        part.add(new NoteEvent(part, 256, 62, 100, 0, 128));
        part.add(new NoteEvent(part, 512, 63, 100, 0, 128));
        part.add(new NoteEvent(part, 768, 64, 100, 0, 128));
        part.setBoundsFromEvents();

        //Create a second part on the same lane
        part = new MidiPart(lane);

        // Add some notes
        part.add(new NoteEvent(part, 1024 + 0, 60, 100, 0, 128));
        part.add(new NoteEvent(part, 1024 + 128, 59, 100, 0, 128));
        part.add(new NoteEvent(part, 1024 + 256, 58, 100, 0, 128));
        part.add(new NoteEvent(part, 1024 + 512, 57, 100, 0, 128));
        part.add(new NoteEvent(part, 1024 + 768, 56, 100, 0, 128));

        part.setBoundsFromEvents();

        // Initialize the Analogika softsynth on a SynthRack
        SynthRack synthRack = new SynthRack(null);

        //Register midi device in the sequencer
        SynthWrapper sw = new SynthWrapper(proj, synthRack);
        proj.addMidiOutDevice(sw);

        synthRack.setVoiceServer(new VoiceServer() {

            @Override
            public void configureAudioOutput(JFrame frame) {
                // TODO Auto-generated method stub

            }
        });
        synthRack.setSynth(0, new Analogika(synthRack));

        // Assign the midi device to track
        part.getTrack().setMidiDevice(sw);

        // Start playing sequence (Uncomment the following line)
        // proj.getSequencer().start();
        // Show the project frame (you can comment out this - if you only want to play)
        new FrinikaFrame().setProject(proj);
    }
}
