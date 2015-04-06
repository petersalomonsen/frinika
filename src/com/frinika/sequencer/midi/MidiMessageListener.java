/*
 * Created on Feb 12, 2007
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

package com.frinika.sequencer.midi;

import javax.sound.midi.MidiMessage;

/**
 * Listener that can be connected to a MonitorReceiver. It receives all MIDI 
 * events that pass through a MonitorReceiver.
 * Note that instances of MidiMessageListener don't get directly connected to
 * a MonitorReceiver (there are no addMidiMessageListener() / 
 * removeMidiMessageListener() methods on MonitorReceiver), but will be added
 * to / removed from higher-level classes that use MonitorReceivers.
 * 
 * @see MonitorReceiver
 * @see com.frinika.sequencer.model.MidiLane
 * @see com.frinika.sequencer.model.FrinikaSequencer
 * @author Jens Gulden
 */
public interface MidiMessageListener {

	public void midiMessage(MidiMessage message);
	
}
