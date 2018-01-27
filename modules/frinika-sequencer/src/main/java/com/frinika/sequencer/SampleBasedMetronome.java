/**
 * Metronome that counts samples rendered to make the click. --pjl   (original code by PJS)
 */
package com.frinika.sequencer;

import com.frinika.sequencer.project.SequencerProjectContainer;
import com.frinika.synth.envelope.MidiVolume;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.server.AudioServer;

public class SampleBasedMetronome implements AudioProcess, SequencerListener {
    // MyTracker tracker;

    float[] sampleData;

    float level = 0f;

    boolean active = false;

    SequencerProjectContainer project;

    int metSamplePos = 0;

    long framePtr = 0;

    private int doClick = 0;

    public SampleBasedMetronome(SequencerProjectContainer project) throws Exception {
//		super(project.getAudioServer(), getFramePos(project.getSequencer(), project.getAudioServer(),
//				0));
        // super(project.getAudioServer().openAudioOutput(project.getAudioServer().getAvailableOutputNames().get(0),null));
        this.project = project;

        final FrinikaSequencer sequencer = project.getSequencer();
        final AudioServer audioServer = project.getAudioServer();
        // this.tracker = tracker;

        sequencer.addSequencerListener(this);
        try {
            AudioInputStream stream = AudioSystem
                    .getAudioInputStream(ClassLoader
                            .getSystemResource("sounds/metronome1.wav"));
            sampleData = new float[(int) stream.getFrameLength()];

            int index = 0;

            byte[] frame = new byte[2];
            int b = stream.read(frame);
            while (b != -1) {
                sampleData[index++] = (((frame[1] * 256) + (frame[0] & 0xff)) / 32768f);
                b = stream.read(frame);
            }
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();

        }

        // project.getMixer().getMainBus().setOutputProcess(this);
    }

    public void dispose() {
        project.getSequencer().removeSequencerListener(this);
    }

    public void setVelocity(int velocity) {
        if (velocity > 0) {

            active = true;
            level = MidiVolume.midiVolumeToAmplitudeRatio(velocity);
            doClick = 4;
        } else {
            active = false;
        }
    }

    long nextClick() {

        // double ticksPerBeat=project.getSequence().getResolution();
        double bpSec = project.getSequencer().getTempoInBPM() / 60.0;    // uS/beat
        double fs = project.getAudioServer().getSampleRate();   // /s 

        double samplesPerClick = fs / bpSec;

        long nClick = ((framePtr + (long) samplesPerClick - 1) / (long) samplesPerClick);
        return (long) (nClick * samplesPerClick);
    }

    @Override
    public int processAudio(AudioBuffer buffer) {

        if (!active) {
            return AUDIO_OK;
        }

        if (!(doClick > 0 || project.getSequencer().isRunning())) {
            return AUDIO_OK;
        }

        int size = buffer.getSampleCount();

        int start = 0;
        long nextClick = nextClick();
//		System. out.println(framePtr + " " + nextClick);
        if (metSamplePos >= sampleData.length) {
            doClick--;
            if (framePtr + size < nextClick) {
                framePtr += size;
                return AUDIO_OK;
            }
            start = (int) (nextClick - framePtr);
            metSamplePos = 0;
        }

        float left[] = buffer.getChannel(0);
        float right[] = buffer.getChannel(1);

        framePtr += start;
        for (int n = start; (n < size)
                && (metSamplePos < sampleData.length); n++, framePtr++) {
            if (framePtr == nextClick) {
                metSamplePos = 0;
            }
            left[n] += sampleData[metSamplePos] * level;
            right[n] += sampleData[metSamplePos++] * level;
        }

        return AUDIO_OK; // super.processAudio(buffer);
    }

    @Override
    public void open() {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    long getFramePos() {
        FrinikaSequencer sequencer = project.getSequencer();
        AudioServer audioServer = project.getAudioServer();

        return (long) (((sequencer.getMicrosecondPosition()) * audioServer
                .getSampleRate()) / 1000000);
    }

    @Override
    public void beforeStart() {
        framePtr = getFramePos();
    }

    @Override
    public void start() {
        //	active=true;
    }

    @Override
    public void stop() {
        //	active=false;
    }
}
