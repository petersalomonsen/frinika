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

import javax.sound.midi.InvalidMidiDataException;

import com.frinika.global.Toolbox;
import com.frinika.sequencer.model.AbstractSysexMacro;

/**
 * This macro parses a raw string-coded sysex data. It does not appear as a
 * real macro to the user, but more like applying no macro at all.
 * Usage:
 * 
 * "f0 a1 34 b2 63 f7"
 * 
 *  or
 *   
 *  "F0 a1 12 B2 34 f7"
 * 
 * Skipping the macro-name "sysex" is allowed (see SysexMacro), so a user 
 * can either enter "sysex f0 a1 34 ..." or just "f0 a1 34 ...", which enables
 * the use of this as "no macro" in the eyes of the user.
 *   
 * @author Jens Gulden
 */
public class Sysex extends AbstractSysexMacro {

	public byte[] parse(String s) throws InvalidMidiDataException {
		String[] w = Toolbox.splitWords(s);
		// allow name of macro "sysex" to miss, as this is ray data
		if (w[0].equalsIgnoreCase("sysex")) {
			String[] a = new String[w.length-1];
			System.arraycopy(w, 1, a, 0, a.length);
			w = a;
		}
		return parse(w);
	}
	
	public byte[] parse(String[] args) throws InvalidMidiDataException {
		byte[] data = new byte[args.length];
		for (int i = 0; i < args.length; i++) {
			data[i] = parseByte(args[i], 16);
		}
		return data;
	}
}
