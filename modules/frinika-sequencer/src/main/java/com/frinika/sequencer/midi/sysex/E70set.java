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

/**
 * Sysex-macro for sending a general data-store message to a Roland E70. 
 * Usage: 
 * 
 * e70set <address> <data1> <data2> <data3> ...
 * 
 * @author Jens Gulden
 */
public class E70set extends E70SysexMacroAbstract {

	/**
	 * Macro: e70set <address> <data1> <data2> <data3> ...
	 * arg[0]: address
	 * arg[1]..arg[n]: bytes to send
	 */
        @Override
	public byte[] parse(int[] args) {
		int address = args[0];
		byte[] data = new byte[args.length-1];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte)args[i + 1];
		}
		return e70Set(address, data);
	}
	
}
