/*
 * Created on 5.3.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

package com.frinika.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.FrinikaTrackWrapper;
import com.frinika.sequencer.model.MidiPlayOptions;

class TrackIterator
{
	FrinikaTrackWrapper track;
	boolean used;
	int pos = 0;
	MidiMessage nextevent = null;
	long nexttick = -1;	
	MidiPlayOptions opt;
	
	public boolean isUsed()
	{
		return used;
	}
	
	public boolean hasNext()
	{
		return nextevent != null;
	}
	
	public long nextTick()
	{
		return nexttick;
	}
	
	public MidiMessage next()
	{
		MidiMessage event = nextevent;
		pos++;
		if(pos < track.size()) 
		{
			nextevent = track.get(pos).getMessage();
			nexttick = track.get(pos).getTick();
		}
		else
		{
			nextevent = null;
			nexttick = -1;
		}
		return event;
	}
}

public class FrinikaMidiPacketProvider implements MidiPacketProvider {

	FrinikaSequence seq;	
	TrackIterator[] tracks_iterator;
	long packetlen;
	int seqres; 
	
	int midi_channel = 0;
	
	public FrinikaMidiPacketProvider(long packetlen, FrinikaSequencer seqr, FrinikaSequence seq, Collection<FrinikaTrackWrapper> tracks)
	{
		this.seq = seq;
		this.packetlen = packetlen;
		seqres = seq.getResolution();
		
		tempo = seqr.getTempoInBPM();

		ArrayList<FrinikaTrackWrapper> seqtracklist = new ArrayList<FrinikaTrackWrapper>();
		Collection<FrinikaTrackWrapper> seqtracks;
        if(seqr.getSoloFrinikaTrackWrappers().size()>0)
        	seqtracks = seqr.getSoloFrinikaTrackWrappers(); 
        else
        	seqtracks = seq.getFrinikaTrackWrappers();
		
		for(FrinikaTrackWrapper track : seqtracks)
		{
		if(track.getMidiDevice() != null)
		{
			MidiPlayOptions opt = seqr.getPlayOptions(track);
			if(opt != null)
			if(opt.muted)
			{
				continue;
			}
			seqtracklist.add(track);
		}		
		}
		
		this.tracks_iterator = new TrackIterator[seqtracklist.size()];		
		for (int i = 0; i < this.tracks_iterator.length; i++) {
			FrinikaTrackWrapper track = seqtracklist.get(i);
			this.tracks_iterator[i] = new TrackIterator();
			this.tracks_iterator[i].track = track;
			this.tracks_iterator[i].used = tracks.contains(track);
			this.tracks_iterator[i].opt = seqr.getPlayOptions(track);
			if(track.size() != 0)
			{
				this.tracks_iterator[i].nextevent = track.get(0).getMessage();
				this.tracks_iterator[i].nexttick = track.get(0).getTick();
			}
			
			if(this.tracks_iterator[i].used)
				if (track.getMidiChannel() != FrinikaTrackWrapper.CHANNEL_FROM_EVENT) {
					midi_channel = track.getMidiChannel();
				}
			
		}
		
		readNextEvent();
		
	}
	
	float tempo = 100; // in BPM
	
	MidiMessage current_msg = null;
	long current_tick_pos;
	long current_event_pos; // In microsecond
	
	public MidiMessage nextEvent()
	{
		while(true)
		{
			long nexttick = 0;
			TrackIterator sel_iterator = null;
			for(TrackIterator track_iterator : tracks_iterator)
			if(track_iterator.hasNext())
			if(sel_iterator == null || track_iterator.nextTick() < nexttick)
			{
				sel_iterator = track_iterator;
				nexttick = track_iterator.nextTick();
			}		
			if(sel_iterator == null) return null;
			
			long tick = sel_iterator.nextTick();
			MidiMessage event = sel_iterator.next();
			
			if(tick != current_tick_pos)
			{
				long tickdiff = tick - current_tick_pos;
				long timediff = (long)( tickdiff * (60000000f/(tempo*seqres)) );				
				current_tick_pos = tick;
				current_event_pos += timediff;
			}
			
			byte[] msgBytes = event.getMessage();
			if (msgBytes[0] == -1 && msgBytes[1] == 0x51
					&& msgBytes[2] == 3) {
				int mpq = ((msgBytes[3] & 0xff) << 16)
						| ((msgBytes[4] & 0xff) << 8)
						| (msgBytes[5] & 0xff);
				
				// pjl removed cast to int for tempo
				tempo = (60000000f / mpq);				               
			}
			 
			if(sel_iterator.used)
			{
				if(event instanceof ShortMessage)
				{									
					return processMessage(sel_iterator.track, sel_iterator.opt, (ShortMessage)event);
				}
			}
		}
	}
	
	public void readNextEvent()
	{
		current_msg = nextEvent();
	}
	
	public void seek(int index)
	{				
		if(index < (current_index-1))
		{
			current_index = -1;
		}
		while(index > (current_index+1))
		{
			if(current_msg == null) return;									
			while(current_event_pos < packetlen)
			{
				updateStatus(current_msg);
				readNextEvent();
			}
			current_event_pos -= packetlen;
			current_index++;
		}
	}
	
	public LinkedList<Integer> activenotes = new LinkedList<Integer>();
	public LinkedList<Integer> activenotes_velocity = new LinkedList<Integer>();
	public LinkedList<Integer> controls = new LinkedList<Integer>();
	public LinkedList<Integer> controls_values = new LinkedList<Integer>();
	public int program = -1;
	public int pitchbend_data1 = -1;
	public int pitchbend_data2 = -1;	
	
	public MidiMessage processMessage(FrinikaTrackWrapper track, MidiPlayOptions opt, ShortMessage message)
	{
		if(opt == null) return message;
		byte[] msgBytes =  message.getMessage();	
		
		int ch = message.getChannel();
		
        if (track.getMidiChannel() != FrinikaTrackWrapper.CHANNEL_FROM_EVENT) {
        	ch = track.getMidiChannel();
        }        
		
		if ( (msgBytes.length > 2) && (((msgBytes[0] & 0xf0) == ShortMessage.NOTE_OFF || (msgBytes[0] & 0xf0) == ShortMessage.NOTE_ON ) &&  (
				(opt.transpose != 0) || (opt.velocityOffset != 0) || (opt.velocityCompression != 0.0f) 
				)) ) {
				// need to do some on-the-fly modifications of the event
				int note = msgBytes[1];
				note += opt.transpose;
				if (note < 0) {
					note = 0;
				} else if (note > 127) {
					note = 127;
				}
				int vel = msgBytes[2];
				if(vel != 0)
				{
				if (opt.velocityCompression != 0.0f) {
					float diff = (64 - vel) * opt.velocityCompression; 
					vel += diff;
				}
				vel += opt.velocityOffset;
				if (vel < 1) {
					vel = 1;
				} else if (vel > 127) {
					vel = 127;
				}
				}				
				ShortMessage shm = new ShortMessage();
				try {
					shm.setMessage(message.getCommand(), ch, note, vel);
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}
				return shm;
			} else { // normalsend
				ShortMessage shm = new ShortMessage();
				try {
					shm.setMessage(message.getCommand(), ch, message.getData1(), message.getData2());
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}				
				return shm;
			}

		
		
	}
	
	public void updateStatus(MidiMessage msg)
	{
		if(msg instanceof ShortMessage)
		{
			ShortMessage sms = (ShortMessage)msg;
			
			switch (sms.getCommand()) {
			case ShortMessage.NOTE_ON:
				
				int ix = activenotes.indexOf(new Integer(sms.getData1()));				
				if(ix != -1)
				{
					activenotes.remove(ix);
					activenotes_velocity.remove(ix);
				}
				if(sms.getData2()>0)
				{
					activenotes.add(new Integer(sms.getData1()));
					activenotes_velocity.add(new Integer(sms.getData2()));
				}
				break;
			case ShortMessage.NOTE_OFF:
				ix = activenotes.indexOf(new Integer(sms.getData1()));				
				if(ix != -1)
				{
					activenotes.remove(ix);
					activenotes_velocity.remove(ix);
				}
				break;
			case ShortMessage.PROGRAM_CHANGE:
				program = sms.getData1();
				break;
			case ShortMessage.PITCH_BEND:
				pitchbend_data1 = sms.getData1();
				pitchbend_data2 = sms.getData2();
				break;
			case ShortMessage.CONTROL_CHANGE:
				
				ix = controls.indexOf(new Integer(sms.getData1()));
				if(ix != -1)
				{
					controls.remove(ix);
					controls_values.remove(ix);
				}
				controls.add(sms.getData1());
				controls_values.add(sms.getData2());
				break;
				
			default:
				break;
			}
		}
	}
	public MidiPacket createPacket()
	{
		MidiPacket packet = new MidiPacket();
		
		Iterator<Integer> iter;
		
		packet.activenotes = new int[activenotes.size()];
		iter = activenotes.iterator();
		for (int i = 0; i < packet.activenotes.length; i++) {
			packet.activenotes[i] = iter.next();
		}
		
		packet.activenotes_velocity = new int[activenotes_velocity.size()];
		iter = activenotes_velocity.iterator();
		for (int i = 0; i < packet.activenotes_velocity.length; i++) {
			packet.activenotes_velocity[i] = iter.next();
		}

		packet.controls = new int[controls.size()];
		iter = controls.iterator();
		for (int i = 0; i < packet.controls.length; i++) {
			packet.controls[i] = iter.next();
		}

		packet.controls_values = new int[controls_values.size()];
		iter = controls_values.iterator();
		for (int i = 0; i < packet.controls_values.length; i++) {
			packet.controls_values[i] = iter.next();
		}
		
		packet.program = program;
		packet.pitchbend_data1 = pitchbend_data1;
		packet.pitchbend_data2 = pitchbend_data2;
				
		return packet;
	}
	public MidiPacket next()
	{
		if(current_msg == null) return null;
		
		MidiPacket packet = createPacket();
		
		ArrayList<MidiEvent> events = new ArrayList<MidiEvent>();
		ShortMessage msg1 = new ShortMessage();
		ShortMessage msg2 = new ShortMessage();
		
		//System.out.println("-------------------------------------------------------------------------");
		
		while(current_event_pos < packetlen)
		{			
			if(current_msg == null) break;
			updateStatus(current_msg);
		/*
			String cmd = "" + ((ShortMessage)current_msg).getCommand();
			if(((ShortMessage)current_msg).getCommand() == ShortMessage.PROGRAM_CHANGE) cmd = "PROGRAM";
			if(((ShortMessage)current_msg).getCommand() == ShortMessage.NOTE_OFF) cmd = "NOTE_OFF";
			if(((ShortMessage)current_msg).getCommand() == ShortMessage.NOTE_ON) cmd = "NOTE_ON";
			if(((ShortMessage)current_msg).getCommand() == ShortMessage.CONTROL_CHANGE) cmd = "CONTROL";
			if(((ShortMessage)current_msg).getCommand() == ShortMessage.PITCH_BEND) cmd = "PITCH";
			System.out.println(current_event_pos + " " + ((ShortMessage)current_msg).getChannel() + "." + cmd 
					                            + "(" + ((ShortMessage)current_msg).getData1() + " , " + ((ShortMessage)current_msg).getData2() + ")");
								                  */
			events.add(new MidiEvent(current_msg, current_event_pos));
			readNextEvent();
		}
		current_event_pos -= packetlen;
		packet.events = new MidiEvent[events.size()];
		events.toArray(packet.events);
		        
        packet.channel = midi_channel;
		
		current_index++;
		
		return packet;
	}
	
	int current_index = -1;
	public MidiPacket get(int index)
	{
		if((current_index+1 != index))
			seek(index);
		return next();
	}
	
}
