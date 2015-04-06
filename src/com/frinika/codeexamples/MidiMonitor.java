/*
 * Created on 3 Nov 2007
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

package com.frinika.codeexamples;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.frinika.tootX.gui.MidiInDeviceSelectPanel;
import com.frinika.tootX.gui.MidiOutDeviceSelectPanel;
import com.frinika.midi.MidiDebugDevice;

public class MidiMonitor {

	static MidiInDeviceSelectPanel inDeviceSelector;
	static MidiOutDeviceSelectPanel outDeviceSelector;
	static Receiver dbgIn;
	static Receiver recv;
	static Transmitter dbgOut;
	static Transmitter trans;
	static JPanel panel;
	
	public static void main(String args[]) {
		
		panel=new JPanel();
		JFrame frame=new JFrame();
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
	
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				MidiMonitor.run();	
			}			
		});
	}
	
	static void run() {
		MidiDevice d=new MidiDebugDevice();
		
		try {
			dbgIn=d.getReceiver();
			dbgOut=d.getTransmitter();
		} catch (MidiUnavailableException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		inDeviceSelector = new MidiInDeviceSelectPanel();
		panel.add(inDeviceSelector);
		
		outDeviceSelector = new MidiOutDeviceSelectPanel();
		panel.add(outDeviceSelector);

		inDeviceSelector.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				
				MidiDevice dev =  inDeviceSelector.getSelected();
				try {
					dev.open();		
					dev.getTransmitter().setReceiver(dbgIn);
					
				} catch (MidiUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		
		
		outDeviceSelector.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				MidiDevice dev =  outDeviceSelector.getSelected();
				try {
					dev.open();		
					dbgOut.setReceiver(dev.getReceiver());
					
				} catch (MidiUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		
		
		
	}

}
