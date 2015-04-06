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

import javax.sound.midi.InvalidMidiDataException;

import junit.framework.TestCase;

import com.frinika.sequencer.midi.message.TempoMessage;

/**
 * @author Peter Johan Salomonsen
 */
public class TempoMessagesTest extends TestCase {
	public void testTempoMessages() throws InvalidMidiDataException
	{
		for(int n = 6;n<200;n++)
		{
			assertEquals(n,Math.round(new TempoMessage(new TempoMessage(n)).getBpm()));
		}
	}
	
}
