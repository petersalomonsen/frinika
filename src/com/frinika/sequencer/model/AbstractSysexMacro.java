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

package com.frinika.sequencer.model;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.SysexMessage;

import com.frinika.global.Toolbox;

import java.util.*;

/**
 * Abstract superclass for SysexMacros.
 * 
 * @author Jens Gulden
 */
public abstract class AbstractSysexMacro implements SysexMacro {
	
	public final static String SYSEX_MACRO_PACKAGE = "com.frinika.sequencer.midi.sysex";
	
	/**
	 * Generic usage message. Should be overwritten by subclasses.
	 *  
	 * @return Usage message string.
	 */
	public String usage() {
		return "Usage: <macro-name> <param1> <param2> ...";
	}
	
	/**
	 * Tries to find a responsible macro-parser class for a given
	 * sysex macro string. The first word of the string is considered
	 * the macro name, which should be available as a class in
	 * com.frinika.sequencer.midi.sysex, e.g.:
	 * "roland 1000 11 22 33" will try to load the macro class
	 * com.frinika.sequencer.midi.sysex.Roland.
	 * 
	 * @param macro
	 * @return
	 */
	public static SysexMacro findMacro(String s) {
		SysexMacro macro;
		String ww = Toolbox.firstWord(s);
		String w = Toolbox.capitalize(ww);
		try {
			Class cl;
			try {
				cl = Class.forName(SYSEX_MACRO_PACKAGE + "." + w);
			} catch (ClassNotFoundException cnfe) {
				cl = Class.forName(ww); // maybe fully qualified classname
			}
			Object o = cl.newInstance();
			macro = (SysexMacro)o;
		} catch (InstantiationException ie) {
			ie.printStackTrace();
			return null;
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
			return null;
		} catch (ClassNotFoundException cnfe) { // not found: maybe raw data
			try {
				byte b = parseByte(w, 16);
			} catch (InvalidMidiDataException imde) {
				// not a number: macro not found
				return null;
			}
			// parse as raw data
			macro = new com.frinika.sequencer.midi.sysex.Sysex();
		}
		return macro;
	}
	
	/**
	 * Entry method called from SysexEvent.
	 * Default implementation assumes a single sysex message to be parsed 
	 * and thus delegates to parse(String).
	 * @param macro
	 * @return
	 */
	public MidiMessage[] parseMessages(String macro) throws InvalidMidiDataException {
		byte[] data = parse(macro);
		SysexMessage syxm = new SysexMessage();
		syxm.setMessage(data, data.length);
		MidiMessage[] mm = { syxm };
		return mm;
	}
	
	/**
	 * The default implementation skipps the macro-name,
	 * then calls parse(StringTokenizer st). 
	 * @param macro
	 * @return
	 */
	public byte[] parse(String macro) throws InvalidMidiDataException {
		StringTokenizer st = new StringTokenizer(macro);
		st.nextToken(); // skip macro name
		return parse(st);
	}
	
	/**
	 * The default implementation extracts individual blank-seperated parameters (not comma-seperated),
	 * then calls parse(String[] args). 
	 * @param st
	 * @return
	 */
	public byte[] parse(StringTokenizer st) throws InvalidMidiDataException {
		int size = st.countTokens();
		String[] args = new String[size];
		for (int i = 0; i < size; i++) {
			args[i] = st.nextToken();
		}
		return parse(args);
	}
	
	/**
	 * The default implementation treats all args as decimal number values,
	 * then calls parse(int[] args)
	 * @param args
	 * @return
	 */
	public byte[] parse(String[] args) throws InvalidMidiDataException {
		int[] a = new int[args.length];
		for (int i = 0; i < args.length; i++) {
			a[i] = parseIntArg(args[i], i);
		}
		return parse(a);
	}
	
	/**
	 * Might be overwritten if other formats than decimal are to be parsed as args.
	 * @param arg
	 * @param index
	 * @return
	 */
	public int parseIntArg(String arg, int index) throws InvalidMidiDataException {
		return parseInt(arg, 10);
	}
	
	/**
	 * The default implementation throws a runtime error, so at last this method
	 * must be overwritten by subclasses.
	 * @param args
	 * @return
	 */
	public byte[] parse(int[] args) throws InvalidMidiDataException {
		throw new AbstractMethodError("parse(int[] args) or another parse-method must be implemented by sublcasses of SysexMacro");
	}
	
	
	// --- Tools ---
	
	/**
	 * Same as splitWords, but without first word (i.e. without macro name itself, 
	 * just parameters).
	 * @param s
	 * @return
	 */
	public static String[] splitArgs(String s) {
		String[] w = Toolbox.splitWords(s);
		String[] args = new String[w.length-1];
		System.arraycopy(w, 1, args, 0, args.length);
		return args;
	}

	/**
	 * Parses a single byte-string. By default a hex-value is assumed, but decimal
	 * values can be given if preceeded by a tilde (~). 
	 * @param s
	 * @return
	 */
	protected static int parseInt(String s, int defaultRadix) throws InvalidMidiDataException {
		int radix = defaultRadix;
		if (s.startsWith("~")) {
			radix = 10;
			s = s.substring(1);
		} else if (s.startsWith("0x")) {
			radix = 16;
			s = s.substring(2);
		}
		try {
			return Integer.parseInt(s, radix);
		} catch (NumberFormatException nfe) {
			throw new InvalidMidiDataException(nfe.getMessage());
		}
	}
	
	protected static int parseInt(String s, int defaultRadix, int min, int max) throws InvalidMidiDataException {
		int i = parseInt(s, defaultRadix);
		if ((i < min) || (i > max)) {
			throw new InvalidMidiDataException("value "+i+" is not valid, expected range "+min+"..."+max);
		}
		return i;
	}
	
	protected static byte parseByte(String s, int defaultRadix) throws InvalidMidiDataException {
		int i = parseInt(s, defaultRadix);
		if (i < 0 || i > 255) {
			throw new InvalidMidiDataException("value "+i+" is not a byte");
		}
		return (byte)i;
	}

	protected int parseType(String s, String[] list) throws InvalidMidiDataException {
		//try {
		//	int num = parseInt(s, 10);
		//	return num;
		//} catch (NumberFormatException nfe) {
			for (int i = 0; i < list.length; i++) {
				if (s.equalsIgnoreCase(list[i])) {
					return i;
				}
			}
			throw new InvalidMidiDataException("Value '"+s+"' is not available in the list '"+Toolbox.joinStrings(list, ",")+"'.");
		//}
	}
	
	protected void error(String msg) throws InvalidMidiDataException {
		throw new InvalidMidiDataException(msg+"\n"+usage());
	}
}
