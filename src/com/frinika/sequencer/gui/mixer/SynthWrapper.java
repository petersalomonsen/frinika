/*
 * Created on Apr 15, 2006
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.gui.mixer;

import com.frinika.global.FrinikaConfig;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
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
import javax.sound.midi.spi.SoundbankReader;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.Icon;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.renderer.MidiRender;
import com.frinika.renderer.MidiRenderFactory;
import com.frinika.soundbank.JARSoundbankLoader;
import com.sun.media.sound.AudioSynthesizer;
import java.lang.reflect.Field;
import javax.annotation.Resource;
import javax.sound.midi.Sequencer;
import uk.org.toot.audio.server.AudioServer;

/**
 * Synthesizer wrapper for external midi out devices. Used to intercept midi messages
 * and update the mixer gui (mixer sliders etc.)
 * 
 * @author Peter Johan Salomonsen
 */
public class SynthWrapper implements Synthesizer, MidiRenderFactory,MidiDeviceIconProvider {

    MidiDevice midiDevice;
    MidiDeviceMixerPanel gui;
    AudioProcess synthVoice = null;
    public String soundBankFile;  // Hack (for simphoney to propagate to the mididevicedescriptor ).
    ProjectContainer project;

    MidiChannel[] midiChannels = new MidiChannel[16];
    {
        for(int n=0;n<midiChannels.length;n++)
        {
            final int channelNo = n;
            
            midiChannels[n] = new MidiChannel() {
                int channel = channelNo;
                boolean mute = false;
                
                public void noteOn(int noteNumber, int velocity) {
                    // TODO Auto-generated method stub                    
                }

                public void noteOff(int noteNumber, int velocity) {
                    // TODO Auto-generated method stub
                }

                public void noteOff(int noteNumber) {
                    // TODO Auto-generated method stub
                }

                public void setPolyPressure(int noteNumber, int pressure) {
                    // TODO Auto-generated method stub
                }

                public int getPolyPressure(int noteNumber) {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public void setChannelPressure(int pressure) {
                    // TODO Auto-generated method stub                    
                }

                public int getChannelPressure() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public void controlChange(int controller, int value) {
                    ShortMessage shm = new ShortMessage();
                    try {
                        shm.setMessage(ShortMessage.CONTROL_CHANGE,channel,controller,value);
                        sendMidiMessage(shm,-1);
                    } catch (InvalidMidiDataException e) {
                        e.printStackTrace();
                    }
                }

                public int getController(int controller) {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public void programChange(int program) {
                    // TODO Auto-generated method stub
                    
                }

                public void programChange(int bank, int program) {
                    // TODO Auto-generated method stub
                    
                }

                public int getProgram() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public void setPitchBend(int bend) {
                    // TODO Auto-generated method stub
                    
                }

                public int getPitchBend() {
                    // TODO Auto-generated method stub
                    return 0;
                }

                public void resetAllControllers() {
                    // TODO Auto-generated method stub
                    
                }

                public void allNotesOff() {
                    // TODO Auto-generated method stub
                    
                }

                public void allSoundOff() {
                    // TODO Auto-generated method stub
                    
                }

                public boolean localControl(boolean on) {
                    // TODO Auto-generated method stub
                    return false;
                }

                public void setMono(boolean on) {
                    // TODO Auto-generated method stub
                    
                }

                public boolean getMono() {
                    // TODO Auto-generated method stub
                    return false;
                }

                public void setOmni(boolean on) {
                    // TODO Auto-generated method stub
                    
                }

                public boolean getOmni() {
                    // TODO Auto-generated method stub
                    return false;
                }

                public void setMute(boolean mute) {
                    this.mute = mute;
                    
                }

                public boolean getMute() {
                
                    return mute;
                }

                public void setSolo(boolean soloState) {
                    // TODO Auto-generated method stub
                    
                }

                public boolean getSolo() {
                    // TODO Auto-generated method stub
                    return false;
                }
                
            };
        }
    }
    
    Receiver receiver = new Receiver(){

        /* (non-Javadoc)
         * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
         */
        public void send(MidiMessage message, long timeStamp) {
            try
            {
                // Check if short message and update the gui
                if(message instanceof ShortMessage)
                {
                    ShortMessage shm = (ShortMessage)message;
                    int channel = shm.getChannel();

                    // Check for mute
                    if(midiChannels[channel].getMute())
                        return;

                    // Pass the message onto midi device
                    sendMidiMessage(message,timeStamp);
                    
                    if(shm.getCommand() == ShortMessage.NOTE_ON)
                    {
                    }
                    else if(shm.getCommand() == ShortMessage.CONTROL_CHANGE)
                    {
                        if(gui!=null && shm.getData1()==7)
                            gui.mixerSlots[channel].setVolume(shm.getData2());
                        else if(gui!=null && shm.getData1()==10)
                            gui.mixerSlots[channel].setPan(shm.getData2());
                    }
                    else if(shm.getCommand() == ShortMessage.PITCH_BEND)
                    {
                        
                    }
                }
                else
                    // Pass the message onto midi device
                	sendMidiMessage(message,timeStamp);

            }
            catch(Exception e) {
                // For debugging
                //e.printStackTrace();
            }
        }

        /* (non-Javadoc)
         * @see javax.sound.midi.Receiver#close()
         */
        public void close() {
            try {
                midiDevice.getReceiver().close();
            } catch (MidiUnavailableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }
        
    };

    /**
     * Dependency injection (Frinika config, project properties e.g.)
     */
    final void injectDependencies()
    {
        Logger.getLogger(getClass().getName()).fine("Injecting dependencies into "+midiDevice.getClass().getName());
        for(Field field : midiDevice.getClass().getDeclaredFields())
        {
            if(field.isAnnotationPresent(Resource.class))
            {
                Resource resourceAnnotation = (Resource)field.getAnnotation(Resource.class);
                if(resourceAnnotation.name().equals("Samplerate"))
                {
                    field.setAccessible(true);
                    Logger.getLogger(getClass().getName()).log(Level.FINE, "Found resource annotation in class {0} with name {1}. Injecting this resource.", new Object[]{midiDevice.getClass().getName(), resourceAnnotation.name()});
                    try {
                        field.setInt(midiDevice, FrinikaConfig.sampleRate);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SynthWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(SynthWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(field.getType().isAssignableFrom(AudioServer.class))
                {
                    field.setAccessible(true);
                    Logger.getLogger(getClass().getName()).log(Level.FINE, "Found resource annotation in class {0} of type {1}. Injecting this resource.", new Object[]{midiDevice.getClass().getName(), field.getType().getName()});
                    try {
                        field.set(midiDevice, project.getAudioServer());
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SynthWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(SynthWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(field.getType().isAssignableFrom(Sequencer.class))
                {
                    field.setAccessible(true);
                    Logger.getLogger(getClass().getName()).log(Level.FINE, "Found resource annotation in class {0} of type {1}. Injecting this resource.", new Object[]{midiDevice.getClass().getName(), field.getType().getName()});
                    try {
                        field.set(midiDevice, project.getSequencer());
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SynthWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalAccessException ex) {
                        Logger.getLogger(SynthWrapper.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }

    }

    /**
     * 
     * @param project - Only neccesary if the device is a synthesizer implementing the mixer interface (NOT USED? --PJL)
     * @param midiDevice
     * @throws MidiUnavailableException 
     */
    public SynthWrapper(ProjectContainer project,final MidiDevice midiDevice)
    {
        this.midiDevice = midiDevice;
        this.project = project;

        injectDependencies();

        /*  This is for midi devices implementing the AudioSynthesizer interface (gervill)
        *  A target dataline will be retrieved from the device and a voice will feed
        *  the dataline into the voiceserver
        */
        if(midiDevice instanceof AudioSynthesizer)
        {
        	
        	try
        	{        	
        		AudioFormat.Encoding PCM_FLOAT = new AudioFormat.Encoding("PCM_FLOAT");
    			AudioFormat format = new AudioFormat(PCM_FLOAT, 44100, 32, 2, 4*2, 44100, ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN));
        	
    			AudioSynthesizer audosynth = (AudioSynthesizer)midiDevice;
    			final AudioInputStream ais = audosynth.openStream(format, null);   // TODO replace null with a map
    			
	            System.out.println("PCM_FLOAT Encoding used!");
	            synthVoice = new AudioProcess(){
	                    
	                    byte[] streamBuffer = null;
	                    float[] floatArray = null;
	                    FloatBuffer floatBuffer = null;
	                    
	  
	                    
						public void close() {
							// TODO Auto-generated method stub
							
						}

						public void open() {
							// TODO Auto-generated method stub
							
						}

						public int processAudio(AudioBuffer buffer) {
                            if(buffer==null)
                            	return 0;

                            if(streamBuffer==null || streamBuffer.length!=buffer.getSampleCount()*8)
                            {
                                    ByteBuffer bytebuffer = ByteBuffer.allocate(buffer.getSampleCount()*8).order(ByteOrder.nativeOrder());
                                    streamBuffer = bytebuffer.array();
                                    floatArray = new float[buffer.getSampleCount() * 2];
                                    floatBuffer = bytebuffer.asFloatBuffer();
                            }                   
							
							if(supress_audio)
							{
	                            float[] left = buffer.getChannel(0);
	                            float[] right = buffer.getChannel(1);
								Arrays.fill(left, 0, buffer.getSampleCount(), 0);
								Arrays.fill(right, 0, buffer.getSampleCount(), 0);
							}
							else
							{
								try {
									ais.read(streamBuffer, 0, buffer.getSampleCount()*8);
								} catch (IOException e) {
									e.printStackTrace();
								}
                            
								floatBuffer.position(0);
								floatBuffer.get(floatArray);
                            
								float[] left = buffer.getChannel(0);
								float[] right = buffer.getChannel(1);
								for(int n=0;n<buffer.getSampleCount()*2;n+=2)
								{
									left[n/2]=floatArray[n];                                                 
									right[n/2]=floatArray[n+1];
								}
							}
                            
                            AudioProcess r_proc = render_audioprocess;
                            if(r_proc != null) r_proc.processAudio(buffer);
                            
							return AUDIO_OK;
						}
	            };  

        	} catch(Exception e) {e.printStackTrace(); }
    			
        } else
			try {
				midiDevice.open();
			} catch (MidiUnavailableException e1) {
				e1.printStackTrace();
			}
        
	   	/*  This is for midi devices implementing the Mixer interface (RasmusDSP)
		*  A target dataline will be retrieved from the device and a voice will feed
		*  the dataline into the voiceserver
		*/
        if(midiDevice instanceof Mixer)
		{
        	try
        	{
                if (midiDevice.isOpen()) {
                    System.err.println(midiDevice + " Already open");
                } else {
                    midiDevice.open();
                }
	    		final TargetDataLine line = (TargetDataLine)((Mixer)midiDevice).getLine( new Line.Info(TargetDataLine.class));
	            
	    		AudioFormat.Encoding PCM_FLOAT = new AudioFormat.Encoding("PCM_FLOAT");
	            AudioFormat format = new AudioFormat(PCM_FLOAT, 44100, 32, 2, 4*2, 44100, ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN));
	
	            line.open(format);
	            // Add a voice representing the soundfont synth
	            
	            System.out.println("PCM_FLOAT Encoding used!");
	            synthVoice = new AudioProcess(){
	                    
	                    byte[] streamBuffer = null;
	                    float[] floatArray = null;
	                    FloatBuffer floatBuffer = null;
	                    
	                    
						public void close() {
							// TODO Auto-generated method stub
							
						}

						public void open() {
							// TODO Auto-generated method stub
							
						}

						public int processAudio(AudioBuffer buffer) {
                            if(buffer==null)
                            	return 0;
							if(streamBuffer==null || streamBuffer.length!=buffer.getSampleCount()*8)
                            {
                                    ByteBuffer bytebuffer = ByteBuffer.allocate(buffer.getSampleCount()*8).order(ByteOrder.nativeOrder());
                                    streamBuffer = bytebuffer.array();
                                    floatArray = new float[buffer.getSampleCount() * 2];
                                    floatBuffer = bytebuffer.asFloatBuffer();
                            }                   
							
							if(supress_audio)
							{
	                            float[] left = buffer.getChannel(0);
	                            float[] right = buffer.getChannel(1);
								Arrays.fill(left, 0, buffer.getSampleCount(), 0);
								Arrays.fill(right, 0, buffer.getSampleCount(), 0);
							}
							else
							{
								line.read(streamBuffer, 0, buffer.getSampleCount()*8);
                            
								floatBuffer.position(0);
								floatBuffer.get(floatArray);
                            
								float[] left = buffer.getChannel(0);
								float[] right = buffer.getChannel(1);
								for(int n=0;n<buffer.getSampleCount()*2;n+=2)
								{
									left[n/2]=floatArray[n];                                                 
									right[n/2]=floatArray[n+1];
								}
							}
                            
                            AudioProcess r_proc = render_audioprocess;
                            if(r_proc != null) r_proc.processAudio(buffer);
                            
							return AUDIO_OK;
						}
	            };  
	      
        	} catch(Exception e) {e.printStackTrace(); }
        }

    }
    
    /**
     * 
     * You can use this grab the output from the synth.
     * 
     * @return the audio process of this synth voice.
     * 
     */
    public AudioProcess getAudioProcess() {  // PJL
    	return synthVoice;
    }

    public Receiver getReceiver() throws MidiUnavailableException {
        return receiver;
    }

    public List<Receiver> getReceivers() {
        List<Receiver> receivers = new ArrayList<Receiver>();
        receivers.add(receiver);
        for(Receiver recv : midiDevice.getReceivers())
        {
            try {
                if(recv != midiDevice.getReceiver())
                    receivers.add(recv);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        return receivers;
    }

    public Transmitter getTransmitter() throws MidiUnavailableException {
        return midiDevice.getTransmitter();
    }

    public List<Transmitter> getTransmitters() {
        return midiDevice.getTransmitters();
    }

    public Info getDeviceInfo() {
        return midiDevice.getDeviceInfo();
    }

    public void open() throws MidiUnavailableException {
        midiDevice.open();
        
    }

    public void close() {
        midiDevice.close();
    }

    public boolean isOpen() {
        return midiDevice.isOpen();
    }

    public long getMicrosecondPosition() {
        return midiDevice.getMicrosecondPosition();
    }

    public int getMaxReceivers() {
        return midiDevice.getMaxReceivers();
    }

    public int getMaxTransmitters() {
        return midiDevice.getMaxTransmitters();
    }

    public int getMaxPolyphony() {
    	if(midiDevice instanceof Synthesizer)
    		return ((Synthesizer)midiDevice).getMaxPolyphony();
    	else
    		return 0;
    }

    public long getLatency() {
    	if(midiDevice instanceof Synthesizer)
    		return ((Synthesizer)midiDevice).getLatency();
    	else
    		return 0;
    }

    public MidiChannel[] getChannels() {
        return midiChannels;
    }

    public VoiceStatus[] getVoiceStatus() {
    	if(midiDevice instanceof Synthesizer)
    		return ((Synthesizer)midiDevice).getVoiceStatus();
    	else
    		return null;
    }

    public boolean isSoundbankSupported(Soundbank soundbank) {
    	if(midiDevice instanceof Synthesizer)
    		return ((Synthesizer)midiDevice).isSoundbankSupported(soundbank);
    	else
    		return false;
    }

    public boolean loadInstrument(Instrument instrument) {
    	if(midiDevice instanceof Synthesizer)
    		return ((Synthesizer)midiDevice).loadInstrument(instrument);
    	else
    		return false;
    }

    public void unloadInstrument(Instrument instrument) {
    	if(midiDevice instanceof Synthesizer)
    		((Synthesizer)midiDevice).unloadInstrument(instrument);
        
    }

    public boolean remapInstrument(Instrument from, Instrument to) {
    	if(midiDevice instanceof Synthesizer)
    	{
    		return ((Synthesizer)midiDevice).remapInstrument(from,to);
    	}
    	else return false;
    }

    public Soundbank getDefaultSoundbank() {
    	if(midiDevice instanceof Synthesizer)
    	{
    		return ((Synthesizer)midiDevice).getDefaultSoundbank();
    	}
    	else return null;
    }

    public Instrument[] getAvailableInstruments() {
    	if(midiDevice instanceof Synthesizer)
    	{
    		return ((Synthesizer)midiDevice).getAvailableInstruments();
    	}
    	else return null;
    }

    public Instrument[] getLoadedInstruments() {
    	if(midiDevice instanceof Synthesizer)
    	{
    		return ((Synthesizer)midiDevice).getLoadedInstruments();
    	}
    	else return null;
    }

    public boolean loadAllInstruments(Soundbank soundbank) {
    	if(midiDevice instanceof Synthesizer)
    	{
    		return ((Synthesizer)midiDevice).loadAllInstruments(soundbank);
    	}
    	else return false;
    }

    public void unloadAllInstruments(Soundbank soundbank) {
    	if(midiDevice instanceof Synthesizer)
    		((Synthesizer)midiDevice).unloadAllInstruments(soundbank);        
    }

    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
    	if(midiDevice instanceof Synthesizer)
    	{
    		return ((Synthesizer)midiDevice).loadInstruments(soundbank,patchList);
    	}
    	else return false;
    }

    public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
    	if(midiDevice instanceof Synthesizer)
    		((Synthesizer)midiDevice).unloadInstruments(soundbank,patchList);
    }
    /**
     * Override toString to provide easy construction of GUI selectors.
     */
    public String toString() {
    	return midiDevice.getDeviceInfo().toString();
    }
    
//    /**
//     * 
//     * @return
//     * @deprecated 
//     */
//	public String getVoiceHandle() {
//		// TODO deduce from getDevInfo()
//		return "SW1000";
//	}
	
	void sendMidiMessage(final MidiMessage message, final long timestamp)
	{
		try {
			midiDevice.getReceiver().send(message,timestamp);
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns the midiDevice that this synthwrapper is wrapping
	 * @return
	 */
	public MidiDevice getRealDevice() 
	{
		return midiDevice;
	}
	
	public void setSaveReferencedData(boolean saveReferencedData)
	{
		try {
			Method setName = midiDevice.getClass().getMethod("setSaveReferencedData", Boolean.TYPE);
			setName.invoke(midiDevice, saveReferencedData);
		} catch (Exception e) {
		}		
	}
	
	public boolean isRenderable()
	{		
		if(!(midiDevice instanceof Synthesizer)) return false;
		if(!(midiDevice instanceof Mixer)) return false;
		if(!(midiDevice instanceof Cloneable)) return false;

		final Synthesizer midiDevice ;
		try {
			if(this.midiDevice.getClass().getMethod("clone") == null) return false; 
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}		
		
		
		return true;
	}
	
	private volatile AudioProcess render_audioprocess = null;
	private volatile boolean supress_audio = false;
	public void setRenderAudioProcess(AudioProcess audioprocess)
	{
		render_audioprocess = audioprocess;
	}
	public void setSupressAudio(boolean supress_audio)
	{
		this.supress_audio = supress_audio;
	}

	public MidiRender getRender(float samplerate, int channels) {
		
		if(!(midiDevice instanceof Synthesizer)) return null;
		if(!(midiDevice instanceof Mixer)) return null;
		if(!(midiDevice instanceof Cloneable)) return null;
		
		final Synthesizer midiDevice ;
		try {
			midiDevice = (Synthesizer)this.midiDevice.getClass().getMethod("clone").invoke(this.midiDevice);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
		final Mixer mixer = (Mixer)midiDevice;
		
		final TargetDataLine line;
		try {
			line = (TargetDataLine)((Mixer)midiDevice).getLine( new Line.Info(TargetDataLine.class));
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
				        
		AudioFormat.Encoding PCM_FLOAT = new AudioFormat.Encoding("PCM_FLOAT");
        AudioFormat format = new AudioFormat(PCM_FLOAT, samplerate, 32, channels, 4*channels, 4*channels*samplerate, ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN));

        try {
			line.open(format);
		} catch (LineUnavailableException e) { 
			e.printStackTrace();
			return null;
		}		
				
		try {
			midiDevice.open();
		} catch (MidiUnavailableException e1) {
			e1.printStackTrace();
		}
				
		final Receiver recv;
		try {
			recv = midiDevice.getReceiver();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
			return null;
		}
		
		byte[] dummy = new byte[1*4*2]; // The Init buffer 
		line.read(dummy, 0, dummy.length);
		
		
		MidiRender render = new MidiRender()
		{
			TargetDataLine f_line = line;
			MidiDevice f_dev = midiDevice;
			Receiver f_recv = recv;
            byte[] streamBuffer = null;
            FloatBuffer floatBuffer = null;
			boolean first = true;
			public void send(MidiMessage message) {			
				
				f_recv.send(message, -1);
			}
			public int read(float[] buffer, int from, int to) {				
                if(buffer==null)
                	return 0;
                int len = to - from;
				if(streamBuffer==null || streamBuffer.length!=len*4)
                {
                        ByteBuffer bytebuffer = ByteBuffer.allocate(len*4).order(ByteOrder.nativeOrder());
                        streamBuffer = bytebuffer.array();
                        floatBuffer = bytebuffer.asFloatBuffer();
                }                                               
                line.read(streamBuffer, 0, len*4);                
                floatBuffer.position(0);
                floatBuffer.get(buffer, from, len);                
				return len;
			}
			public void close() {
				f_line.close();
				midiDevice.close();				
			}
		};
		
		return render;
	}
	
	Icon icon;
	public Icon getIcon()
	{		
		if(icon != null) return icon;
		icon = ProjectFrame.getMidiDeviceIcon(midiDevice);
		return icon;
	}
	
	
	public Soundbank getSoundbank(File file) throws InvalidMidiDataException, IOException
	{                          
            Soundbank soundbank;
            if(file.getName().toLowerCase().endsWith(".jar"))
            {
                // Special case for JAR soundbanks that should be saved to temporary file in order to avoid urlclassloader cache
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Jar soundbank {0}", file.getName());
                file = JARSoundbankLoader.getTempSoundbankFile(file);
            }         
            if(midiDevice instanceof SoundbankReader)
            {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting soundbank {0} using Synth", file);
                soundbank =  ((SoundbankReader)midiDevice).getSoundbank(file);
                
            }
            else
            {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Getting soundbank {0} using MidiSystem", file);
                soundbank = MidiSystem.getSoundbank(file); 
            }
            
            for(Field field : soundbank.getClass().getDeclaredFields())
            {
                if(field.isAnnotationPresent(Resource.class))
                {
                    if(Sequencer.class.isAssignableFrom(field.getType()))
                    {
                        field.setAccessible(true);
                        try {
                            field.set(soundbank,project.getSequencer());
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(SynthWrapper.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(SynthWrapper.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            return soundbank;
	}		
}
