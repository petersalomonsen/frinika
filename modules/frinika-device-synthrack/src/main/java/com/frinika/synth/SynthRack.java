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
package com.frinika.synth;

import com.frinika.audio.gui.ChannelListProvider;
import com.frinika.audio.midi.MidiDeviceIconProvider;
import com.frinika.synth.settings.SynthSettings;
import com.frinika.synth.settings.synthsettingsversions.SynthSettings20070815;
import com.frinika.synth.soundbank.SynthRackSoundbank;
import com.frinika.voiceserver.VoiceServer;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.annotation.Resource;
import javax.sound.midi.Instrument;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Control.Type;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import rasmus.midi.provider.RasmusSynthesizer;

/**
 * The SynthRack class can hold 16 Frinika soft synths - one for each
 * MidiChannel. Also making it ideal for connecting to your MidiKeyboard for
 * live playing. You can then setup 16 synths and shift between them using the
 * channel switch on your master keyboard.
 *
 * @author Peter Salomonsen
 *
 */
public class SynthRack implements Synthesizer, InstrumentNameListener, ChannelListProvider, MidiDeviceIconProvider, Mixer {

    public static final double GAIN = 0.5;
    VoiceServer voiceServer;

    @Resource(name = "Samplerate")
    int samplerate;

    private static Icon icon = new javax.swing.ImageIcon(RasmusSynthesizer.class.getResource("/icons/frinika.png"));

    @Override
    public Icon getIcon() {
        if (icon.getIconHeight() > 16 || icon.getIconWidth() > 16) {
            BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = img.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
            Image im = img.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            icon = new ImageIcon(im);
        }
        return icon;
    }

    boolean saveReferencedData;

    public static class SynthRackInfo extends MidiDevice.Info {

        SynthRackInfo() {
            super("Frinika SynthRack", "petersalomonsen.com", "A MIDI container for Frinika soft synths", "0.4.0");
        }
    }
    MidiDevice.Info deviceInfo = new SynthRackInfo();

    Receiver receiver = new Receiver() {

        /* (non-Javadoc)
         * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
         */
        @Override
        public void send(MidiMessage message, long timeStamp) {
            try {
                if (ShortMessage.class.isInstance(message)) {
                    ShortMessage shm = (ShortMessage) message;
                    int synthIndex = shm.getChannel();
                    MidiChannel channel = midiChannels[shm.getChannel()];
                    if (channel != null) {
                        if (shm.getCommand() == ShortMessage.NOTE_ON) {
                            if (shm.getData2() == 0) {
                                channel.noteOff(shm.getData1());
                            } else if (!channel.getMute()) {
                                channel.noteOn(shm.getData1(), shm.getData2());
                            }
                        } else if (shm.getCommand() == ShortMessage.NOTE_OFF) {
                            channel.noteOff(shm.getData1());
                        } else if (shm.getCommand() == ShortMessage.CONTROL_CHANGE) {
                            channel.controlChange(shm.getData1(), shm.getData2());
                            if (gui != null && shm.getData1() == 7) {
                                gui.synthStrips[synthIndex].setVolume(shm.getData2());
                            } else if (gui != null && shm.getData1() == 10) {
                                gui.synthStrips[synthIndex].setPan(shm.getData2());
                            }
                        } else if (shm.getCommand() == ShortMessage.PITCH_BEND) {
                            channel.setPitchBend((0xff & shm.getData1()) + ((0xff & shm.getData2()) << 7));
                        }
                    }
                    if (shm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                        midiChannels[shm.getChannel()] = synths[shm.getData1()];
                    }
                } else if (MetaMessage.class.isInstance(message)) {
                    byte[] msgBytes = message.getMessage();
                    // Handle tempo message
                    if (msgBytes[0] == -1 && msgBytes[1] == 0x51 && msgBytes[2] == 3) {
                        int mpq = ((msgBytes[3] & 0xff) << 16)
                                | ((msgBytes[4] & 0xff) << 8) | (msgBytes[5] & 0xff);

                        tempoBPM = ((60000000f / mpq));
                        java.util.logging.Logger.getLogger(this.getClass().getName()).fine("Tempo set to " + tempoBPM + " bpm");
                    }
                }
            } catch (Exception e) {
                // For debugging
//                e.printStackTrace();
            }
        }

        /* (non-Javadoc)
         * @see javax.sound.midi.Receiver#close()
         */
        @Override
        public void close() {
            // TODO Auto-generated method stub

        }

    };

    Vector<Receiver> receivers = new Vector<>();

    {
        receivers.add(receiver);
    }

    Vector<Transmitter> transmitters = new Vector<>();

    SynthRackGUI gui;
    // Previously - one synth per channel - now one synth per program (in the future also banks will be supported)
    Synth[] synths = new Synth[256];

    MidiChannel[] midiChannels = new MidiChannel[16];

    private List<GlobalInstrumentNameListener> globalInstrumentNameListeners = new ArrayList<>();
    private SynthRackSoundbank soundbank = new SynthRackSoundbank();

    private float tempoBPM = 100f;

    public SynthRack(VoiceServer voiceServer) {
        this.voiceServer = voiceServer;

        if (voiceServer != null) {
            MasterVoice.getDefaultInstance().initialize(voiceServer);
        }
    }

    public void setSynth(int index, Synth synth) {
        if (synths[index] != null) {
            synths[index].close();
        }

        if (synth != null) {
            synth.addInstrumentNameListener(this);
            Patch patch = new Patch(0, index);
            soundbank.createAndRegisterInstrument(patch, synth);
        }

        synths[index] = synth;

        if (gui != null) {
            gui.synthStrips[index].setSynth(synth);
        }

    }

    public Synth getSynth(int index) {
        return (synths[index]);
    }

    public SynthSettings getSynthSetup() {
        SynthSettings setup = new SynthSettings20070815();
        String[] synthClassNames = setup.getSynthClassNames();
        Serializable[] synthSettings = setup.getSynthSettings();
        for (int n = 0; n < synths.length; n++) {
            try {
                synthClassNames[n] = synths[n].getClass().getName();
                synthSettings[n] = synths[n].getSettings();
            } catch (NullPointerException nex) {
            }
        }
        return (setup);
    }

    public void clearSynths() {
        for (int n = 0; n < synths.length; n++) {
            try {
                setSynth(n, null);
            } catch (Exception e) {
            }
        }
    }

    public void loadSynthSetup(final SynthSettings setup) {
        if (gui != null) {
            gui.initLoadSynthSetupProgress(
                    new Thread() {
                @Override
                public void run() {
                    loadSynthSetupThread(setup);
                }
            });
        } else {
            loadSynthSetupThread(setup);
        }
    }

    public void loadSynthSetupThread(SynthSettings setup) {
        if (voiceServer == null) {
            setVoiceServer(new VoiceServer() {
                @Override
                public void configureAudioOutput(JFrame frame) {
                    // TODO Auto-generated method stub
                }
            });
        }

        clearSynths();
        for (int n = 0; n < setup.getSynthClassNames().length; n++) {
            try {
                String synthName = setup.getSynthClassNames()[n];

                if (synthName == null) {
                    break;
                }

                // Handle older fileformats
                if (synthName.equals("com.petersalomonsen.mystudio.mysynth.synths.SoundFont")
                        || synthName.equals("com.petersalomonsen.mystudio.mysynth.synths.MySampler")) {
                    synthName = com.frinika.synth.synths.MySampler.class.getName();
                }

                setSynth(n, (Synth) Class.forName(synthName).getConstructors()[0].newInstance(new Object[]{this}));
                synths[n].loadSettings(setup.getSynthSettings()[n]);

                if (gui != null) {
                    gui.notifyLoadSynthSetupProgress((n * 100) / setup.getSynthClassNames().length, synths[n].getInstrumentName());
                }
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | SecurityException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (gui != null) {
            gui.notifyLoadSynthSetupProgress(100, "Completed");
        }
    }

    public void save(File file) {
        try {
            SynthSettings setup = getSynthSetup();

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            out.writeObject(setup);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(File file) {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            SynthSettings setup = (SynthSettings) in.readObject();
            loadSynthSetup(setup);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#getMaxPolyphony()
     */
    @Override
    public int getMaxPolyphony() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#getLatency()
     */
    @Override
    public long getLatency() {
        return voiceServer.getLatency();
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#getChannels()
     */
    @Override
    public MidiChannel[] getChannels() {
        return null;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#getVoiceStatus()
     */
    @Override
    public VoiceStatus[] getVoiceStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#isSoundbankSupported(javax.sound.midi.Soundbank)
     */
    @Override
    public boolean isSoundbankSupported(Soundbank soundbank) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#loadInstrument(javax.sound.midi.Instrument)
     */
    @Override
    public boolean loadInstrument(Instrument instrument) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#unloadInstrument(javax.sound.midi.Instrument)
     */
    @Override
    public void unloadInstrument(Instrument instrument) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#remapInstrument(javax.sound.midi.Instrument, javax.sound.midi.Instrument)
     */
    @Override
    public boolean remapInstrument(Instrument from, Instrument to) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#getDefaultSoundbank()
     */
    @Override
    public Soundbank getDefaultSoundbank() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#getAvailableInstruments()
     */
    @Override
    public Instrument[] getAvailableInstruments() {
        // No default instruments for SynthRack - hence no instruments returned
        return new Instrument[0];
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#getLoadedInstruments()
     */
    @Override
    public Instrument[] getLoadedInstruments() {
        return soundbank.getInstruments();
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#loadAllInstruments(javax.sound.midi.Soundbank)
     */
    @Override
    public boolean loadAllInstruments(Soundbank soundbank) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#unloadAllInstruments(javax.sound.midi.Soundbank)
     */
    @Override
    public void unloadAllInstruments(Soundbank soundbank) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#loadInstruments(javax.sound.midi.Soundbank, javax.sound.midi.Patch[])
     */
    @Override
    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.Synthesizer#unloadInstruments(javax.sound.midi.Soundbank, javax.sound.midi.Patch[])
     */
    @Override
    public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#getDeviceInfo()
     */
    @Override
    public MidiDevice.Info getDeviceInfo() {
        return deviceInfo;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#open()
     */
    @Override
    public void open() {
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#close()
     */
    @Override
    public void close() {
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#isOpen()
     */
    @Override
    public boolean isOpen() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#getMicrosecondPosition()
     */
    @Override
    public long getMicrosecondPosition() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#getMaxReceivers()
     */
    @Override
    public int getMaxReceivers() {
        return -1;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#getMaxTransmitters()
     */
    @Override
    public int getMaxTransmitters() {
        return 0;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#getReceiver()
     */
    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
        return receiver;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#getReceivers()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List getReceivers() {
        return receivers;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#getTransmitter()
     */
    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
        return null;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiDevice#getTransmitters()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List getTransmitters() {
        return transmitters;
    }

    public void addGlobalInstrumentNameListener(GlobalInstrumentNameListener globalInstrumentNameListener) {
        globalInstrumentNameListeners.add(globalInstrumentNameListener);
    }

    /* (non-Javadoc)
     * @see com.petersalomonsen.mystudio.mysynth.InstrumentNameListener#instrumentNameChange(java.lang.String)
     */
    @Override
    public void instrumentNameChange(Synth synth, String instrumentName) {
        for (int n = 0; n < synths.length; n++) {
            if (synth.equals(synths[n])) {
                for (GlobalInstrumentNameListener globalInstrumentNameListener : globalInstrumentNameListeners) {
                    globalInstrumentNameListener.instrumentNameChange(n, instrumentName);
                }
            }
        }
    }

    public int getNumberOfSynths() {
        return synths.length;
    }

    /**
     * @return Returns the voiceServer.
     */
    public VoiceServer getVoiceServer() {
        return voiceServer;
    }

    /**
     * over to provide easier GUI manufactoring
     */
    @Override
    public String toString() {
        return getDeviceInfo().toString();
    }

    @Override
    public Object[] getList() {
        // TODO Auto-generated method stub
        return synths;
    }

    /**
     * @return Returns the saveReferencedData.
     */
    public boolean isSaveReferencedData() {
        return saveReferencedData;
    }

    /**
     * @param saveReferencedData The saveReferencedData to set.
     */
    public void setSaveReferencedData(boolean saveReferencedData) {
        this.saveReferencedData = saveReferencedData;
    }

    public void setVoiceServer(VoiceServer voiceServer) {
        this.voiceServer = voiceServer;
        if (voiceServer != null) {
            MasterVoice.getDefaultInstance().initialize(voiceServer);
        }
    }

    @Override
    public Line getLine(javax.sound.sampled.Line.Info info) throws LineUnavailableException {
        return new TargetDataLine() {

            ByteBuffer buf = null;
            float[] floatBuffer = null;
            FloatBuffer flView = null;

            @Override
            public void open(AudioFormat format) throws LineUnavailableException {
            }

            @Override
            public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
                // TODO Auto-generated method stub
            }

            @Override
            public int read(byte[] b, int off, int len) {
                // TODO implement a proper read method
                if (buf == null || buf.capacity() != len) {
                    buf = ByteBuffer.allocate(b.length).order(ByteOrder.nativeOrder());
                    flView = buf.asFloatBuffer();
                    floatBuffer = new float[flView.capacity()];
                }
                for (int n = 0; n < floatBuffer.length; n++) {
                    floatBuffer[n] = 0;
                }
                voiceServer.read(floatBuffer);
                flView.position(0);
                flView.put(floatBuffer);
                buf.position(0);
                buf.get(b);

                return len;
            }

            @Override
            public int available() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public void drain() {
                // TODO Auto-generated method stub
            }

            @Override
            public void flush() {
                // TODO Auto-generated method stub
            }

            @Override
            public int getBufferSize() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public AudioFormat getFormat() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getFramePosition() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public float getLevel() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getLongFramePosition() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public long getMicrosecondPosition() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public boolean isActive() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isRunning() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void start() {
                // TODO Auto-generated method stub

            }

            @Override
            public void stop() {
                // TODO Auto-generated method stub

            }

            @Override
            public void addLineListener(LineListener listener) {
                // TODO Auto-generated method stub

            }

            @Override
            public void close() {
                // TODO Auto-generated method stub

            }

            @Override
            public Control getControl(Type control) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Control[] getControls() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public javax.sound.sampled.Line.Info getLineInfo() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isControlSupported(Type control) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isOpen() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void open() throws LineUnavailableException {
                // TODO Auto-generated method stub
            }

            @Override
            public void removeLineListener(LineListener listener) {
                // TODO Auto-generated method stub
            }
        };
    }

    @Override
    public int getMaxLines(javax.sound.sampled.Line.Info info) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public javax.sound.sampled.Mixer.Info getMixerInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.sound.sampled.Line.Info[] getSourceLineInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.sound.sampled.Line.Info[] getSourceLineInfo(javax.sound.sampled.Line.Info info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Line[] getSourceLines() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.sound.sampled.Line.Info[] getTargetLineInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.sound.sampled.Line.Info[] getTargetLineInfo(javax.sound.sampled.Line.Info info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Line[] getTargetLines() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLineSupported(javax.sound.sampled.Line.Info info) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void synchronize(Line[] lines, boolean maintainSync) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unsynchronize(Line[] lines) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addLineListener(LineListener listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public Control getControl(Type control) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Control[] getControls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public javax.sound.sampled.Line.Info getLineInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isControlSupported(Type control) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeLineListener(LineListener listener) {
        // TODO Auto-generated method stub
    }

    public float getTempoBPM() {
        return tempoBPM;
    }

    /**
     * Frinika specific method to show gui of this synth
     *
     */
    public void show() {
        JFrame f = new JFrame();
        JScrollPane s = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        if (gui == null) {
            gui = new SynthRackGUI(f, this);
        }

        f.add(s);
        f.setVisible(true);
        f.setSize(600, 400);
        s.getViewport().add(gui);

    }

    /**
     * Local test program for SynthRack with GUI - opens a SynthRack midi device
     * and creates the GUI dialog
     *
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String args[]) throws Exception {
        MidiDevice.Info info = null;
        for (MidiDevice.Info inf : MidiSystem.getMidiDeviceInfo()) {
            if (inf.getClass().equals(SynthRack.SynthRackInfo.class)) {
                info = inf;
            }
        }
        SynthRack s = ((SynthRack) MidiSystem.getMidiDevice(info));
        s.show();
        s.gui.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
