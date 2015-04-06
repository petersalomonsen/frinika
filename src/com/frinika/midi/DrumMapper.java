/*
 * Created on Sep 11, 2004
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

package com.frinika.midi;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import rasmus.midi.provider.RasmusSynthesizer;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.mixer.MidiDeviceIconProvider;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.synth.Synth;

/**
 * 
 * DrumMapper is a midi device that redirects midi events to other devices doing some mapping enroute.
 * 
 */
public class DrumMapper implements MidiDevice, MidiDeviceIconProvider {
	
	
	public class NoteMap {
		public int note;
//		Receiver recv;
//		int chan;
	}
	
	private static Icon icon = new javax.swing.ImageIcon(RasmusSynthesizer.class.getResource("/icons/frinika.png"));
	
	public Icon getIcon()
	{
		if(icon.getIconHeight() > 16 || icon.getIconWidth() > 16)
		{
			BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics2D g = img.createGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			Image im = img.getScaledInstance(16 , 16, Image.SCALE_SMOOTH);
			icon = new ImageIcon(im);
		}		
		return icon;
	}	

	MidiDevice defaultDevice;

	//int channel=-1;
	
	Receiver defRecv;

	NoteMap noteMap[] = new NoteMap[128];

	public static class DrumMapperInfo extends Info {
		DrumMapperInfo() {
			super("DrumMapper", "drpj.co.uk", "A MIDI drum mapper", "0.0.1");
		}
	}

	Info deviceInfo = new DrumMapperInfo();

	Receiver receiver;

	List<Receiver> receivers;

	DrumMapper() {
		int i = 0;
		for (; i < 128; i++) {
			NoteMap n = noteMap[i] = new NoteMap();
			n.note = i;
	//		n.recv = null;
	//		n.chan = 9;
		}

		receiver = new Receiver() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage,
			 *      long)
			 */
			public void send(MidiMessage message, long timeStamp) {

				// if it's a note then use note device
				// otherwise use the defualt device.
			//	if (channel == -1) return;
				
				try {
					if (message instanceof ShortMessage) {
						ShortMessage shm = (ShortMessage) message;
						if (shm.getCommand() == ShortMessage.NOTE_ON) {
							int note = shm.getData1();
					
						//	Receiver recv = noteMap[note].recv;
						//	if (recv == null)
							Receiver  recv = defRecv;
							if (recv == null)
								return;
							int noteByte = noteMap[note].note;
							shm.setMessage(shm.getCommand(), shm.getChannel(), noteByte, shm
									.getData2());
							recv.send(shm, timeStamp);
							return;
						}
					}
					if (defRecv != null)
						defRecv.send(message, timeStamp);

				} catch (Exception e) {
					// For debugging
					e.printStackTrace();
				}
			}

			public void close() {
				// TODO Auto-generated method stub
			}
		};

		receivers = new ArrayList<Receiver>();
		receivers.add(receiver);

	}


	public void save(File file) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(file));
			// out.writeObject(setup);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(" DRUM MAP SAVE ");
	}

	public void load(File file) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					file));
			// SynthSettings setup = (SynthSettings)in.readObject();
			// loadSynthSetup(setup);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(" DRUM MAP LOAD ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#getMaxPolyphony()
	 */
	public int getMaxPolyphony() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#getChannels()
	 */
	public MidiChannel[] getChannels() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#getVoiceStatus()
	 */
	public VoiceStatus[] getVoiceStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#isSoundbankSupported(javax.sound.midi.Soundbank)
	 */
	public boolean isSoundbankSupported(Soundbank soundbank) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#loadInstrument(javax.sound.midi.Instrument)
	 */
	public boolean loadInstrument(Instrument instrument) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#unloadInstrument(javax.sound.midi.Instrument)
	 */
	public void unloadInstrument(Instrument instrument) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#remapInstrument(javax.sound.midi.Instrument,
	 *      javax.sound.midi.Instrument)
	 */
	public boolean remapInstrument(Instrument from, Instrument to) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#getDefaultSoundbank()
	 */
	public Soundbank getDefaultSoundbank() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#getAvailableInstruments()
	 */
	public Instrument[] getAvailableInstruments() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#getLoadedInstruments()
	 */
	public Instrument[] getLoadedInstruments() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#loadAllInstruments(javax.sound.midi.Soundbank)
	 */
	public boolean loadAllInstruments(Soundbank soundbank) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#unloadAllInstruments(javax.sound.midi.Soundbank)
	 */
	public void unloadAllInstruments(Soundbank soundbank) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#loadInstruments(javax.sound.midi.Soundbank,
	 *      javax.sound.midi.Patch[])
	 */
	public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.Synthesizer#unloadInstruments(javax.sound.midi.Soundbank,
	 *      javax.sound.midi.Patch[])
	 */
	public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#getDeviceInfo()
	 */
	public Info getDeviceInfo() {
		// TODO Auto-generated method stub
		return deviceInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#open()
	 */
	public void open() throws MidiUnavailableException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#close()
	 */
	public void close() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#isOpen()
	 */
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#getMicrosecondPosition()
	 */
	public long getMicrosecondPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#getMaxReceivers()
	 */
	public int getMaxReceivers() {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#getMaxTransmitters()
	 */
	public int getMaxTransmitters() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#getReceiver()
	 */
	public Receiver getReceiver() throws MidiUnavailableException {
		return receiver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#getReceivers()
	 */
	@SuppressWarnings("unchecked")
	public List getReceivers() {
		return receivers;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#getTransmitter()
	 */
	public Transmitter getTransmitter() throws MidiUnavailableException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sound.midi.MidiDevice#getTransmitters()
	 */
	@SuppressWarnings("unchecked")
	public List getTransmitters() {
		return null;
	}

	/**
	 * over to provide easier GUI manufactoring
	 */
	public String toString() {
		return getDeviceInfo().toString();
	}

	public void instrumentNameChange(Synth synth, String instrumentName) {
		// TODO Auto-generated method stub

	}

	public MidiDevice getDefaultMidiDevice() {
		return defaultDevice;
	}

	public void setDefaultMidiDevice(MidiDevice midiDevice) {
		if (defaultDevice != midiDevice) {

			if (defRecv != null)
				defRecv.close();
			try {
				midiDevice.open();
				defRecv = midiDevice.getReceiver();
				System.out.println(" Set default receiver " + defRecv);
				defaultDevice = midiDevice;
			} catch (MidiUnavailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		defaultDevice = midiDevice;
	}

	public JPanel getGUIPanel(ProjectFrame frame,MidiLane lane) {
		return new DrumMapperGUI(this, frame.getProjectContainer(),lane);
	}


	public NoteMap getNoteMap(int i) {
		// TODO Auto-generated method stub
		return noteMap[i];
	}

	public void setMapping(int in,int out) {
		if (in <0 || in > 127) return;
		if (out <0 || out > 127) return;
		System.out.println(in + " --->" +out);
		noteMap[in].note=out;
		
	}


	public void setNoteMap(int[] noteMap2) {
		for (int i=0;i<128;i++) {			
			noteMap[i].note=noteMap2[i];
		}
		
	}


//	public int getChannel() {
//		return channel;
//	}
//
//
//	public void setChannel(int channel) {
//		this.channel = channel;
//	}
}
