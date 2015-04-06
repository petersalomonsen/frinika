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
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 * Abstract superclass containing tool methods for macro implementations
 * for the Roland E70.
 * 
 * @author Jens Gulden
 */
abstract class E70SysexMacroAbstract extends RolandSysexMacroAbstract {

	public final static byte MODEL_ID_E70 = (byte)0x3f;
	public final static int USER_PROGRAM_BASE_ADDR = 0x01705a; 
	public final static int USER_PROGRAM_SIZE = 0x10e; 
	
	protected byte[] e70Set(int addr, byte[] data) {
		return rolandSysexSet(MODEL_ID_E70, addr, data);
	}

	protected byte[] e70Req(int addr, int size) {
		return rolandSysexReq(MODEL_ID_E70, addr, size);
	}
	
	protected byte[] e70UserProgramSet(int userProgram, int offset, byte[] data) {
		return e70Set(USER_PROGRAM_BASE_ADDR + (USER_PROGRAM_SIZE * userProgram) + offset, data);
	}
	
	protected byte[] e70UserProgramReq(int userProgram, int offset, int size) {
		return e70Req(USER_PROGRAM_BASE_ADDR + (USER_PROGRAM_SIZE * userProgram) + offset, size);
	}
	
	public static MidiMessage[] usrPrgChg(int pgmnr, int chn) throws InvalidMidiDataException {
        ShortMessage bankselectMSB = new ShortMessage();
        bankselectMSB.setMessage(ShortMessage.CONTROL_CHANGE, chn, 0, 0);
        ShortMessage bankselectLSB = new ShortMessage();
        bankselectLSB.setMessage(ShortMessage.CONTROL_CHANGE, chn, 0x20, 0);
        ShortMessage pgmChg = new ShortMessage();
        pgmChg.setMessage(ShortMessage.PROGRAM_CHANGE, chn, pgmnr, 0);
        return new MidiMessage[] { bankselectMSB, bankselectLSB, pgmChg };
	}
}
