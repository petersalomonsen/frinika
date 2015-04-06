/*
 * Created on Jan 19, 2006
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 *               2007 Karl Helgason
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
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;


/**
 * @author Peter Johan Salomonsen
 */
public class MetaEvent extends MultiEvent {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    transient MidiEvent metaEvent;
    
    int type;
    byte[] data;
    
	@SuppressWarnings("deprecation")
	@Override
	void commitRemoveImpl() { // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()
	//	System. out.println(" COMMIT REMOVE PROG ");

		getTrack().remove(metaEvent);

	}

	@SuppressWarnings("deprecation")
	@Override
	public void commitAddImpl() { // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()

		// System. out.println(" COMMIT ADD PROG ");
		try {

			metaEvent = new MidiEvent(getMessage(), startTick);
			getTrack().add(metaEvent);

		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void restoreFromClone(EditHistoryRecordable object) {
		// TODO Auto-generated method stub
		MetaEvent evt=(MetaEvent)object;
		this.part = evt.part;
		this.startTick = evt.startTick;
		this.type = evt.type;
		this.data = evt.data;
	}    

    public MetaEvent(MidiPart part, long startTick)
    {
    		super(part, startTick);
    }
    
	public long getEndTick() {
		// TODO Auto-generated method stub
		return 0;
	}        

	public void setMessage(int type, byte[] data) {
    	this.type = type;
    	this.data = data;
    }    
	
	public void setMessage(MetaMessage message)
	{
    	this.type = message.getType();
    	this.data = message.getData();
	}
	
	public MetaMessage getMessage() throws InvalidMidiDataException
	{
		MetaMessage msg = new MetaMessage();
		msg.setMessage(type, data, data.length);
		return msg;
	}	

}
