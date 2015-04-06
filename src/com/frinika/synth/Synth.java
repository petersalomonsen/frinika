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

import com.frinika.voiceserver.VoiceServer;
import com.frinika.voiceserver.VoiceInterrupt;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.sound.midi.*;


import com.frinika.audio.*;
import com.frinika.synth.envelope.MidiVolume;


/**
 * @author Peter Johan Salomonsen
 *
 */
public abstract class Synth implements MidiChannel {
	protected boolean sustain = false;
	
	protected HashMap<Integer,Oscillator> keys = new HashMap<Integer,Oscillator>();
	protected HashMap<Integer,Oscillator> sustainedKeys = new HashMap<Integer,Oscillator>();
	protected LinkedList<Oscillator> oscillators = new LinkedList<Oscillator>();
		
	protected PreOscillator preOscillator;
	protected PostOscillator postOscillator;

    private String instrumentName = "New Synth";

    private Vector<InstrumentNameListener> instrumentNameListeners = new Vector<InstrumentNameListener>();

    private boolean mute;
	
    SynthRack frinikaSynth;
    
	public Synth(SynthRack synth)
	{
        this.frinikaSynth = synth;
        
		preOscillator = new PreOscillator(this);
		postOscillator = new PostOscillator(this);

		preOscillator.nextVoice = postOscillator;
		
        postOscillator.nextVoice = MasterVoice.getDefaultInstance();
		
		synth.getVoiceServer().addTransmitter(postOscillator);
		synth.getVoiceServer().addTransmitter(preOscillator);
	}
	
	protected synchronized void addOscillator(int noteNumber, Oscillator osc)
	{
		try
		{	
			if(sustain)
			{
				sustainedKeys.get(new Integer(noteNumber)).release();
				sustainedKeys.remove(new Integer(noteNumber));
			}
			else
			{
				keys.get(new Integer(noteNumber)).release();
				keys.remove(new Integer(noteNumber));
			}
		} catch(NullPointerException e) {}

		osc.nextVoice = postOscillator;
		frinikaSynth.getVoiceServer().addTransmitter(osc);
		keys.put(new Integer(noteNumber),osc);
		oscillators.add(osc);		
	}
	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#noteOff(int, int)
	 */
	public void noteOff(int noteNumber, int velocity) {
		noteOff(noteNumber);
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#noteOff(int)
	 */
	public synchronized void noteOff(int noteNumber) {
		if(sustain)
			sustainedKeys.put(new Integer(noteNumber),keys.get(new Integer(noteNumber)));
		else
		{
            Oscillator voice = keys.get(new Integer(noteNumber));
            if(voice!=null)
                voice.release();
        }
		keys.remove(new Integer(noteNumber));
	}

	public abstract void loadSettings(Serializable settings);
	
	public abstract Serializable getSettings();

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setPolyPressure(int, int)
	 */
	public void setPolyPressure(int noteNumber, int pressure) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getPolyPressure(int)
	 */
	public int getPolyPressure(int noteNumber) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setChannelPressure(int)
	 */
	public void setChannelPressure(int pressure) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getChannelPressure()
	 */
	public int getChannelPressure() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#controlChange(int, int)
	 */
	public void controlChange(int controller, final int value) {
		switch(controller)
		{
			case 1:
				preOscillator.setVibratoAmount(value);
				break;
			case 2:
				preOscillator.setVibratoFrequency((float)value);
				break;
            case 10:
                getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
                    public void doInterrupt() {
                        postOscillator.setPan(value);
                    }
                });
                break;
			case 7:
				getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
					public void doInterrupt() {
						postOscillator.setVolume(MidiVolume.midiVolumeToAmplitudeRatio(value));
					}
				});		
				break;
			case 20:
				getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
					public void doInterrupt() {
						postOscillator.setOverDriveAmount(value);
					}
				});		
				break;
			case 22:
				getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
					public void doInterrupt() {
						postOscillator.setEchoAmount(value);
					}
				});		
				break;
			case 23:
				getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
					public void doInterrupt() {
						postOscillator.setEchoLength(value);
					}
				});		
				break;
			case 64:
				if(value>63 )
					enableSustain();
				else
					disableSustain();
				break;
			case 91:
				getAudioOutput().interruptTransmitter(postOscillator, new VoiceInterrupt() {
					public void doInterrupt() {
						postOscillator.setReverb(MidiVolume.midiVolumeToAmplitudeRatio(value));
					}
				});		
				break;				
		}		
	}
	
	void enableSustain()
	{
		getAudioOutput().interruptTransmitter(preOscillator, new VoiceInterrupt() {
			public void doInterrupt() {
				sustain = true;
			}
		});
	}
	
	synchronized void disableSustain()
	{
		sustain = false;
		for(Oscillator osc : oscillators)
			if(!keys.containsValue(osc))
				osc.release();
	}
	
	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getController(int)
	 */
	public int getController(int controller) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#programChange(int)
	 */
	public void programChange(int program) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#programChange(int, int)
	 */
	public void programChange(int bank, int program) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getProgram()
	 */
	public int getProgram() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setPitchBend(int)
	 */
	public void setPitchBend(final int bend) {
		getAudioOutput().interruptTransmitter(preOscillator, new VoiceInterrupt() {
			public void doInterrupt() {
				preOscillator.pitchBend = bend;
				preOscillator.pitchBendFactor = (float)Math.pow(2.0,( ((double)(bend-0x2000) / (double)0x1000)/12.0));
			}
		});	
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getPitchBend()
	 */
	public int getPitchBend() {
		return preOscillator.pitchBend;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#resetAllControllers()
	 */
	public void resetAllControllers() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#allNotesOff()
	 */
	public void allNotesOff() {
		for(Oscillator osc : oscillators)
			osc.release();		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#allSoundOff()
	 */
	public void allSoundOff() {
		for(Oscillator osc : oscillators)
			frinikaSynth.getVoiceServer().removeTransmitter(osc);
		frinikaSynth.getVoiceServer().removeTransmitter(postOscillator);
		frinikaSynth.getVoiceServer().removeTransmitter(preOscillator);
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#localControl(boolean)
	 */
	public boolean localControl(boolean on) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setMono(boolean)
	 */
	public void setMono(boolean on) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getMono()
	 */
	public boolean getMono() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setOmni(boolean)
	 */
	public void setOmni(boolean on) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getOmni()
	 */
	public boolean getOmni() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setMute(boolean)
	 */
	public void setMute(boolean mute) {
        this.mute = mute;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getMute()
	 */
	public boolean getMute() {
		return mute;
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#setSolo(boolean)
	 */
	public void setSolo(boolean soloState) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#getSolo()
	 */
	public boolean getSolo() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public VoiceServer getAudioOutput() {
		return frinikaSynth.getVoiceServer();
	}
	
	public void close()
	{
        allSoundOff();
	}

	/**
	 * 
	 */
	public void showGUI() {
		System.out.println("Sorry, no GUI...");
	}

    /**
     * @return
     */
    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName)
    {
        this.instrumentName = instrumentName;
        for(InstrumentNameListener instrumentNameListener : instrumentNameListeners )
            instrumentNameListener.instrumentNameChange(this,instrumentName);
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
