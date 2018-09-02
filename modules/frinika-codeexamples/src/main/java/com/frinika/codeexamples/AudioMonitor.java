package com.frinika.codeexamples;

import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.voiceserver.AudioContext;
import com.frinika.voiceserver.AudioInput;
import com.frinika.voiceserver.Voice;
import com.frinika.voiceserver.VoiceServer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;

/**
 * Simple program that will just capture your audio input and monitor it to the
 * output
 *
 * @author Peter Salomonsen
 *
 */
public class AudioMonitor {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        VoiceServer voiceServer = new AudioContext().getVoiceServer();

        voiceServer.configureAudioOutput(new JFrame());
        /**
         * JACK doesn't require any input line - provide your own line if not
         * using jack
         */
        final AudioInput input = new AudioInput(AudioSystem.getTargetDataLine(new AudioFormat((float) FrinikaGlobalProperties.getSampleRate(), 16, 2, true, true)), FrinikaGlobalProperties.getSampleRate());

        input.start();
        input.getLine().start();

        voiceServer.addTransmitter(new Voice() {
            byte[] inBuffer = null;

            @Override
            public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
                if (inBuffer == null || inBuffer.length != buffer.length * 2) {
                    inBuffer = new byte[buffer.length * 2];
                }

                int numOfBytes = (endBufferPos - startBufferPos) * 2;

                input.getLine().read(inBuffer, 0, numOfBytes);

                TargetDataLine line = input.getLine();

                int n = 0;
                for (int i = startBufferPos; i < endBufferPos; i++) {
                    short sample = (short) ((0xff & inBuffer[n + 1]) + ((0xff & inBuffer[n + 0]) * 256));
                    buffer[i] = sample / 32768f;
                    n += 2;
                }
            }
        });
        // otherwise we terminate
        Thread.sleep(100000);
    }
}
