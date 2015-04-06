/*
 * Created on 06-Jun-2006
 *
 * Copyright (c) 2006 P.J.Leonard
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
package com.frinika.tootX.gui;

import java.util.Vector;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiDevice.Info;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public class MidiOutDeviceSelectPanel extends JComboBox {
	private static final long serialVersionUID = 1L;

//	Vector<JCheckBox> boxes = new Vector<JCheckBox>();

	JComboBox combo;
	
	public MidiOutDeviceSelectPanel() {
		super(getList());
		// MidiDeviceHandle [] list= MidiHub.getMidiInHandles();

	}
	
	static Vector<MidiDevice.Info> getList() {
		Info infos[] = MidiSystem.getMidiDeviceInfo();

		Vector<MidiDevice.Info> list=new Vector<MidiDevice.Info>();
		for (Info info : infos) {
			MidiDevice dev;
			try {
				dev = MidiSystem.getMidiDevice(info);

				if (dev.getMaxReceivers() != 0) {
					list.add(dev.getDeviceInfo());
				}
			} catch (MidiUnavailableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return list;
		//			
		//			
		// for(final MidiDeviceHandle d:list){
		// if (d.getMidiDevice() == null) continue;
		// JCheckBox box=new
		// JCheckBox(d.toString(),names.contains(d.toString()));
		// boxes.add(box);
		// box.addActionListener(act);
		// add(box);
		// }

	}
	
	public MidiDevice getSelected() {
		Info str = (Info) getSelectedItem();
		MidiDevice dev1 = null;
		try {
			dev1 = MidiSystem.getMidiDevice(str);
		} catch (MidiUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dev1;
	}
	public static void main(String args[]) {

		JFrame f = new JFrame();
		f.setContentPane(new MidiOutDeviceSelectPanel());
		f.pack();
		f.setVisible(true);
	}
}
