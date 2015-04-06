/*
 * Created on Sep 18, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.gui.virtualkeyboard;

import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/**
 * @author Peter Johan Salomonsen
 *
 */
public final class VirtualKeyboard {
	public static int Octave = 4;

	static final String[] noteNames = new String[] {
			"C-",
			"C#",
			"D-",
			"D#",
			"E-",
			"F-",
			"F#",
			"G-",
			"G#",
			"A-",
			"A#",
			"B-"
	};
		 
	public static String getNoteString(int note)
	{
		if(note>=0)
			return(noteNames[note%12]+(note/12));
		else
			return("");
	}
	
	static private int addOctave(int note)
	{
		return((Octave*12)+note);
	}
	
	public static int keyToInt(char c) throws Exception
	{
		switch(c)
		{
			case 'z':
				return(addOctave(-12));
			case 's':
				return(addOctave(-11));
			case 'x':
				return(addOctave(-10));
			case 'd':
				return(addOctave(-9));
			case 'c':
				return(addOctave(-8));
			case 'v':
				return(addOctave(-7));
			case 'g':
				return(addOctave(-6));
			case 'b':
				return(addOctave(-5));
			case 'h':
				return(addOctave(-4));
			case 'n':
				return(addOctave(-3));
			case 'j':
				return(addOctave(-2));
			case 'm':
				return(addOctave(-1));
			case 'q':
				return(addOctave(0));
			case '2':
				return(addOctave(1));
			case 'w':
				return(addOctave(2));
			case '3':
				return(addOctave(3));
			case 'e':
				return(addOctave(4));
			case 'r':
				return(addOctave(5));
			case '5':
				return(addOctave(6));
			case 't':
				return(addOctave(7));
			case '6':
				return(addOctave(8));
			case 'y':
				return(addOctave(9));
			case '7':
				return(addOctave(10));
			case 'u':
				return(addOctave(11));
			case 'i':
				return(addOctave(12));
			case '9':
				return(addOctave(13));
			case 'o':
				return(addOctave(14));
			case '0':
				return(addOctave(15));
			case 'p':
				return(addOctave(16));
		}
		throw new Exception();
	}
	
	public static void noteOn(final Receiver recv, final int note, final int channel, final int velocity)
	{
            try
		    {
                ShortMessage msg = new ShortMessage();
                msg.setMessage(ShortMessage.NOTE_ON,channel,note,velocity);
                recv.send(msg,-1);
		    }
		    catch(Exception e) {e.printStackTrace();}
	}

    public static void noteOff(final Receiver recv,final int note, final int channel)
    {
            try
            {
                ShortMessage msg = new ShortMessage();
                msg.setMessage(ShortMessage.NOTE_ON,channel,note,0);
                recv.send(msg,-1);
            }
            catch(Exception e) {e.printStackTrace();}
    }
}
