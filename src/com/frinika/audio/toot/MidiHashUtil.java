/*
 * Created on 5 Sep 2007
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

package com.frinika.audio.toot;

import javax.sound.midi.ShortMessage;

public class MidiHashUtil {
	
	static public long hashValue(ShortMessage mess) {

		long chn = mess.getChannel();
		long cmd = mess.getCommand();
		long cntrl;
		if (cmd == ShortMessage.CONTROL_CHANGE)
			cntrl = mess.getData1();
		else if (cmd == ShortMessage.PITCH_BEND)
			cntrl = 0;
		else {
			System.out.println(" Don't know what to do with " + mess);
			return -1;
		}
		return ((chn << 8 + cmd) << 8) + cntrl;
	}

	static public void hashDisp(long hash) {

		long cntrl = hash & 0xFF;

		long cmd = (hash & 0xFF00) >> 8;
		long chn = (hash & 0xFF00) >> 16;

		System.out.println(chn + "  " + cmd + " " + cntrl);
	}

}
