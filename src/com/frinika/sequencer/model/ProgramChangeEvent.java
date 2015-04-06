/*
 * Created on Apr 24, 2006
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

package com.frinika.sequencer.model;

import com.frinika.sequencer.patchname.MyPatch;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

public class ProgramChangeEvent extends ChannelEvent {

	transient MidiEvent programEvent;

	transient MidiEvent msbEvent;

	transient MidiEvent lsbEvent;

	int prog;

	int msb;

	int lsb;

	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;

	public ProgramChangeEvent(MidiPart part, long startTick, int prog, int msb,
			int lsb) {
		super(part, startTick);

	}

	@Override
	public long getEndTick() {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	void commitRemoveImpl() { // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()
	//	System .out.println(" COMMIT REMOVE PROG ");

		getTrack().remove(msbEvent);
		getTrack().remove(lsbEvent);
		getTrack().remove(programEvent);
                zombie=true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void commitAddImpl() { // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()

		// System. out.println(" COMMIT ADD PROG ");
		try {

			ShortMessage shm = new ShortMessage();
			shm.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0, msb);
			msbEvent = new MidiEvent(shm, startTick);
			getTrack().add(msbEvent);

			shm = new ShortMessage();
			shm.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0x20, lsb);
			lsbEvent = new MidiEvent(shm, startTick);
			getTrack().add(lsbEvent);

			shm = new ShortMessage();
			shm.setMessage(ShortMessage.PROGRAM_CHANGE, channel, prog, 0);
			programEvent = new MidiEvent(shm, startTick);
			getTrack().add(programEvent);

		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                zombie=false;
	}

	public void restoreFromClone(EditHistoryRecordable object) {
		// TODO Auto-generated method stub
		ProgramChangeEvent evt=(ProgramChangeEvent)object;
		this.part = evt.part;
		this.startTick = evt.startTick;
		this.prog = evt.prog;
		this.msb = evt.msb;
		this.lsb = evt.lsb;		
	}

	public void setProgram(int prog2, int msb2, int lsb2) {

		
		prog=prog2;
		msb=msb2;
		lsb=lsb2;
		
	}
        
        public MyPatch getPatch() {
            return new MyPatch(prog, msb, lsb);
        }

}
