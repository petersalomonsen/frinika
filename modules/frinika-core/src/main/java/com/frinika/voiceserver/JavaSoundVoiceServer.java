/*
 * Created on Sep 11, 2004
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
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.voiceserver;

import com.frinika.global.property.FrinikaGlobalProperties;
import com.frinika.localization.CurrentLocale;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * An integration of the Voice server with the sound hardware interfaces of the
 * java sound api.
 *
 * Brief description of the latency schemes
 *
 * Frinika latency schemes is more or less all about how to pause after data is
 * processed and written to the audio output. A typical (buffer) cycle is this:
 *
 * 1. Process audio data 2. Write data to audioOut 3. Pause until time for
 * processing new buffer The pause time is the total buffer time (represented by
 * number of samples in the buffer) minus the time used in step 1 and 2.
 *
 * It's very important that the time used for all these three steps is as
 * constant as possible, and that the total time equals the time represented by
 * a buffer.
 *
 * To obtain this constant time different "blocking" schemes are implemented, as
 * alternative to the blocking provided by sourceDataLine.write. What scheme to
 * use depends on your system, try yourself and choose the scheme that is most
 * stable. My experience is that the sdl.write blocking sometimes blocks too
 * long, resulting in glitches. The two main alternatives in Frinika makes sure
 * that sd.write doesn't block at all (by setting a very large buffer size), and
 * then takes care of the blocking manually.
 *
 * - Standard latency cheme (no checkboxes checked in the audio device conf -
 * this is the scheme that works best on most systems) - Blocking is done using
 * Thread.sleepNanos
 *
 * - "UltraLowLatency" - Blocking is done using a loop and Thread.yield() while
 * measuring sdl.getLongFramePosition(), very CPU intensive, but very accurate
 * on when it stops blocking
 *
 * - "UltraLowLatency" with "Frinika estimated framepos" - Same as above but
 * instead of using sdl.getLongFramePosition(), Frinika calculates an ideal
 * Frame position by measuring System.nanoTime()
 *
 * @author Peter Johan Salomonsen
 */
public class JavaSoundVoiceServer extends VoiceServer implements Runnable {

    AudioFormat format = new AudioFormat(getSampleRate(), 16, 2, true, true);

    SourceDataLine lineOut = null;

    boolean isRunning = false;
    boolean hasStopped = false;

    // 512 frames by default
    int bufferSize = 2048;

    public CPUMeter cpuMeter = new CPUMeter(this);

    /**
     * Ultra low latency mode can be used for small buffer sizes to obtain
     * better latency - BUT it eats all your CPU.
     */
    protected boolean ultraLowLatency = false;

    /**
     * Use Java standard way of latency control (blocking on sdl.write)
     */
    protected boolean standardLatency = false;

    /**
     * Use Frinika estimated frame pos (using System.nanoTime) or
     * SDL.getLongFramePosition
     */
    private boolean useEstimatedFramePos = true;

    /**
     * Max audio thread priority
     *
     */
    private boolean maxPriority = true;

    public JavaSoundVoiceServer() {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            System.out.println("Detected Mac OS X. Automatically tuning audio device settings. ");
            // These are the settings working best on a G5 iMac 2Ghz
            useEstimatedFramePos = false;
            ultraLowLatency = true;
        }

        bufferSize = 4 * FrinikaGlobalProperties.AUDIO_BUFFER_LENGTH.getValue();

        startAudioOutput();
    }

    public void startAudioOutput() {
        try {

            if (lineOut == null) {
                lineOut = (SourceDataLine) AudioSystem.getSourceDataLine(format);
            }

            if (standardLatency) {
                lineOut.open(format, bufferSize);
            } else {
                lineOut.open(format);
            }

            lineOut.start();
            System.out.println("Buffersize: " + bufferSize + " / " + lineOut.getBufferSize());
        } catch (LineUnavailableException e) {
            lineOut = null;
            System.out.println("No audio output available. Use Audio Devices dialog to reconfigure.");
        }

        Thread thread = new Thread(this);
        if (maxPriority) {
            thread.setPriority(Thread.MAX_PRIORITY);
        }
        thread.start();
    }

    public void stopAudioOutput() throws Exception {
        isRunning = false;
        while (!hasStopped) {
            Thread.yield();
        }
        hasStopped = false;
        if (lineOut != null) {
            //lineOut.drain(); // Seems to hang on some windows systems
            lineOut.stop();
            lineOut.close();
        }
    }

    @Override
    public void run() {
        try {
            isRunning = true;
            byte[] outBuffer = new byte[bufferSize];
            float[] floatBuffer = new float[bufferSize / 2];

            long totalTimeNanos = (long) (((float) (bufferSize / 4f) / (float) getSampleRate()) * 1000000000);
            // nanoTime when buffer expires
            long expireNanos = 0;
            long framesWritten = 0;

            while (isRunning) {
                long startTimeNanos = System.nanoTime();
                for (int n = 0; n < (floatBuffer.length); n++) {
                    floatBuffer[n] = 0;
                }

                read(outBuffer, floatBuffer);
                long endTimeNanos = System.nanoTime();

                if (lineOut != null) {
                    lineOut.write(outBuffer, 0, outBuffer.length);
                }

                /**
                 * If we use standard latency model, the write above will do the
                 * blocking. Else we'll have to do the blocking manually
                 * below....
                 */
                if (!standardLatency) {
                    if (expireNanos < System.nanoTime()) {
                        expireNanos = System.nanoTime() + totalTimeNanos;
                    } else {
                        expireNanos += totalTimeNanos;
                    }

                    if (ultraLowLatency) {
                        /**
                         * Ultra low latency mode can be used for small buffer
                         * sizes to obtain better latency - BUT it eats all your
                         * CPU. Be careful.
                         */
                        if (useEstimatedFramePos) {
                            //Frinika Estimated frame position
                            while (expireNanos - System.nanoTime() > totalTimeNanos) {
                                Thread.yield(); // Keeps your CPU as busy as possible
                            }
                        } else {
                            //Use lineOut frameposition
                            while (lineOut.getLongFramePosition() < framesWritten) {
                                Thread.yield();
                            }
                        }

                    } else {
                        long sleepNanos = expireNanos - totalTimeNanos - System.nanoTime();
                        if (sleepNanos > 0) {
                            Thread.sleep(sleepNanos / 1000000, (int) (sleepNanos % 1000000));
                        }
                    }
                }
                // This is used when not using the Frinika estimated position
                framesWritten += (outBuffer.length / 4);

                cpuMeter.setCpuPercent((int) (((float) (endTimeNanos - startTimeNanos) / (float) totalTimeNanos) * 100));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        hasStopped = true;
    }

    @Override
    public void configureAudioOutput(JFrame frame) {
        JDialog dialog = new JDialog(frame, "Audio devices");
        dialog.setLayout(new GridLayout(9, 1));

        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        List<Mixer.Info> validMixerInfos = new ArrayList<>();

        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if (mixer.isLineSupported(new Line.Info(SourceDataLine.class))) {
                validMixerInfos.add(mixerInfo);
            }
        }

        final JComboBox cb = new JComboBox(validMixerInfos.toArray());
        dialog.add(cb);

        dialog.add(new JLabel("Slide to adjust latency:"));
        final JSlider sl = new JSlider(64, 8192);
        sl.setToolTipText("Slide to adjust latency");
        dialog.add(sl);

        final JLabel lb = new JLabel();
        dialog.add(lb);

        class LatencyListener implements ChangeListener {

            int bufferSize = 0;

            @Override
            public void stateChanged(ChangeEvent e) {
                bufferSize = ((int) (sl.getValue() / 64)) * 64;

                lb.setText("Latency = " + bufferSize + " frames");
            }
        }

        final LatencyListener latencyListener = new LatencyListener();
        sl.addChangeListener(latencyListener);
        sl.setValue(bufferSize / 4);

        final JCheckBox ultraLowLatencyCheckBox = new JCheckBox("Ultra-low latency support (CAREFUL - eats all CPU it can get)", ultraLowLatency);
        final JCheckBox useEstimatedFramePosCheckBox = new JCheckBox("Use Frinika estimated framepos", useEstimatedFramePos);

        ultraLowLatencyCheckBox.setToolTipText("If you set a low latency (typical below 1024) above, you might need to turn this on as well. ");
        ultraLowLatencyCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                ultraLowLatency = ultraLowLatencyCheckBox.isSelected();
                useEstimatedFramePosCheckBox.setEnabled(!standardLatency & ultraLowLatency);
            }
        });
        ultraLowLatencyCheckBox.setEnabled(!standardLatency);
        dialog.add(ultraLowLatencyCheckBox);

        useEstimatedFramePosCheckBox.setToolTipText("On some systems Frinika does a better estimation of the audio position, on others not... ");
        useEstimatedFramePosCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                useEstimatedFramePos = useEstimatedFramePosCheckBox.isSelected();
            }
        });
        useEstimatedFramePosCheckBox.setEnabled(!standardLatency & ultraLowLatency);
        dialog.add(useEstimatedFramePosCheckBox);

        final JCheckBox standardLatencyCheckBox = new JCheckBox("Use standard javasound latency control", standardLatency);
        standardLatencyCheckBox.setToolTipText("This is the standard javasound method for latency control, may be good on some systems - but not on others.. ");
        standardLatencyCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                standardLatency = standardLatencyCheckBox.isSelected();
                // Cannot use ultra low latency if using standard latency
                useEstimatedFramePosCheckBox.setEnabled(!standardLatency & ultraLowLatency);
                ultraLowLatencyCheckBox.setEnabled(!standardLatency);
            }
        });
        dialog.add(standardLatencyCheckBox);

        final JCheckBox maxPriorityCheckBox = new JCheckBox(CurrentLocale.getMessage("voiceserver.javasound.max_priority"), maxPriority);
        maxPriorityCheckBox.setToolTipText(CurrentLocale.getMessage("voiceserver.javasound.max_priority.tooltip"));
        maxPriorityCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent evt) {
                maxPriority = maxPriorityCheckBox.isSelected();

            }
        });
        dialog.add(maxPriorityCheckBox);

        final JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    stopAudioOutput();
                    JavaSoundVoiceServer.this.bufferSize = latencyListener.bufferSize * 4;
                    FrinikaGlobalProperties.AUDIO_BUFFER_LENGTH.setValue(latencyListener.bufferSize);
                    JavaSoundVoiceServer.this.lineOut = (SourceDataLine) AudioSystem.getMixer((Mixer.Info) cb.getSelectedItem()).getLine(new Line.Info(SourceDataLine.class));
                    startAudioOutput();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        dialog.add(applyButton);
        dialog.setSize(600, 200);
        dialog.setVisible(true);
    }

    public void setBufferSize(int len) throws Exception {
        stopAudioOutput();
        bufferSize = len * 4;
        FrinikaGlobalProperties.AUDIO_BUFFER_LENGTH.setValue(len);
        startAudioOutput();
    }

    public void printStats() {
        if (false) {
            System.out.println("Audio Output State");
            try {
                for (Voice gen : audioOutputGenerators) {
                    System.out.println(gen);
                }
                System.out.println("Num of generators: " + audioOutputGenerators.size());
            } catch (Exception e) {
            }
        }
    }
}
