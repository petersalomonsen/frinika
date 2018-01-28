// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

import java.util.Collections;
import java.util.List;

import javax.sound.midi.MidiMessage;

import uk.org.toot.audio.system.AudioInput;
import uk.org.toot.audio.system.AudioOutput;
import uk.org.toot.midi.core.AbstractMidiDevice;
import uk.org.toot.midi.message.*;

import static uk.org.toot.midi.message.ChannelMsg.*;
import static uk.org.toot.midi.misc.Controller.*;

/**
 * A BasicMidiSynth is a MidiSynth with 16 SynthChannels which may be set by the user.
 * So it is multitimbral and each SynthChannel may be a different implementation,
 * think a multitimbral synth with channel 1 a TX81Z, channel 2 a Moog etc.
 * @author st
 */
abstract public class BasicMidiSynth extends AbstractMidiDevice implements MidiSynth
{
	private List<AudioOutput> audioOutputs;
	
    /**
     * @link aggregationByValue
     * @supplierCardinality 16 
     */
	private SynthChannel[] synthChannels = new SynthChannel[16];
	private String location = "?";

	
	public BasicMidiSynth(String name) {
		super(name);
		addMidiInput(this);
		audioOutputs = new java.util.ArrayList<AudioOutput>();
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getLocation() {
		return location;
	}
	
	protected void setChannel(int chan, SynthChannel synthChannel) {
		synthChannels[chan] = synthChannel;
	}

	public SynthChannel[] getChannels() {
		return synthChannels; 
	}

	public SynthChannel getChannel(int chan) {
		return synthChannels[chan];
	}
	
	public void transport(MidiMessage msg, long timestamp) {
		if ( isChannel(msg) ) {
			int chan = ChannelMsg.getChannel(msg);
			SynthChannel synthChannel = synthChannels[chan];
			if ( synthChannel == null ) return;
			if ( NoteMsg.isNote(msg) ) {
				int pitch = NoteMsg.getPitch(msg);
				int velocity = NoteMsg.getVelocity(msg);
				boolean on = NoteMsg.isOn(msg);
				if ( on ) {
					synthChannel.noteOn(pitch, velocity);
				} else {
					synthChannel.noteOff(pitch, velocity);
				}
			} else {
				int cmd = getCommand(msg);
				switch ( cmd ) {
				case PITCH_BEND:
					synthChannel.setPitchBend(getData1and2(msg));
					break;
				case CONTROL_CHANGE:
					int controller = getData1(msg);
					if ( controller == ALL_CONTROLLERS_OFF ) {
						synthChannel.resetAllControllers();
					} else if ( controller == ALL_NOTES_OFF ) {
						synthChannel.allNotesOff();
					} else if ( controller == ALL_SOUND_OFF ) {
						synthChannel.allSoundOff();
					} else {
						synthChannel.controlChange(controller, getData2(msg));
					}
					break;
				case ChannelMsg.CHANNEL_PRESSURE:
					synthChannel.setChannelPressure(getData1(msg));
					break;
				}
			}
		}
	}
	
	public void closeMidi() {}
	
    protected void addAudioOutput(AudioOutput output) {
        audioOutputs.add(output);
        setChanged();
        notifyObservers(output);
    }

    protected void removeAudioOutput(AudioOutput output) {
        audioOutputs.remove(output);
        setChanged();
        notifyObservers(output);
    }

	public List<AudioOutput> getAudioOutputs() {
        return Collections.unmodifiableList(audioOutputs);
	}
	
	public List<AudioInput> getAudioInputs() {
		return Collections.emptyList();
	}
	
	public void closeAudio() {	
	}
	
}
