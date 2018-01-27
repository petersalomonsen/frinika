package com.frinika.codeexamples;

import com.frinika.base.FrinikaAudioServer;
import com.frinika.base.FrinikaAudioSystem;
import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.project.FrinikaProjectContainer;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.tools.MyMidiRenderer;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * Example of loading a Frinika project and playing it without opening the
 * Frinika gui.
 *
 * TODO: Threads should be able to stop without doing System.exit() (Check
 * BufferedRandomAccessFileManager)
 *
 * TODO: Test wav export with audio tracks
 *
 * TODO: How to obtain end tick in song
 *
 * @author Peter Johan Salomonsen
 */
public class StandaloneProjectWavExport {

    public static void main(String[] args) throws Exception {
        // Load a project
        FrinikaAudioSystem.usePhysicalAudioOutput = false;
        FrinikaProjectContainer project = FrinikaProjectContainer.loadProject(new File("/home/peter/mystudio/faro.frinika"), null);
        long startTick = 0;
        long endTick = 128 * 16;
        FrinikaAudioServer audioServer = (FrinikaAudioServer) project.getAudioServer();

        MyMidiRenderer midiRenderer = new MyMidiRenderer(project.getMixer(), project.getSequencer(), startTick, (int) (endTick - startTick), project.getAudioServer().getSampleRate());
        audioServer.setRealTime(false);
        long numberOfSamples = midiRenderer.available() / 4;

        byte[] buffer = new byte[1024];

        Type type = Type.WAVE;
        File outputFile = new File("/home/peter/mystudio/faro.wav");

        try {
            AudioInputStream ais = new AudioInputStream(midiRenderer, new AudioFormat((float) FrinikaGlobalProperties.getSampleRate(), 16, 2, true, true), numberOfSamples);
            FrinikaSequencer sequencer = project.getSequencer();

            sequencer.setRealtime(false);
            sequencer.start();
            System.out.println("Writing");
            AudioSystem.write(ais, type, outputFile);
            System.out.println("Done writing");
            sequencer.stop();
            project.close();
            System.out.println("Done with all");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
