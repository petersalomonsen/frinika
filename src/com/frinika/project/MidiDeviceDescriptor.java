package com.frinika.project;

import java.io.Serializable;
import java.lang.reflect.Method;
/*
 * Created on Sep 19, 2006
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

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.sampled.Mixer;
import javax.swing.Icon;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.mixer.MidiDeviceIconProvider;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.SynthLane;
import com.frinika.synth.SynthRack;

/**
 * A serializable representation of a Midi Device used in a Frinika project.
 * Contains resources neccesary for re-opening the correct midi device.
 * 
 * @author Peter Johan Salomonsen
 */
public class MidiDeviceDescriptor implements Serializable, MidiDeviceIconProvider {
	private static final long serialVersionUID = 1L;

	String midiDeviceName;

	String projectName;

	protected Serializable serializableMidiDevice = null;

	transient MidiDevice midiDevice;

	transient private boolean isInstalled = false;

	/**
	 * Construct a new MidiDeviceDescriptor from a MidiDevice
	 * 
	 * @param midiDevice
	 */
	public MidiDeviceDescriptor(MidiDevice midiDevice) {
		this.midiDevice = midiDevice;
		this.midiDeviceName = midiDevice.getDeviceInfo().getName();
		this.projectName = midiDeviceName;

		if (midiDevice instanceof SynthWrapper) {
			SynthWrapper synthwrapper = (SynthWrapper) midiDevice;
			if (synthwrapper.getRealDevice() instanceof Serializable)
				serializableMidiDevice = (Serializable) synthwrapper
						.getRealDevice();
		}
	}

	/**
	 * Name of the midi device as registered in MidiSystem
	 * 
	 * @return
	 */
	public String getMidiDeviceName() {
		return midiDeviceName;
	}

	/**
	 * Set the name of the midi device as registered in MidiSystem
	 * 
	 * @param midiDeviceName
	 */
	public void setMidiDeviceName(String midiDeviceName) {
		this.midiDeviceName = midiDeviceName;
	}

	/**
	 * Get the name of the midi device as the user has set for it in the Frinika
	 * project
	 * 
	 * @return
	 */
	public String getProjectName() {
		return projectName;
	}

	/**
	 * Set the name of the midi device as the user wants to name it in the
	 * Frinika project
	 * 
	 * @param projectName
	 */
	public void setProjectName(String projectName) {
		this.projectName = projectName;

		if (midiDevice instanceof SynthWrapper) {
			SynthWrapper synthwrapper = (SynthWrapper) midiDevice;
			MidiDevice realdevice = synthwrapper.getRealDevice();
			try {
				Method setName = realdevice.getClass().getMethod("setName",
						String.class);
				setName.invoke(realdevice, projectName);
			} catch (Exception e) {
			}

		}
	}

	/**
	 * Install a MidiDevice that this descriptor describes into the given
	 * project
	 * 
	 * @param container
	 */
	final public void install(ProjectContainer project) {
		if (isInstalled)
			return;

		if (serializableMidiDevice != null) {
			if (serializableMidiDevice instanceof MidiDevice) {
				midiDevice = new SynthWrapper(project,
						(MidiDevice) serializableMidiDevice);
				try {
					midiDevice.open();
					project.loadMidiOutDevice(this);
					isInstalled = true;
					attachToSynthLane(project); // PJL
					return;
				} catch (MidiUnavailableException e) {
					System.out.println(" Failed to open MidiDevice "
							+ midiDeviceName);
					e.printStackTrace();
				}
			}
		}

		installImp(project);
		isInstalled = true;

		if (midiDevice instanceof SynthWrapper) {
			SynthWrapper synthwrapper = (SynthWrapper) midiDevice;
			if (synthwrapper.getRealDevice() instanceof Serializable)
				serializableMidiDevice = (Serializable) synthwrapper
						.getRealDevice();
		
		}
		attachToSynthLane(project); 
		setProjectName(projectName);
	}

	private void attachToSynthLane(ProjectContainer project ) {

		// **************** PJL

		// System. out.println(" Resolving Synth lane ");
		boolean found = false;
		for (Lane lane : project.projectLane.getChildren()) {

			if (lane instanceof SynthLane) {
				SynthLane sl=(SynthLane) lane;
				if (sl.getMidiDescriptor() != this ) continue;
				if (sl.install(this)) {

					MidiDevice dev = sl.getMidiDescriptor().getMidiDevice();
					if (dev instanceof SynthWrapper  && ((SynthWrapper)dev).getAudioProcess() != null
							|| dev instanceof SynthRack ){        	  
						sl.attachAudioProcessToMixer();
					}
					found = true;
					break;
				}
			}
		}

		if (!found) {
			System.out.println("Unable to find a SynthLane for synth "
					+ midiDevice);
			SynthLane lane = project.createSynthLane(this);
			lane.attachAudioProcessToMixer();
		}
		
	}
	/**
	 * Called from install to actually do the work. Typically you want to
	 * override this but make sure super.installImp is called.
	 * 
	 * 
	 * @param project
	 */
	protected void installImp(ProjectContainer project) {
		MidiDevice dev = null;

		try {

			for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
				if (info.getName().equals(midiDeviceName)) {
					dev = MidiSystem.getMidiDevice(info);

					// Assure that this is actually a MidOutDevice
					if (dev.getMaxReceivers() != 0)
						break;
					else
						dev = null;
				}
			}

			// Bypass MidiHub
			// dev = MidiHub.getMidiOutDeviceByName(midiDeviceName);

			if (dev == null) {
				System.out.println(" Failed to find MidiDevice "
						+ midiDeviceName);
			} else {
				midiDevice = new SynthWrapper(project, dev);
				//midiDevice.open();
				project.loadMidiOutDevice(this);
			}
		} catch (MidiUnavailableException e) {
			System.out.println(" Failed to open MidiDevice " + midiDeviceName);
			e.printStackTrace();
		}

	}

	public MidiDevice getMidiDevice() {
		return midiDevice;
	}
	
	public Serializable getSerializableMidiDevice() {
		return serializableMidiDevice;
	}

	@Override
	public String toString() {
		return getProjectName() + " (" + getMidiDeviceName() + ")";
	}

	public Icon getIcon() {
		return ProjectFrame.getMidiDeviceIcon(midiDevice);
	}
	
	public Icon getLargeIcon() {
		return ProjectFrame.getMidiDeviceLargeIcon(midiDevice);
	}
	
}
