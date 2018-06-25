/*
 * Copyright (c) 2011 Peter Johan Salomonsen (http://petersalomonsen.com) - Licensed under GNU LGPL
 */

package com.frinika.jvstsynth;

import com.synthbot.audioplugin.view.StringGui;
import com.synthbot.audioplugin.vst.JVstLoadException;
import com.synthbot.audioplugin.vst.vst2.AbstractJVstHostListener;
import com.synthbot.audioplugin.vst.vst2.JVstHost2;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
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
import javax.swing.JFileChooser;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class FrinikaJVSTSynth implements Synthesizer,Mixer,Serializable {

    static final long serialVersionUID = 1L;

    private int bank;
    private int program;

    private File vstiFile;

    private byte[] programChunk = null;

    private transient StringGui gui;

    private transient float[][] fInputs;
    private transient float[][] fOutputs;
  
    transient JVstHost2 vst = null;
    transient private boolean isOpen = false;
    transient Receiver receiver;

    public FrinikaJVSTSynth() {
        initialize();
    }

    private void initialize() {
        try {
            NativeLibLoader.loadNativeLibs();
        } catch (Throwable ex) {
            Logger.getLogger(FrinikaJVSTSynth.class.getName()).log(Level.SEVERE, null, ex);
        }
        receiver = new Receiver() {

            public void send(MidiMessage message, long timeStamp) {
                if(vst!=null && ShortMessage.class.isInstance(message))
                {
                    ShortMessage shm = (ShortMessage) message;
                    switch(shm.getCommand())
                    {
                        case ShortMessage.PROGRAM_CHANGE:
                            program = shm.getData1();
                            vst.setProgram(program%128+(bank*128));
                            break;
                        default:
                            vst.queueMidiMessage(shm);
                    }


                }

            }

            public void close() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

    }

    public int getMaxPolyphony() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLatency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MidiChannel[] getChannels() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public VoiceStatus[] getVoiceStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSoundbankSupported(Soundbank soundbank) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean loadInstrument(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unloadInstrument(Instrument instrument) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean remapInstrument(Instrument from, Instrument to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Soundbank getDefaultSoundbank() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Instrument[] getAvailableInstruments() {
        ArrayList<Instrument> instruments = new ArrayList<Instrument>();
        for(int n = 0;n<vst.numPrograms();n++)
        {
            instruments.add(new Instrument(null,new Patch(n/128,n%128),vst.getProgramName(n),null) {

                @Override
                public Object getData() {
                    return null;
                }
            });
        }
        Instrument[] instrumentArr = new Instrument[instruments.size()];
        instruments.toArray(instrumentArr);
        return instrumentArr;
    }

    public Instrument[] getLoadedInstruments() {
        return getAvailableInstruments();
    }

    public boolean loadAllInstruments(Soundbank soundbank) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unloadAllInstruments(Soundbank soundbank) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public MidiDevice.Info getDeviceInfo() {
        return new FrinikaJVSTSynthProvider.FrinikaJVSTSynthProviderInfo();
    }

    public void open() {
        isOpen = true;
    }

    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isOpen() {
        return isOpen;
    }

    public long getMicrosecondPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getMaxReceivers() {
        return 1;
    }

    public int getMaxTransmitters() {
        return 0;
    }

    public Receiver getReceiver() throws MidiUnavailableException {
        return receiver;
    }

    public List<Receiver> getReceivers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transmitter getTransmitter() throws MidiUnavailableException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Transmitter> getTransmitters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Mixer.Info getMixerInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Line.Info[] getSourceLineInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Line.Info[] getTargetLineInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Line.Info[] getSourceLineInfo(Line.Info info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Line.Info[] getTargetLineInfo(Line.Info info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isLineSupported(Line.Info info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Line getLine(Line.Info info) throws LineUnavailableException {
        return new TargetDataLine() {

            ByteBuffer buf = null;
            float[] floatBuffer = null;
            FloatBuffer flView = null;

            boolean isOpen = false;
            AudioFormat format;
            public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
                this.format = format;
                buf = ByteBuffer.allocate(bufferSize).order(format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
                flView = buf.asFloatBuffer();
                floatBuffer = new float[flView.capacity()];
                try {
                    if(vst==null)
                    {
                        if(vstiFile==null)
                        {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.showDialog(null, "Select VSTi");
                            vstiFile = fileChooser.getSelectedFile();
                        }
                        vst = JVstHost2.newInstance(vstiFile, format.getSampleRate(), flView.capacity());
                        if(programChunk!=null)
                            vst.setProgramChunk(programChunk);

                        vst.addJVstHostListener(new AbstractJVstHostListener() {
                            @Override
                            public void onAudioMasterAutomate(JVstHost2 vst, int index, float value) {
                                if (gui != null) {
                                    gui.updateParameter(index, value);
                                }
                            }});
                        System.out.println("vst blocksize: "+vst.getBlockSize());
                        if(vst.hasEditor())
                        {
                            vst.openEditor("VST GUI");
                        } else {
                            gui = new StringGui(vst);
                            gui.setVisible(true);
                        }
                        fInputs = new float[vst.numInputs()][flView.capacity()/format.getChannels()];
                        fOutputs = new float[vst.numOutputs()][flView.capacity()/format.getChannels()];
                        isOpen = true;
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(FrinikaJVSTSynth.class.getName()).log(Level.SEVERE, null, ex);
                } catch (JVstLoadException ex) {
                    Logger.getLogger(FrinikaJVSTSynth.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }

            public void open(AudioFormat format) throws LineUnavailableException {
                open(format,16384);
            }

            public int read(byte[] b, int off, int len) {
                if(!isOpen)
                    return len;

                int bytesLeft = len;
                while(bytesLeft>0)
                {
                    int templen = bytesLeft>buf.capacity() ? buf.capacity() : bytesLeft;

                    for(int n=0;n<floatBuffer.length;n++)
                        floatBuffer[n] = 0;

                    fillBuffer(floatBuffer,templen/format.getFrameSize(),format.getChannels());

                    flView.position(0);
                    flView.put(floatBuffer);

                    buf.position(0);

                    buf.get(b,off,templen);
                    off+=buf.capacity();
                    bytesLeft-=templen;
                }
                return len;
            }

            public void drain() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void flush() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void start() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void stop() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean isRunning() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean isActive() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public AudioFormat getFormat() {
                return format;
            }

            public int getBufferSize() {
                return buf.limit();
            }

            public int available() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public int getFramePosition() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public long getLongFramePosition() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public long getMicrosecondPosition() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public float getLevel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Info getLineInfo() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void open() throws LineUnavailableException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void close() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean isOpen() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Control[] getControls() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public boolean isControlSupported(Type control) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Control getControl(Type control) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void addLineListener(LineListener listener) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void removeLineListener(LineListener listener) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public int getMaxLines(Line.Info info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Line[] getSourceLines() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Line[] getTargetLines() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void synchronize(Line[] lines, boolean maintainSync) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void unsynchronize(Line[] lines) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Line.Info getLineInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Control[] getControls() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isControlSupported(Type control) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Control getControl(Type control) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addLineListener(LineListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeLineListener(LineListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void fillBuffer(float[] floatBuffer,int numberOfFrames,int channels)
    {        
        vst.processReplacing(fInputs, fOutputs, numberOfFrames);
        for(int n=0;n<numberOfFrames*channels;n++) {
            floatBuffer[n] = fOutputs[n%channels][n/channels];
        }        
    }

    private void writeObject(java.io.ObjectOutputStream out)
     throws IOException {
        programChunk = vst.getProgramChunk();
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        initialize();
        in.defaultReadObject();
    }
    
    public void show() {
        if(vst.hasEditor())
        {
            vst.openEditor("VST GUI");
        } else {
            gui = new StringGui(vst);
            gui.setVisible(true);
        }        
    }
}
