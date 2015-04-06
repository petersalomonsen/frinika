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
import javax.sound.midi.SysexMessage;

/**
 * Sysex-macro for setting interal effects on a Roland E70.
 * 
 * This macro not only returns sysex-events, but also generates bank-select
 * controller messages. (An example how flexible SysexMacros are.)
 * 
 * @author Jens Gulden
 */
public class E70fx extends E70SysexMacroAbstract {
	
	public String usage() {
		return "Usage: e70fx <reverb-type> <reverb-intensity> <chorus-type> <chorus-intensity>  [ <user-prg> ] [ <use-prg-change> ]";
	}
	
	public final static int ADDR_OFFSET_REVERB = 0x15;
	public final static int ADDR_OFFSET_CHORUS = 0x16;
	public final static String[] REVERB_TYPES = {
		"Off",
		"Room1",
		"Room2",
		"Room3",
		"Hall1",
		"Hall2",
		"Plate",
		"Delay",
		"Panning"
	};
	public final static String[] CHORUS_TYPES = {
		"Off",
		"Chorus1",
		"Chorus2",
		"Chorus3",
		"Chorus4",
		"Feedback",
		"Flanger",
		"Delay1",
		"Delay2"
	};
		
	/**
	 * Macro: e70fx <reverb-type> <reverb-intensity> <chorus-type> <chorus-intensity>  [ <user-prg> ] [ <use-prg-change> ]
	 * 
	 * Sets the built-in reverb-effect style on one of the user-programs.
	 * There are two modes of operation: 
 	 * In order to overcome the limitation that the actively
	 * selected user-program cannot be configured via sysex, the current
	 * user-program will be changed to before sending the sysex message
	 * and after having made the changes will be switched back again.
	 * This has the effect that after setting the effect this way, the
	 * <user-prg> will be the actively selected one, even if it hadn't
	 * been selected before.
	 * To disable the special behaviour of switching user-programs, use  
	 * 
	 * arg[0]: reverb-type (0: none, 1: room1, 2: room2, 3: room3, 4: hall1, etc.)
	 * arg[1]: reverb-intensity (0-7)
	 * arg[2]: chorus-type (0: none, 1: chorus1, 2: chorus2, etc.)
	 * arg[3]: chorus-intensity (0-7)
	 * arg[4] (optional): user-program to modify, default "11" (0)
	 * arg[5] (optional): channel on which to send user program switch, may differ
	 *                    from current Frinika-track's channel. Default is 16 (i.e. 
	 *                    15 if counting from 0 to 15), use -1 to disable sending
	 *                    user-program changes before and after the sysex data.
	 */
	public MidiMessage[] parseMessages(String macro) throws InvalidMidiDataException {
		String[] args = splitArgs(macro);
		int usrPrg = 0;
		int chn = 15;
		if (args.length > 4) {
			usrPrg = parseInt(args[4], 10);
		}
		if (args.length > 5) {
			chn = parseInt(args[5], 10);
		}
		
		byte[] data = parse(args); // get sysex data

		SysexMessage syxm = new SysexMessage();
		syxm.setMessage(data, data.length);
		
		if ((chn >= 0) && (chn <= 15)) {
			int otherUsrPrg = (usrPrg == 0) ? 1 : 0;
			MidiMessage[] prgChg = usrPrgChg( otherUsrPrg, chn );
			MidiMessage[] prgChgBack = usrPrgChg( usrPrg, chn );
			MidiMessage[] mm = { prgChg[0], prgChg[1], prgChg[2], 
					             syxm, 
					             prgChgBack[0], prgChgBack[1], prgChgBack[2] };
			return mm;
		} else {
			return new MidiMessage[] { syxm };
		}
	}
	
	public byte[] parse(String[] args) throws InvalidMidiDataException {
		if (args.length < 4) {
			error("At least 4 parameters are required.");
		}
		int reverbType = parseType(args[0], REVERB_TYPES);
		int reverbIntensity = parseInt(args[1], 10, 1, 8);
		int chorusType = parseType(args[2], CHORUS_TYPES);
		int chorusIntensity = parseInt(args[3], 10, 1, 8);
		int usrPrg = 0;
		if (args.length > 4) {
			usrPrg = parseInt(args[4], 10, 0, 99);
		}
		// let intensities start with 1 (except set to 0 already)
		if (reverbIntensity > 0) reverbIntensity -= 1;
		if (chorusIntensity > 0) chorusIntensity -= 1;
		byte reverbData = (byte)(((reverbType << 4) & 0xf0) | (reverbIntensity & 0xf));
		byte chorusData = (byte)(((chorusType << 4) & 0xf0) | (chorusIntensity & 0xf));
		return e70UserProgramSet(usrPrg, ADDR_OFFSET_REVERB, new byte[] { reverbData, chorusData } );
	}
	
}
