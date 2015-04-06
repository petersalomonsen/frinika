/*
 * Created on Apr 15, 2006
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.midi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import com.frinika.sequencer.MidiResource;
import com.frinika.sequencer.gui.mixer.MidiDeviceMixerPanel;
import com.frinika.sequencer.model.ControllerListProvider;

/**
 * Wrapper for external midi out devices
 * 
 * @author Peter Johan Salomonsen
 */
public class MidiOutDeviceWrapper implements MidiDevice, MidiListProvider,Serializable   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// TODO transient or not transient that is the question
	transient ControllerListProvider controllerList;
	//Info name;
	
    transient MidiDevice midiDevice;
    transient MidiDeviceMixerPanel gui;
    transient Receiver receiver = new Receiver(){

        /* (non-Javadoc)
         * @see javax.sound.midi.Receiver#send(javax.sound.midi.MidiMessage, long)
         */
        public void send(MidiMessage message, long timeStamp) {
            
            try
            {
                midiDevice.getReceiver().send(message,timeStamp);
                
                // Check if short message and update the gui
                if(message instanceof ShortMessage)
                {
                    ShortMessage shm = (ShortMessage)message;
                    int channel = shm.getChannel();
    
                    // Pass the message onto midi device
                    
                    if(shm.getCommand() == ShortMessage.NOTE_ON)
                    {
                    }
                    else if(shm.getCommand() == ShortMessage.CONTROL_CHANGE)
                    {
                        if(gui!=null && shm.getData1()==7)
                            gui.mixerSlots[channel].setVolume(shm.getData2());
                        else if(gui!=null && shm.getData1()==10)
                            gui.mixerSlots[channel].setPan(shm.getData2());
                    }
                    else if(shm.getCommand() == ShortMessage.PITCH_BEND)
                    {
                        
                    }
                }
            }
            catch(Exception e) {
                // For debugging
                //e.printStackTrace();
            }
        }

        /* (non-Javadoc)
         * @see javax.sound.midi.Receiver#close()
         */
        public void close() {
            try {
                midiDevice.getReceiver().close();
            } catch (MidiUnavailableException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }            
        }
        
    };
    

    public MidiOutDeviceWrapper(MidiDevice midiDevice)
    {
        this.midiDevice = midiDevice;
        controllerList=MidiResource.getDefaultControllerList();
//        name=midiDevice.getDeviceInfo();
    }

    public Receiver getReceiver() throws MidiUnavailableException {
        return receiver;
    }

    public List<Receiver> getReceivers() {
        List<Receiver> receivers = new ArrayList<Receiver>();
        receivers.add(receiver);
        for(Receiver recv : midiDevice.getReceivers())
        {
            try {
                if(recv != midiDevice.getReceiver())
                    receivers.add(recv);
            } catch (MidiUnavailableException e) {
                e.printStackTrace();
            }
        }
        return receivers;
    }

    public Transmitter getTransmitter() throws MidiUnavailableException {
        return midiDevice.getTransmitter();
    }

    public List<Transmitter> getTransmitters() {
        return midiDevice.getTransmitters();
    }

    public Info getDeviceInfo() {
        return midiDevice.getDeviceInfo();
    }

    public void open() throws MidiUnavailableException {
        midiDevice.open();
        
    }

    public void close() {
        midiDevice.close();
    }

    public boolean isOpen() {
        return midiDevice.isOpen();
    }

    public long getMicrosecondPosition() {
        return midiDevice.getMicrosecondPosition();
    }

    public int getMaxReceivers() {
        return midiDevice.getMaxReceivers();
    }

    public int getMaxTransmitters() {
        return midiDevice.getMaxTransmitters();
    }

	public ControllerListProvider getControllerList() {
		// TODO Auto-generated method stub
		return controllerList;
	}


}
