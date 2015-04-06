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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

public class MidiPacketsRenderer {
	
	MidiRender render;
	float samplerate;
	int channels;	
	double ms_factor;
	
	public MidiPacketsRenderer(MidiRenderFactory factory, float samplerate, int channels)
	{
		this(factory, samplerate, channels, null);
	}
	
	public MidiPacketsRenderer(MidiRenderFactory factory, float samplerate, int channels, MidiPacket init_packet)
	{
		this.render = factory.getRender(samplerate, channels);
		this.samplerate = samplerate;
		this.channels = channels;		
		ms_factor = samplerate / 1000000.0;  		
		if(init_packet != null)
		{
			
			if(init_packet.controls != null)
			for (int i = 0; i < init_packet.controls.length; i++) 
			{
				int control = init_packet.controls[i];
				int control_value = init_packet.controls_values[i];

				try {
					ShortMessage sms = new ShortMessage();
					sms.setMessage(ShortMessage.CONTROL_CHANGE, init_packet.channel, control, control_value);
					render.send(sms);				
				} catch (InvalidMidiDataException e) {}
				
			}
			
			if(init_packet.program != -1)
			{
				try {
					ShortMessage sms = new ShortMessage();
					sms.setMessage(ShortMessage.PROGRAM_CHANGE, init_packet.channel, init_packet.program, 0);
					render.send(sms);				
				} catch (InvalidMidiDataException e) {}
			}
			if(init_packet.pitchbend_data1 != -1)
			{
				try {
					ShortMessage sms = new ShortMessage();
					sms.setMessage(ShortMessage.PITCH_BEND, init_packet.channel, init_packet.pitchbend_data1, init_packet.pitchbend_data2);
					render.send(sms);				
				} catch (InvalidMidiDataException e) {}
			}			
		}
	}
	
	public void render(MidiPacket packet, float[] buffer, int start, int end)
	{
			
		int writepos = 0;
		int len = end - start;
		
		if(packet != null)
		{
		MidiEvent[] events = packet.events;
		if(events != null)
		{			
			for (int i = 0; i < events.length; i++) {
				MidiEvent event = events[i];
				int samplepos = ((int)(event.getTick()*ms_factor))*channels;
				if(samplepos != writepos && samplepos <= len)
				{
					// Render 500 points at max !!!
					while(writepos != samplepos)
					{
						if((samplepos - writepos) > 500)
						{
							render.read(buffer, writepos+start, writepos+500+start);
							writepos += 500;							
						}
						else
						{
							render.read(buffer, writepos+start, samplepos+start);
							writepos = samplepos;
						}
					}
					
				}
				render.send(event.getMessage());
			}
		}		
		}
		
		if(len != 0)
		{
		int samplepos = len;
		while(writepos != samplepos)
		{
			if((samplepos - writepos) > 500)
			{
				render.read(buffer, writepos+start, writepos+500+start);
				writepos += 500;							
			}
			else
			{
				render.read(buffer, writepos+start, samplepos+start);
				writepos = samplepos;
			}
		}		
		}
	}
	
	public void close()
	{		
		render.close();
	}
	
}
