// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.synths.plucked;

import java.util.List;

import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.core.ChannelFormat;
import uk.org.toot.audio.system.AudioOutput;
import uk.org.toot.dsp.jSTK.instrument.PluckedString;
import uk.org.toot.synth.BasicMidiSynth;
import uk.org.toot.synth.SynthChannel;

/**
 * This class models a plucked string instrument.
 * Each string has its own channel to allow different strings to play the same notes.
 * @author st
 *
 */
public class PluckedSynth extends BasicMidiSynth implements AudioOutput
{
	private PluckedSynthControls controls;
	private AudioBuffer.MetaInfo info;
	private List<PluckedStringChannel> strings = 
		new java.util.ArrayList<PluckedStringChannel>();
	
	public PluckedSynth(PluckedSynthControls controls) {
		super(controls.getName());
		addAudioOutput(this);
		this.controls = controls;
		int nstrings = controls.getStringCount();
		for ( int i = 0; i < nstrings; i++ ) {
			PluckedStringChannel string = 
				new PluckedStringChannel(controls.getLowestFrequency(i)); 
			setChannel(i, string);
			strings.add(string);
		}
	}

	public void setLocation(String location) {	
        info = new AudioBuffer.MetaInfo(getName(), location);
        super.setLocation(location);
	}

	public void open() throws Exception {
	}

	public int processAudio(AudioBuffer buffer) {
	    buffer.setMetaInfo(info);
	    buffer.setChannelFormat(ChannelFormat.MONO);
		buffer.makeSilence();
		
		for ( PluckedStringChannel string : strings  ) {
			string.mix(buffer);
		}
		
		return AudioProcess.AUDIO_OK;
	}

	public void close() {
	}
	
	protected class PluckedStringChannel extends SynthChannel
	{
		private PluckedString string;
		private float frequency;
		private float bendFactor = 1f;
		private float ampTracking;
		
		public PluckedStringChannel(float fLow) {
			string = new PluckedString(fLow);
		}
		
		public boolean mix(AudioBuffer buffer) {
			float level = controls.getLevel() * 12f;
			ampTracking = controls.getVelocityTrack();
			float bf = getBendFactor();
			if ( bendFactor != bf ) {
				string.setFrequency(frequency * bf);
				bendFactor = bf;
			}
			float[] samples = buffer.getChannel(0);
			int nsamples = buffer.getSampleCount();
			for ( int i = 0; i < nsamples; i++ ) {
				samples[i] += level * string.getSample();
			}
			return false; // !!!
		}
		
		@Override
		public void noteOn(int pitch, int velocity) {
			frequency = midiFreq(pitch);
			float amplitude = (float)velocity / 128;
			float ampT = velocity == 0 ? 0f : (1 - ampTracking * (1 - amplitude));
			string.noteOn(frequency, ampT, amplitude, controls.getPick());
		}
		
		@Override
		public void noteOff(int pitch) {
			string.noteOff(0.1f); // !!!
		}

		@Override
		public void allNotesOff() {
			string.noteOff(0.1f); // !!!
		}

		@Override
		public void allSoundOff() {
			// TODO Auto-generated method stub
			
		}

		public void setLocation(String location) {
			// we don't need a location
		}		
	}
}
