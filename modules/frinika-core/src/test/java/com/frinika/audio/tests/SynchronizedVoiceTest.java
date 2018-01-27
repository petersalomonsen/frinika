package com.frinika.audio.tests;

import com.frinika.voiceserver.VoiceServer;
import com.frinika.voiceserver.voicetemplate.SynchronizedVoice;
import javax.swing.JFrame;
import junit.framework.TestCase;

/**
 * This test will verify the synchronization mechanisms of a SynchronizedVoice
 *
 * @author Peter Johan Salomonsen
 */
public class SynchronizedVoiceTest extends TestCase {

    VoiceServer voiceServer;

    /**
     * Set up the test
     */
    @Override
    protected void setUp() throws Exception {
        this.voiceServer = new VoiceServer() {

            @Override
            public void configureAudioOutput(JFrame frame) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public void testSynchronizedVoice() throws Exception {
        // Set up a synchronized voice
        SynchronizedVoice voice = new SynchronizedVoice(voiceServer, 0) {

            @Override
            public void fillBufferSynchronized(int startBufferPos, int endBufferPos, float[] buffer) {
                if (getMissedFrames() != 0) {
                    System.out.println("GLITCH! : FramePos " + getFramePos() + " Missed frames: " + getMissedFrames());
                } else {
                    System.out.println("Everything OK. FramePos " + getFramePos() + " Missed frames: " + getMissedFrames());
                }
            }
        };

        // Now add to the voiceServer
        voiceServer.addTransmitter(voice);

        // The start time - for framePos reference
        long startTime = System.currentTimeMillis();

        // Run the test for 20 seconds - notify every second
        for (int n = 0; n < 20; n++) {
            voice.setFramePos(((System.currentTimeMillis() - startTime) * voiceServer.getSampleRate()) / 1000);
            Thread.sleep(1000);
        }
    }
}
