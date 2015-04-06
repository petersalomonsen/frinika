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
package com.frinika.tootX.midi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;

import javax.sound.midi.ShortMessage;

public class MidiHashUtil {

    static public long hashValue(ShortMessage mess) {

        byte data[] = mess.getMessage();

        long cmd = mess.getCommand();

        if (cmd == ShortMessage.PITCH_BEND) {
            return ((long) data[0] << 8);
        } else {
            return (((long) data[0] << 8) + data[1]);
        }
    }

    static public void hashDisp(long hash) {

        long cntrl = hash & 0xFF;

        long cmd = (hash >> 8) & 0xFF;
        long chn = (hash >> 16) & 0xFF;

        System.out.println(chn + "  " + cmd + " " + cntrl);
    }

    static ShortMessage reconstructShortMessage(long hash, ShortMessage mess) {

        if (mess == null) mess=new ShortMessage();
        
        int status = (int) ((hash >> 8) & 0xFF);
        int data1 = (int) (hash & 0xFF);
        try {
            mess.setMessage(status, data1, 0);
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(MidiHashUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mess;
    }
}
