/*
 * Created on Feb 2, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

package com.frinika.sequencer.midi.sysex;

import com.frinika.sequencer.model.AbstractSysexMacro;

/**
 * Abstract superclass with tools methods for macro implementations specific
 * to some Roland devices.
 * 
 * Note that this implements a 3-byte size format for addresses and sizes,
 * devices that require 4-byte sizes will require additional work. 
 * 
 * @author Jens Gulden
 */
abstract class RolandSysexMacroAbstract extends AbstractSysexMacro {
	
	public final static byte DEVICE_ID_ROLAND = (byte)0x10;
	public final static byte COMMAND_SYSEX_SET = (byte)0x12;
	public final static byte COMMAND_SYSEX_REQUEST = (byte)0x11;

	public void checksum(byte[] data) {
		int l = data.length;
		byte c = calculateChecksum(data, 5, l-3);
		data[l-2] = c;
	}

	/**
	 * Creates sysex-data for sending a block of data to the device.
	 * The data will automatically converted to nibblized format,
	 * as internally required for transmission. 
	 * @param modelId
	 * @param addr
	 * @param data
	 * @return
	 */
	protected byte[] rolandSysexSet(byte modelId, int addr, byte[] data) {
		data = nibblize(data);
		return rolandSysexSetRaw(modelId, addr, data);
	}

	/**
	 * Creates sysex-data for sending a block of data to the device.
	 * The data is expected to already be in nibblized format, as required
	 * for transmission.
	 *  
	 * @param modelId
	 * @param addr
	 * @param data
	 * @return
	 */
	protected byte[] rolandSysexSetRaw(byte modelId, int addr, byte[] data) {
		byte[] a = new byte[10 + data.length];
		byte deviceId = DEVICE_ID_ROLAND;
		byte cmd = COMMAND_SYSEX_SET; 
	    a[0] = (byte)0xf0;
	    a[1] = 0x41;
	    a[2] = deviceId;
	    a[3] = modelId;
	    a[4] = cmd;
		// 3-byte address
	    a[5] = (byte)((addr>>16) & 0xff);
	    a[6] = (byte)((addr>>8) & 0xff);
	    a[7] = (byte)(addr & 0xff);
	    System.arraycopy(data, 0, a, 8, data.length);
		//a[a.size-2] = 0; // checksum
	    a[a.length-1] = (byte)0xf7;
	    checksum(a);
	    return a; 
	}
	
	/**
	 * Creates sysex-data for requesting a block of data from the midi-device.
	 *  
	 * @param modelId
	 * @param addr
	 * @param size
	 * @return
	 */
	protected byte[] rolandSysexReq(byte modelId, int addr, int size) {
		byte[] a = new byte[13];
	    a[0] = (byte)0xf0;
	    a[1] = 0x41;
	    a[2] = DEVICE_ID_ROLAND;
	    a[3] = modelId;
	    a[4] = COMMAND_SYSEX_REQUEST;
		// 3-byte address
	    a[5] = (byte)((addr>>16) & 0xff);
	    a[6] = (byte)((addr>>8) & 0xff);
	    a[7] = (byte)(addr & 0xff);
	    // 3-byte size
	    a[8] = (byte)((size>>16) & 0xff);
	    a[9] = (byte)((size>>8) & 0xff);
	    a[10] = (byte)((size) & 0xff);
		//a[11] = 0; // checksum
	    a[12] = (byte)0xf7;
	    checksum(a);
	    return a; 
	}
	
	public static byte[] nibblize(byte[] data) {
		byte[] nibbles = new byte[2 * data.length];
		int j = 0;
		boolean up = false;
		byte b = 0;
		for (int i = 0; i < nibbles.length; i++) {
			if (up) {
				nibbles[i] = (byte)((b>>4) & 0x0f);
				up = false;
			} else {
				b = data[j++]; 
				nibbles[i] = (byte)(b & 0x0f);
				up = true;
			}
		}
		return nibbles;
	}
	
	public static byte[] denibblize(byte[] nibbles) {
		byte[] data = new byte[nibbles.length / 2];
		int j = 0;
		boolean up = true;
		byte b = nibbles[0];
		for (int i = 1; i < nibbles.length; i++) {
			if (up) {
				data[j++] = (byte)(b | (nibbles[i] << 4));
				up = false;
			} else {
				b = nibbles[i];
				up = true;
			}
		}
		return data;
	}		

	public static byte calculateChecksum(byte[] data, int from, int to) {
		int sum = 0;
		for (int i = from; i <= to; i++) {
			sum += data[i];
		}
		return (byte)(128 - (sum % 128));
	}
}
