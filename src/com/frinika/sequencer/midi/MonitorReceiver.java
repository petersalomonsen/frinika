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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import java.util.Collection;

/** 
 * Allows to 'snoop' the data sent to a receiver, by passing data on to
 * MidiMessageListeners.
 * 
 * Note that instances of MidiMessageListener don't get directly connected to a
 * MonitorReceiver (there are no addMidiMessageListener() /
 * removeMidiMessageListener() methods on MonitorReceiver), but will be added to /
 * removed from higher-level classes that use MonitorReceivers.
 * 
 * @see MidiMessageListener
 * @author Jens Gulden
 */
public class MonitorReceiver implements Receiver {

	protected Receiver chained;
	protected Collection<MidiMessageListener> listeners;

	private static boolean isLinux = System.getProperty("os.name").equals(
			"Linux");

	public MonitorReceiver(Collection<MidiMessageListener> listeners,
			Receiver chained) {
		this.chained = chained;
		this.listeners = listeners;
	}

	public void send(MidiMessage message, long timeStamp) {

		// I hope no one is interested in these events
		if (message.getStatus() >= ShortMessage.MIDI_TIME_CODE)
			return;

		if (isLinux) {
			if (message.getStatus() == ShortMessage.PITCH_BEND) {
				ShortMessage mess = (ShortMessage) message;
				short low = (byte) mess.getData1();
				short high = (byte) mess.getData2();


				int channel = mess.getChannel();

                                // linux midi has a bug in the pitch bend this fixes the problem
				low = (byte) mess.getData1();
				high = (byte) mess.getData2();
				
				high= (short) ((high+64) & 0x007f);
				
				try {
					mess.setMessage(ShortMessage.PITCH_BEND, channel,
							low, high);
				} catch (InvalidMidiDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		chained.send(message, timeStamp);
		notifyListeners(message);
	}

	public void close() {
		chained.close();
	}

	protected void notifyListeners(MidiMessage message) {
		for (MidiMessageListener l : listeners) {
			l.midiMessage(message);
		}
	}

}