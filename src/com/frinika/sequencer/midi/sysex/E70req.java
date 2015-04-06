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
 * Sysex-macro for sending a general data-request message to a Roland E70.
 * Usage: 
 * 
 * e70req <address> <size>
 * 
 * (Note that receiving the answered sysex-data is not covered by Frinika.
 * This macro exists for completeness and symmetry to E70set.)
 * 
 * @author Jens Gulden
 */
public class E70req extends E70SysexMacroAbstract {

	/**
	 * Macro: e70req <address> <size>
	 * 
	 * Will cause the device to send the requested block of data. 
	 * 
	 * arg[0]: address
	 * arg[1]: size
	 */
	public byte[] parse(int[] args) {
		int address = args[0];
		int size = args[1];
		return e70Req(address, size);
	}
	
}
