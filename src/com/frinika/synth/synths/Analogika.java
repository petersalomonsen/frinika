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
import java.io.Serializable;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import com.frinika.voiceserver.VoiceServer;
import com.frinika.audio.Decibel;
import com.frinika.synth.*;
import com.frinika.synth.envelope.VolumeEnvelope;
import com.frinika.synth.filters.LoPass;
import com.frinika.synth.synths.analogika.AnalogikaGUI;
import com.frinika.synth.synths.analogika.settings.AnalogikaSettings;
import com.frinika.synth.synths.analogika.settings.analogikasettingsversions.AnalogikaSettings20050303;
/**
 * @author Peter Johan Salomonsen
 *
 */
public final class Analogika extends Synth {
    
    static final float panLeft(final double position)
    {
        return Decibel.getAmplitudeRatio(-(float)(-20f*Math.log10(Math.sqrt(1-position))));
    }
    
    static final float panRight(final double position)
    {
        return Decibel.getAmplitudeRatio(-(float)(-20f*Math.log10(Math.sqrt(position))));
    }

    static final float PAN_LEFT_0 = panLeft(0.00);
    static final float PAN_LEFT_15 = panLeft(0.15);
    static final float PAN_LEFT_20 = panLeft(0.2);
    static final float PAN_LEFT_25 = panLeft(0.25);
    static final float PAN_LEFT_30 = panLeft(0.30);
    static final float PAN_LEFT_40 = panLeft(0.40);
    static final float PAN_LEFT_45 = panLeft(0.45);
    static final float PAN_LEFT_50 = panLeft(0.5);
    static final float PAN_LEFT_55 = panLeft(0.55);
    static final float PAN_LEFT_60 = panLeft(0.60);
    static final float PAN_LEFT_70 = panLeft(0.70);
    static final float PAN_LEFT_75 = panLeft(0.75);
    static final float PAN_LEFT_80 = panLeft(0.80);
    static final float PAN_LEFT_85 = panLeft(0.85);

    static final float PAN_RIGHT_15 = panRight(0.15);
    static final float PAN_RIGHT_20 = panRight(0.2);
    static final float PAN_RIGHT_25 = panRight(0.25);
    static final float PAN_RIGHT_30 = panRight(0.30);
    static final float PAN_RIGHT_40 = panRight(0.40);
    static final float PAN_RIGHT_45 = panRight(0.45);
    static final float PAN_RIGHT_50 = panRight(0.5);
    static final float PAN_RIGHT_55 = panRight(0.55);
    static final float PAN_RIGHT_60 = panRight(0.60);
    static final float PAN_RIGHT_70 = panRight(0.70);
    static final float PAN_RIGHT_75 = panRight(0.75);
    static final float PAN_RIGHT_80 = panRight(0.80);
    static final float PAN_RIGHT_85 = panRight(0.85);
    static final float PAN_RIGHT_100 = panRight(1.0);
    
    AnalogikaSettings20050303 settings = new AnalogikaSettings20050303();
    
	/**
	 * @param voiceServer
	 */
	public Analogika(SynthRack synth) {
		super(synth);
	}
    
    
    public AnalogikaSettings getAnalogikaSettings()
    {
        return settings;
    }
    
    @Override
    public void loadSettings(Serializable settings) {
        AnalogikaSettings newSettings = (AnalogikaSettings)settings;
        this.settings.setFreqSpread(newSettings.getFreqSpread());
        this.settings.setLayers(newSettings.getLayers());
        this.settings.setLoPassAttack(newSettings.getLoPassAttack());
        this.settings.setLoPassDecay(newSettings.getLoPassDecay());
        this.settings.setLoPassSustain(newSettings.getLoPassSustain());
        this.settings.setLoPassRelease(newSettings.getLoPassRelease());
        this.settings.setLoPassMax(newSettings.getLoPassMax());
        this.settings.setVolAttack(newSettings.getVolAttack());
        this.settings.setVolDecay(newSettings.getVolDecay());
        this.settings.setVolSustain(newSettings.getVolSustain());
        this.settings.setVolRelease(newSettings.getVolRelease());
        this.settings.setWaveform(newSettings.getWaveform());
        if(newSettings.getInstrumentName()==null)
            setInstrumentName("analogika");
        else
            setInstrumentName(newSettings.getInstrumentName()); 
    }
    
    @Override
    public Serializable getSettings() {
        this.settings.setInstrumentName(getInstrumentName());
        return settings;
    }
    
	/* (non-Javadoc)
	 * @see javax.sound.midi.MidiChannel#noteOn(int, int)
	 */
	public void noteOn(int noteNumber, int velocity) { 
		Oscillator osc = new AnalogikaOscillator(this);
		osc.setNoteNumber(noteNumber);
		osc.setVelocity(velocity);
		addOscillator(noteNumber,osc);
	}
	
    /* (non-Javadoc)
     * @see com.petersalomonsen.mystudio.mysynth.Synth#showGUI()
     */
    public void showGUI() {
        new AnalogikaGUI(this);
    }

    final class AnalogikaOscillator extends Oscillator {
        final LoPass leftLoPass = new LoPass();
        final LoPass rightLoPass = new LoPass();

        final VolumeEnvelope volEnvelope = new VolumeEnvelope(getAudioOutput().getSampleRate(),-100f,0f);
        final VolumeEnvelope loPassEnvelope = new VolumeEnvelope(getAudioOutput().getSampleRate(),-100f,-(settings.getLoPassMax()/10f));
        
        final int layers = settings.getLayers();
        final float freqSpread = settings.getFreqSpread();
        final float[] waveform = settings.getWaveform();
        final float positionMultiplier = (float)(waveform.length / (2 * Math.PI));
        final float layerMiddle = layers/2f;
        final float freqBase = 1f+((0.5f-layerMiddle)/layerMiddle)*freqSpread;
        final float spreadMiddle = freqSpread / layerMiddle;
        
		/**
		 * @param synth
		 */
		public AnalogikaOscillator(Synth synth) {
			super(synth);
            volEnvelope.setAttack(settings.getVolAttack());
            volEnvelope.setDecay(settings.getVolDecay());
            volEnvelope.setSustain(settings.getVolSustain());
            volEnvelope.setRelease(settings.getVolRelease());
            loPassEnvelope.setAttack(settings.getLoPassAttack());
            loPassEnvelope.setDecay(settings.getLoPassDecay());
            loPassEnvelope.setSustain(settings.getLoPassSustain());
            loPassEnvelope.setRelease(settings.getLoPassRelease());
        }        
        
        final float getSample(final float layerIndex)
        {
            return waveform[(int)(position * (layerIndex*spreadMiddle + freqBase)) % waveform.length];
        }
                
        
		public final void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
		    if(release)
                volEnvelope.release();
            
            for(int n=startBufferPos;n<endBufferPos;)
			{
                float left = 0;
                float right = 0;
                float tmpSample;

                switch(layers)
                {
                    case 1:
                        left=getSample(0) * PAN_LEFT_50;
                        right=left;
                        break;
                    case 2:
                        left=getSample(0) * PAN_LEFT_0;
                        right=getSample(1) * PAN_RIGHT_100;
                        break;
                    case 3:
                        left=getSample(0) * PAN_LEFT_50;
                        right=left;
                        left+=getSample(1) * PAN_LEFT_0;
                        right+=getSample(2) * PAN_RIGHT_100;
                        break;
                    case 4:
                        left=getSample(0) * PAN_LEFT_0;
                        right=getSample(1) * PAN_RIGHT_100;
                        tmpSample = getSample(2);
                        left+=tmpSample * PAN_LEFT_25;
                        right+=tmpSample * PAN_RIGHT_25;
                        tmpSample = getSample(3);
                        left+=tmpSample * PAN_LEFT_75;
                        right+=tmpSample * PAN_RIGHT_75;
                        break;
                    case 5:
                        left=getSample(0) * PAN_LEFT_50;
                        right=left;
                        left+=getSample(1) * PAN_LEFT_0;
                        right+=getSample(2) * PAN_RIGHT_100;
                        tmpSample = getSample(3);
                        left+=tmpSample * PAN_LEFT_25;
                        right+=tmpSample * PAN_RIGHT_25;
                        tmpSample = getSample(4);
                        left+=tmpSample * PAN_LEFT_75;
                        right+=tmpSample * PAN_RIGHT_75;
                        break;
                    case 6:
                        left=getSample(0) * PAN_LEFT_0;
                        right=getSample(1) * PAN_RIGHT_100;
                        tmpSample = getSample(2);
                        left+=tmpSample * PAN_LEFT_20;
                        right+=tmpSample * PAN_RIGHT_20;
                        tmpSample = getSample(3);
                        left+=tmpSample * PAN_LEFT_80;
                        right+=tmpSample * PAN_RIGHT_80;
                        tmpSample = getSample(4);
                        left+=tmpSample * PAN_LEFT_40;
                        right+=tmpSample * PAN_RIGHT_40;
                        tmpSample = getSample(5);
                        left+=tmpSample * PAN_LEFT_60;
                        right+=tmpSample * PAN_RIGHT_60;
                        break;
                    case 7:
                        left=getSample(0) * PAN_LEFT_50;
                        right=left;
                        left+=getSample(1) * PAN_LEFT_0;
                        right+=getSample(2) * PAN_RIGHT_100;

                        tmpSample = getSample(3);
                        left+=tmpSample * PAN_LEFT_20;
                        right+=tmpSample * PAN_RIGHT_20;
                        tmpSample = getSample(4);
                        left+=tmpSample * PAN_LEFT_80;
                        right+=tmpSample * PAN_RIGHT_80;
                        
                        tmpSample = getSample(5);
                        left+=tmpSample * PAN_LEFT_40;
                        right+=tmpSample * PAN_RIGHT_40;
                        tmpSample = getSample(6);
                        left+=tmpSample * PAN_LEFT_60;
                        right+=tmpSample * PAN_RIGHT_60;
                        break;
                    case 8:
                        left=getSample(0) * PAN_LEFT_0;
                        right=getSample(1) * PAN_RIGHT_100;
                        
                        tmpSample = getSample(2);
                        left+=tmpSample * PAN_LEFT_15;
                        right+=tmpSample * PAN_RIGHT_15;
                        tmpSample = getSample(3);
                        left+=tmpSample * PAN_LEFT_85;
                        right+=tmpSample * PAN_RIGHT_85;
                        
                        tmpSample = getSample(4);
                        left+=tmpSample * PAN_LEFT_30;
                        right+=tmpSample * PAN_RIGHT_30;
                        tmpSample = getSample(5);
                        left+=tmpSample * PAN_LEFT_70;
                        right+=tmpSample * PAN_RIGHT_70;

                        tmpSample = getSample(6);
                        left+=tmpSample * PAN_LEFT_45;
                        right+=tmpSample * PAN_RIGHT_45;
                        tmpSample = getSample(7);
                        left+=tmpSample * PAN_LEFT_55;
                        right+=tmpSample * PAN_RIGHT_55;
                        break;
                }
                
                position+=increment * positionMultiplier * preOscillator.getLfoBuffer()[n / 2];
                
                float volEnvelopeAttenuation = volEnvelope.getAttenuation();
                float loPassEnvelopeSample = loPassEnvelope.getAttenuation();
                
                // Filter -----------------------
                
                leftLoPass.setCutOff(loPassEnvelopeSample);
                left = leftLoPass.filter(left);
                
                rightLoPass.setCutOff(loPassEnvelopeSample);
                right = rightLoPass.filter(right);    
                
/*                leftHiPass.setCutOff(hiFilterSustain);
                left = leftHiPass.filter(left) / (filterCompensate[127-hiMidiFilterSustain]);
                
                rightHiPass.setCutOff(hiFilterSustain) ;
                right = rightHiPass.filter(right) / (filterCompensate[127-hiMidiFilterSustain]);  

*/
                //-----------------------------------
                
                preOscillator.sampleBuffer[n++]+=left * level *  volEnvelopeAttenuation;
                preOscillator.sampleBuffer[n++]+=right * level * volEnvelopeAttenuation;
				                
				if(this.release && volEnvelopeAttenuation<0.01)
				{
                    getAudioOutput().removeTransmitter(this);
					break;
				}
			}
		}
	}
 
    // For direct testing
    
    public static void main(String[] args) throws Exception
    {
        final  VoiceServer audioOutput = new com.frinika.voiceserver.JavaSoundVoiceServer();
        final SynthRack synth = new SynthRack(audioOutput);
        synth.open();
        synth.setSynth(0,new Analogika(synth));
        System.out.println("Initializing midi, please wait...");
        MidiDevice midiIn = MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[0]);
        midiIn.open();
        
        class Recv implements Receiver
        {
            public void send(MidiMessage message, long timeStamp) {
                try
                {
                    ShortMessage shm = (ShortMessage)message;
                    shm.setMessage(shm.getCommand(),0,shm.getData1(),shm.getData2());
                    
                    synth.getReceiver().send(shm,-1);
                }
                catch(Exception e) {}
            }

            public void close() {                           
            }   
        }
        
        midiIn.getTransmitter().setReceiver(new Recv());
        System.out.println("Ready to play!!!");
        AnalogikaGUI gui = new AnalogikaGUI((Analogika)synth.getSynth(0));
        gui.setDefaultCloseOperation(AnalogikaGUI.EXIT_ON_CLOSE);
    }
    

	/**
	 * 
	 */
	public String toString() {
		//TODO count  ETC
		return "Analogika: "+getInstrumentName();
	}
}
