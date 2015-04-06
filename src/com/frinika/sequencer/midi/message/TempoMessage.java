package com.frinika.sequencer.midi.message;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

/*
 * Created on Jun 18, 2006
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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

/**
 * A tempo message api extended from a MetaMessage
 * @author Peter Johan Salomonsen
 *         PJL changed bpm to allow floats
 */
public class TempoMessage extends MetaMessage {
	public TempoMessage(float bpm) throws InvalidMidiDataException
	{
		int mpq = (int) (60000000.0 / bpm);
		this.setMessage(0x51, new byte[] { (byte) ((mpq >> 16) & 0xff),
				(byte) ((mpq >> 8) & 0xff), (byte) (mpq & 0xff) }, 3);
	}
	
	public TempoMessage(MetaMessage metaMessage) throws InvalidMidiDataException
	{
		this.setMessage(metaMessage.getType(),metaMessage.getData(),3);
	}

	/**
	 * Return tempo in BPM from this message
	 * @return
	 */
	public final float getBpm()
	{
        byte[] data = getData();	
        int mpq = (((data[0] & 0xff) << 16) | ((data[1] & 0xff ) << 8) | (data[2] & 0xff));
        return(float)(60000000.0/mpq);
	}
}

