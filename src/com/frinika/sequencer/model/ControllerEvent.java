/*
 * Created on 06 August 2005
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

package com.frinika.sequencer.model;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

import com.frinika.sequencer.FrinikaTrackWrapper;

/**
 * Controller events represents a MIDI control change
 * @author Peter Johan Salomonsen
 *
 */
public class ControllerEvent extends ChannelEvent {
    private static final long serialVersionUID = 1L;

    transient MidiEvent midiEvent;
    
    int controlNumber;
    int value;

    @SuppressWarnings("deprecation")
    public ControllerEvent(FrinikaTrackWrapper track, long startTick,int controlNumber,int value) {
        super(track, startTick);
        this.controlNumber = controlNumber;
        this.value = value;
    }

    public ControllerEvent(MidiPart multiEventGroup, long startTick,int controlNumber,int value) {
        super(multiEventGroup, startTick);
        this.controlNumber = controlNumber;
        this.value = value;
    }

    /**
     * @return Returns the controlNumber.
     */
    public int getControlNumber() {
        return controlNumber;
    }

    /**
     * @param controlNumber The controlNumber to set.
     */
    public void setControlNumber(int controlNumber) {
        this.controlNumber = controlNumber;
    }

    /**
     * @return Returns the value.
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(int value) {
        this.value = value;
    }
    @Override
    public long getEndTick() {
    	return startTick;
    }
    
    @SuppressWarnings({"deprecation","deprecation"})
    @Override
    void commitRemoveImpl() { // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()
        getTrack().remove(midiEvent);
        zombie=true;
    }

    @SuppressWarnings("deprecation")
    @Override
	public
    void commitAddImpl() { // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()
        try
        {
            ShortMessage shm = new ShortMessage();
            shm.setMessage(ShortMessage.CONTROL_CHANGE,channel,controlNumber,value);
            midiEvent = new MidiEvent(shm,startTick);
            getTrack().add(midiEvent);
         
        } catch(InvalidMidiDataException e)
        {
            e.printStackTrace();
     
        }
        zombie=false;
    }

	public void restoreFromClone(EditHistoryRecordable object) {
		ControllerEvent evt = (ControllerEvent)object;
		this.part = evt.part;
		this.startTick = evt.startTick;
		this.controlNumber = evt.controlNumber;
		this.value = evt.value;
	}

}
