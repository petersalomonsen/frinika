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

import java.util.Arrays;
import java.util.zip.CRC32;

import javax.sound.midi.MidiEvent;

/*
 *  This packet contains midi status information and events for only one midi channel for specific length 
 */
public class MidiPacket {
	
	public long length = 0; // length of packet in microseconds	
	public int channel = 0;
	
	// Status information
	public int[] activenotes = null;
	public int[] activenotes_velocity = null;
	public int[] controls = null;
	public int[] controls_values = null;
	public int program = -1;
	public int pitchbend_data1 = -1;
	public int pitchbend_data2 = -1;
	
	// Events inside the packet
	public MidiEvent[] events = null; // only that tick is a microsecond instead of midi tick		

	private boolean checksumset = false;
	private int checksum_value = 0;
	public int checksum() {
		if(checksumset) return checksum_value;
		
		CRC32 crc32 = new CRC32();

		crc32.update((byte)channel);		
		crc32.update((byte)program);
		
		crc32.update((byte)pitchbend_data1);  
		crc32.update((byte)pitchbend_data2);  
		
		for (int i = 0; i < events.length; i++) {
			crc32.update(events[i].getMessage().getMessage());  
		}

		if(controls != null)
		{
			int[] sorted = new int[controls.length];
			for (int i = 0; i < controls.length; i++) {
				sorted[i] = (controls[i] << 2) + controls_values[i];
			}
			Arrays.sort(sorted);		
			for (int i = 0; i < sorted.length; i++) {
				crc32.update((byte)(sorted[i] & 0xFF));
				crc32.update((byte)((sorted[i] & 0xFF00) >> 2));
			}			
		}
		
		if(activenotes != null)
		{
			int[] sorted = new int[activenotes.length];
			for (int i = 0; i < activenotes.length; i++) {
				sorted[i] = (activenotes[i] << 2) + activenotes_velocity[i];
			}
			Arrays.sort(sorted);		
			for (int i = 0; i < sorted.length; i++) {
				crc32.update((byte)(sorted[i] & 0xFF));
				crc32.update((byte)((sorted[i] & 0xFF00) >> 2));
			}				
		}				
				
		checksumset = true;
		checksum_value = (int)crc32.getValue();
		return checksum_value;
	}
		
	
}
