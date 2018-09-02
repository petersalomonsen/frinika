/*
 * Created on Sep 12, 2004
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

import com.frinika.synth.envelope.MidiVolume;
import com.frinika.voiceserver.VoiceInterrupt;
import com.frinika.voiceserver.VoiceServer;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;
import javax.sound.midi.*;

/**
 * @author Peter Johan Salomonsen
 *
 */
public abstract class Synth implements MidiChannel {

    protected boolean sustain = false;

    protected HashMap<Integer, Oscillator> keys = new HashMap<>();
    protected HashMap<Integer, Oscillator> sustainedKeys = new HashMap<>();
    protected LinkedList<Oscillator> oscillators = new LinkedList<>();

    protected PreOscillator preOscillator;
    protected PostOscillator postOscillator;

    private String instrumentName = "New Synth";

    private Vector<InstrumentNameListener> instrumentNameListeners = new Vector<>();

    private boolean mute;

    SynthRack frinikaSynth;

    public Synth(SynthRack synth) {
        this.frinikaSynth = synth;

        preOscillator = new PreOscillator(this);
        postOscillator = new PostOscillator(this);

        preOscillator.nextVoice = postOscillator;

        postOscillator.nextVoice = MasterVoice.getDefaultInstance();

        synth.getVoiceServer().addTransmitter(postOscillator);
        synth.getVoiceServer().addTransmitter(preOscillator);
    }

    protected synchronized void addOscillator(int noteNumber, Oscillator osc) {
        try {
            if (sustain) {
                sustainedKeys.get(noteNumber).release();
                sustainedKeys.remove(noteNumber);
            } else {
                keys.get(noteNumber).release();
                keys.remove(noteNumber);
            }
        } catch (NullPointerException e) {
        }

        osc.nextVoice = postOscillator;
        frinikaSynth.getVoiceServer().addTransmitter(osc);
        keys.put(noteNumber, osc);
        oscillators.add(osc);
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#noteOff(int, int)
     */
    @Override
    public void noteOff(int noteNumber, int velocity) {
        noteOff(noteNumber);
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#noteOff(int)
     */
    @Override
    public synchronized void noteOff(int noteNumber) {
        if (sustain) {
            sustainedKeys.put(noteNumber, keys.get(noteNumber));
        } else {
            Oscillator voice = keys.get(noteNumber);
            if (voice != null) {
                voice.release();
            }
        }
        keys.remove(noteNumber);
    }

    public abstract void loadSettings(Serializable settings);

    public abstract Serializable getSettings();

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setPolyPressure(int, int)
     */
    @Override
    public void setPolyPressure(int noteNumber, int pressure) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getPolyPressure(int)
     */
    @Override
    public int getPolyPressure(int noteNumber) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setChannelPressure(int)
     */
    @Override
    public void setChannelPressure(int pressure) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getChannelPressure()
     */
    @Override
    public int getChannelPressure() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#controlChange(int, int)
     */
    @Override
    public void controlChange(int controller, final int value) {
        switch (controller) {
            case 1:
                preOscillator.setVibratoAmount(value);
                break;
            case 2:
                preOscillator.setVibratoFrequency((float) value);
                break;
            case 10:
                getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
                    @Override
                    public void doInterrupt() {
                        postOscillator.setPan(value);
                    }
                });
                break;
            case 7:
                getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
                    @Override
                    public void doInterrupt() {
                        postOscillator.setVolume(MidiVolume.midiVolumeToAmplitudeRatio(value));
                    }
                });
                break;
            case 20:
                getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
                    @Override
                    public void doInterrupt() {
                        postOscillator.setOverDriveAmount(value);
                    }
                });
                break;
            case 22:
                getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
                    @Override
                    public void doInterrupt() {
                        postOscillator.setEchoAmount(value);
                    }
                });
                break;
            case 23:
                getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
                    @Override
                    public void doInterrupt() {
                        postOscillator.setEchoLength(value);
                    }
                });
                break;
            case 64:
                if (value > 63) {
                    enableSustain();
                } else {
                    disableSustain();
                }
                break;
            case 91:
                getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
                    @Override
                    public void doInterrupt() {
                        postOscillator.setReverb(MidiVolume.midiVolumeToAmplitudeRatio(value));
                    }
                });
                break;
        }
    }

    void enableSustain() {
        getAudioOutput().interruptTransmitter(preOscillator, new VoiceInterrupt() {
            @Override
            public void doInterrupt() {
                sustain = true;
            }
        });
    }

    synchronized void disableSustain() {
        sustain = false;
        for (Oscillator osc : oscillators) {
            if (!keys.containsValue(osc)) {
                osc.release();
            }
        }
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getController(int)
     */
    @Override
    public int getController(int controller) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#programChange(int)
     */
    @Override
    public void programChange(int program) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#programChange(int, int)
     */
    @Override
    public void programChange(int bank, int program) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getProgram()
     */
    @Override
    public int getProgram() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setPitchBend(int)
     */
    @Override
    public void setPitchBend(final int bend) {
        getAudioOutput().interruptTransmitter(preOscillator, new VoiceInterrupt() {
            @Override
            public void doInterrupt() {
                preOscillator.pitchBend = bend;
                preOscillator.pitchBendFactor = (float) Math.pow(2.0, (((double) (bend - 0x2000) / (double) 0x1000) / 12.0));
            }
        });
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getPitchBend()
     */
    @Override
    public int getPitchBend() {
        return preOscillator.pitchBend;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#resetAllControllers()
     */
    @Override
    public void resetAllControllers() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#allNotesOff()
     */
    @Override
    public void allNotesOff() {
        for (Oscillator osc : oscillators) {
            osc.release();
        }
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#allSoundOff()
     */
    @Override
    public void allSoundOff() {
        for (Oscillator osc : oscillators) {
            frinikaSynth.getVoiceServer().removeTransmitter(osc);
        }
        frinikaSynth.getVoiceServer().removeTransmitter(postOscillator);
        frinikaSynth.getVoiceServer().removeTransmitter(preOscillator);
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#localControl(boolean)
     */
    @Override
    public boolean localControl(boolean on) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setMono(boolean)
     */
    @Override
    public void setMono(boolean on) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getMono()
     */
    @Override
    public boolean getMono() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setOmni(boolean)
     */
    @Override
    public void setOmni(boolean on) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getOmni()
     */
    @Override
    public boolean getOmni() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setMute(boolean)
     */
    @Override
    public void setMute(boolean mute) {
        this.mute = mute;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getMute()
     */
    @Override
    public boolean getMute() {
        return mute;
    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setSolo(boolean)
     */
    @Override
    public void setSolo(boolean soloState) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getSolo()
     */
    @Override
    public boolean getSolo() {
        // TODO Auto-generated method stub
        return false;
    }

    public VoiceServer getAudioOutput() {
        return frinikaSynth.getVoiceServer();
    }

    public void close() {
        allSoundOff();
    }

    public void showGUI() {
        System.out.println("Sorry, no GUI...");
    }

    /**
     * @return
     */
    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
        for (InstrumentNameListener instrumentNameListener : instrumentNameListeners) {
            instrumentNameListener.instrumentNameChange(this, instrumentName);
        }
    }

    /**
     * @param strip
     */
    public void addInstrumentNameListener(InstrumentNameListener instrumentNameListener) {
        instrumentNameListeners.add(instrumentNameListener);
    }

    /**
     * @param adapter
     */
    public void removeInstrumentNameListener(InstrumentNameListener instrumentNameListener) {
        instrumentNameListeners.remove(instrumentNameListener);
    }

    /**
     * @return Returns the postOscillator.
     */
    public final PostOscillator getPostOscillator() {
        return postOscillator;
    }

    /**
     * @return Returns the preOscillator.
     */
    public final PreOscillator getPreOscillator() {
        return preOscillator;
    }

    /**
     * @return Returns the frinikaSynth.
     */
    public SynthRack getFrinikaSynth() {
        return frinikaSynth;
    }

    @Override
    public String toString() {
        return getInstrumentName();
    }
}
