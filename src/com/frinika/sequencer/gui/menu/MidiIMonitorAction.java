/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
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
package com.frinika.sequencer.gui.menu;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import com.frinika.tootX.midi.MidiInDeviceManager;
import com.frinika.priority.Priority;

public class MidiIMonitorAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	JFrame frame;
	boolean first=true;
	public MidiIMonitorAction() {
		super(getMessage("project.menu.debug.midi_test"));
	}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		MidiDevice in = null;
                // FIXME 
                // MidiInDeviceManager.getMidiInDevice();
		try {
			in.open();
			Transmitter t = in.getTransmitter();
			t.setReceiver(new Receiver() {
				long tLast = 0;
				double dtRef=2000.0/96.0;
				double dtMax=0.0;
				long cnt=0;
				public void close() {
					// TODO Auto-generated method stub
				}

				public void send(MidiMessage message, long timeStamp) {
					
					if (first) {
						Priority.setPriorityFIFO(90);
						first=false;
					}
					switch (message.getStatus()) {
					case ShortMessage.TIMING_CLOCK:
						
						long t = System.nanoTime();
						double dt=(t-tLast)/1e6;
						tLast = t;
						dt=dt-dtRef;
						
						if (Math.abs(dt) > dtMax) {
							dtMax=Math.abs(dt);
						}
						
						if ((cnt--)==0) {
							System.out.println(String.format("%5.5f mS ",dtMax));
							dtMax=0.0;
							cnt=50;
							Priority.display();
						}
						break;

					default:

					}

				}

			});
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}
