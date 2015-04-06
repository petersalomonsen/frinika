/*
 * Created on Sep 30, 2004
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
package com.frinika.synth.synths;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import com.frinika.global.FrinikaConfig;
import com.frinika.synth.*;
import com.frinika.synth.envelope.VolumeEnvelope;
import com.frinika.synth.importers.soundfont.SoundFontImporter;
import com.frinika.synth.importers.soundfont.SoundFontImporterGUI;
import com.frinika.synth.synths.sampler.SamplerGUI;
import com.frinika.synth.synths.sampler.SamplerOscillator;
import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;
import com.frinika.synth.synths.sampler.settings.SamplerSettings;
import com.frinika.synth.synths.sampler.settings.sampledsoundsettingversions.SampledSound20050403;
import com.frinika.synth.synths.sampler.settings.samplersettingversions.Sampler20050227;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class MySampler extends Synth {
	protected HashMap<Integer,Vector<SoundFontOscillator>> exclusiveClasses = 
        new HashMap<Integer,Vector<SoundFontOscillator>>();
    public SoundFontImporter sfi;
	public SamplerOscillator samplerOscillator = new SamplerOscillator(this);
	public SamplerGUI gui;
	
	public SampledSoundSettings[][] sampledSounds = new SampledSoundSettings[200][128];
    
	public int recordMode;
	
    SamplerSettings samplerSettings = new Sampler20050227();
    
	static public final int RECORDMODE_SINGLE_KEY = 0;
	static public final int RECORDMODE_ALL_KEYS = 1;
	static public final int RECORDMODE_SELECTION = 2;
    
    public static final int SAMPLEMODE_NO_LOOP = 0;
    public static final int SAMPLEMODE_LOOP_CONTINOUSLY = 1;
    public static final int SAMPLEMODE_LOOP_UNTIL_RELEASE = 3;
	   
	public MySampler(SynthRack synth)
	{ 
		super(synth);
		sfi = new SoundFontImporter(this);
		getAudioOutput().addTransmitter(samplerOscillator);
	}
	
	public SoundFontImporter getImporter()
	{
		return(sfi);
	}
	
	public void insertSample(SampledSoundSettings snd, int noteNumber, int velocity)
	{
/*		if(!samplePool.contains(snd))
			samplePool.add(snd); */
		sampledSounds[noteNumber][velocity] = snd;
	}
	
	public void loadSettings(Serializable settings)
	{
		try
		{
			samplerSettings = (SamplerSettings)settings;
            setInstrumentName(samplerSettings.getInstrumentName());
            
			if(samplerSettings.getSoundFontName()!=null)
			{
				try
                {
                    sfi.getSoundFont(new java.io.File(samplerSettings.getSoundFontName()));
                       
                } catch(FileNotFoundException fne) {
                    SoundFontImporterGUI.getMissingSoundFont(new java.io.File(samplerSettings.getSoundFontName()),sfi);
                }
                
				System.out.println("InstrumentIndex: "+samplerSettings.getInstrumentIndex());
				sfi.getInstrument(samplerSettings.getInstrumentIndex());
			}
			else
			{
				sampledSounds = samplerSettings.getSampledSounds();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Serializable getSettings()
	{
		Sampler20050227 settings = new Sampler20050227();
		try
		{
			if(MySampler.this.getFrinikaSynth().isSaveReferencedData())
				throw new Exception("Also saving referenced data");

			settings.setSoundFontName(sfi.file.getAbsolutePath());
			settings.setInstrumentIndex(sfi.getInstrumentIndex());
            settings.setInstrumentName(getInstrumentName());
		}
		catch(Exception e)
		{	settings.setInstrumentName(getInstrumentName());
            settings.setSampledSounds(sampledSounds);
        }
        settings.setFreqSpread(samplerSettings.getFreqSpread());
        settings.setLayers(samplerSettings.getLayers());
		return(settings);
	}
    
    public SamplerSettings getSamplerSettings()
    {
        return samplerSettings;
    }
	
    /* (non-Javadoc)
     * @see com.petersalomonsen.mystudio.mysynth.Synth#close()
     */
    public void close() {
        super.close();
        getAudioOutput().removeTransmitter(samplerOscillator);
    }
    
	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#noteOn(int, int)
	 */
	public synchronized void noteOn(int noteNumber, int velocity) { 
		

		if( samplerOscillator.isMonitoring() && 
			!samplerOscillator.isRecording())
		{
			switch(recordMode)
			{
				case RECORDMODE_SELECTION:
					if(!gui.isNoteInSelection(noteNumber,velocity))
						break;
				default:
					samplerOscillator.startRecording();
					Oscillator osc = new SamplingOscillator(this,noteNumber,velocity);
					addOscillator(noteNumber,osc);
			}
		}
		else
		{
            switch(samplerSettings.getLayers())
            {
                case 2:
                    SoundFontOscillator osc = new SoundFontOscillator(this,noteNumber,velocity);
                    osc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread(),0);
                    osc.setVelocity(velocity);
                    addOscillator(noteNumber,osc);
            
                    SoundFontOscillator layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread() ,1);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    
                    break;
                case 3:
                    osc = new SoundFontOscillator(this,noteNumber,velocity);
                    osc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread(),0);
                    osc.setVelocity(velocity);
                    addOscillator(noteNumber,osc);
            
                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,0,0.5f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    
                    
                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread() ,1);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    
                    break;

                case 4:
                    osc = new SoundFontOscillator(this,noteNumber,velocity);
                    osc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread(),0);
                    osc.setVelocity(velocity);
                    addOscillator(noteNumber,osc);
            
                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread()*0.33f,0.33f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    

                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread()*0.33f,0.66f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    

                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread() ,1);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    
                    break;

                case 5:
                    osc = new SoundFontOscillator(this,noteNumber,velocity);
                    osc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread(),0);
                    osc.setVelocity(velocity);
                    addOscillator(noteNumber,osc);
            
                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread()*0.33f,0.33f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    

                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,0,0.5f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    

                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread()*0.33f,0.66f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    

                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread() ,1);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    
                    break;
                case 6:
                    osc = new SoundFontOscillator(this,noteNumber,velocity);
                    osc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread(),0);
                    osc.setVelocity(velocity);
                    addOscillator(noteNumber,osc);
            
                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread()*0.60f,0.33f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    
                    
                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,-samplerSettings.getFreqSpread()*0.20f,0.33f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    

                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread()*0.20f,0.66f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    

                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread()*0.60f,0.66f);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    

                    layerOsc = new SoundFontOscillator(this,noteNumber,velocity);
                    layerOsc.setNoteNumber(noteNumber,samplerSettings.getFreqSpread() ,1);
                    layerOsc.setVelocity(velocity);
                    osc.addLayer(layerOsc);                    
                    break;

                default:
                    osc = new SoundFontOscillator(this,noteNumber,velocity);
                    osc.setNoteNumber(noteNumber,0,0.5f);
                    osc.setVelocity(velocity);
                    addOscillator(noteNumber,osc);
            }
		}
	}
	
	/* (non-Javadoc)
	 * @see com.petersalomonsen.mystudio.mysynth.Synth#showGUI()
	 */
	public void showGUI() {
		gui = new SamplerGUI(this);
	}
	
	public class SamplingOscillator extends Oscillator{
		int velocity;
		int noteNumber;
		boolean releaseTriggered = false;
        
		public SamplingOscillator(Synth synth, int noteNumber, int velocity)
		{
			super(synth);
			System.out.println(velocity);
			this.velocity = velocity;
			this.noteNumber = noteNumber;
		}
		
        /* (non-Javadoc)
         * @see com.petersalomonsen.mystudio.mysynth.Oscillator#release()
         */
        public void release() {
            if(!releaseTriggered)
            {
                releaseTriggered = true;
                SampledSoundSettings sampledSound = new SampledSound20050403();

                short[][] samples = ((MySampler)synth).samplerOscillator.stopRecording();
                sampledSound.setLeftSamples(samples[0]);
                sampledSound.setRightSamples(samples[1]==null ? samples[0] : samples[1]);
                sampledSound.setRootKey(noteNumber);
                sampledSound.setSampleRate((int) FrinikaConfig.sampleRate);
                java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sampledSound.setSampleName(dateFormat.format(new java.util.Date()));
                switch(recordMode)
                {
                    case RECORDMODE_SINGLE_KEY:                 
                        for(int v=0;v<128;v++)
                                    ((MySampler)synth).insertSample(sampledSound,noteNumber,v);
                        break;
                    case RECORDMODE_ALL_KEYS:
                        for(int n = 0;n<96;n++)
                            for(int v=0;v<128;v++)
                                ((MySampler)synth).insertSample(sampledSound,n,v);                  
                        break;
                    case RECORDMODE_SELECTION:
                        gui.insertSampleToSelection(sampledSound);
                        break;
                }
    
                ((MySampler)synth).samplerOscillator.stopMonitor();
                System.out.println("Record stored");
                getAudioOutput().removeTransmitter(this);
            }
        }

        /* (non-Javadoc)
		 * @see com.petersalomonsen.mystudio.audio.IAudioOutputGenerator#fillBuffer(int, int, float[])
		 */
		public final void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
        
		}
	}
	
	public class SoundFontOscillator extends Oscillator{
		SampledSoundSettings sampledSound;
		
		float samplesPerPeriod = (float)871.74;
		float samplePos = 0;
		float sampleIncrement;
		float dBFactor = 1;
		int velocity;
		int noteNumber;
		
		float attenuationPerSample = (float)0.1;

        short[] leftSamples;
        short[] rightSamples;
        
        int loopStart;
        int loopEnd;
        int sampleMode;
        
        float leftLevel;
        float rightLevel;
        VolumeEnvelope envelope = new VolumeEnvelope(sampleRate,-100f,0);
        
        Vector<SoundFontOscillator> layers = new Vector<SoundFontOscillator>();
        
		public SoundFontOscillator(Synth synth, int noteNumber, int velocity)
		{
			super(synth);
			this.velocity = velocity;
			this.noteNumber = noteNumber;
		}
		
        public void addLayer(SoundFontOscillator osc)
        {
            layers.add(osc);
        }
        
        public void terminate()
        {
            //Force immediate release
            attenuationPerSample = envelope.getAttenuationPerSample(-100,-32768);
            release();
        }
        
		/* (non-Javadoc)
		 * @see com.petersalomonsen.mystudio.mysynth.OscillatorAdapter#setNoteNumber(int)
		 */
		public void setNoteNumber(int noteNumber, float customFineTune, float customPan) {
			this.noteNumber = noteNumber;
			sampledSound = sampledSounds[noteNumber][velocity];
			
            leftSamples = sampledSound.getLeftSamples();
            rightSamples = sampledSound.getRightSamples();
            loopStart = sampledSound.getLoopStart();
            loopEnd = sampledSound.getLoopEnd();
            sampleMode = sampledSound.getSampleMode();
            
            attenuationPerSample = envelope.getAttenuationPerSample(-100,sampledSound.getRelease()); 

            float rootFreq = getFrequency(sampledSound.getRootKey());
			samplesPerPeriod = (sampledSound.getSampleRate()/rootFreq) * PitchCents.getPitchCent(sampledSound.getFineTune()) * PitchCents.getRealPitchCent(customFineTune);
			
			frequency = rootFreq + ((getFrequency(noteNumber)-rootFreq) * (sampledSound.getScaleTune()/100f));
            updateIncrement();
            
            sampleIncrement = (float)(( increment / (2*Math.PI))*samplesPerPeriod);
            
            Pan pan = new Pan(customPan);
            leftLevel = pan.getLeftLevel();
            rightLevel = pan.getRightLevel();
            
            if(sampledSound.getExclusiveClass()>0)
            {
                int exclusiveClass = sampledSound.getExclusiveClass();
                if(exclusiveClasses.containsKey(exclusiveClass))
                {
                    Vector<SoundFontOscillator>exOscillators =
                        exclusiveClasses.get(exclusiveClass);
                    
                    if(exOscillators.get(0).noteNumber == noteNumber)
                        exOscillators.add(this);
                    else
                    {
                        for(SoundFontOscillator exOscillator : exOscillators)
                            exOscillator.terminate();
                        exclusiveClasses.remove(exclusiveClass);
                    }
                }
                if(!exclusiveClasses.containsKey(exclusiveClass))
                {
                    Vector<SoundFontOscillator> exOscillators = new Vector<SoundFontOscillator>();
                    exOscillators.add(this);
                    exclusiveClasses.put(exclusiveClass,exOscillators);
                }
            }
		}
					
		public final void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
			for(int n=startBufferPos;n<endBufferPos;)
			{
				float left=0;
				float right=0;
	
				if(samplePos<(rightSamples.length-1))
				{			
					//linear interpolation
					int x1 = (int)samplePos;
					int x2 = x1+1;
					float posMinusX1 = samplePos - x1;
					short y1 = rightSamples[x1];
				
					float dydx = (rightSamples[x2] - y1);
					right = (((dydx * posMinusX1 + y1) * level) / 32768f);
									
					y1 = leftSamples[x1];
					dydx = (leftSamples[x2] - y1);
				
					left = (((dydx * posMinusX1 + y1) * level) / 32768f);
				
					if(dBFactor > 0.01)
					{
					 
						left *= dBFactor * leftLevel;
						right *= dBFactor  * rightLevel;
                                                
						if(release)
							dBFactor *= attenuationPerSample;

						samplePos+=(sampleIncrement * preOscillator.getLfoBuffer()[n / 2]);

						preOscillator.sampleBuffer[n++]+=left;
						preOscillator.sampleBuffer[n++]+=right;

						// Handle looping 

						if(sampleMode == SAMPLEMODE_LOOP_CONTINOUSLY && 
								samplePos>=loopEnd)
							samplePos = loopStart + (samplePos-loopEnd);
						
					}
					else
					{
						getAudioOutput().removeTransmitter(this);
						break;
					}
				}
				else
				{
					getAudioOutput().removeTransmitter(this);
					break;
				}
			}

            // Convert to array to avoid concurrent mod exception (don't use synchronized to avoid blocking)
            for(Object o : layers.toArray())
            {
                SoundFontOscillator osc = (SoundFontOscillator)o;
                osc.release = this.release;
                osc.fillBuffer(startBufferPos,endBufferPos,buffer);
            }
        }
	}
	/**
	 * 
	 */
	public String toString() {
		return "MS: "+getInstrumentName();
	}


}

