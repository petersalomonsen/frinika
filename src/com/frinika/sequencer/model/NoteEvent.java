/*
 * Created on 06 August 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 *               2007 PJL added some comparators
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

import java.util.Comparator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

import com.frinika.sequencer.FrinikaTrackWrapper;


import com.frinika.sequencer.gui.virtualkeyboard.VirtualKeyboard;
/**
 * 
 * @author Peter Johan Salomonsen
 *
 */
public class NoteEvent extends ChannelEvent {

	final static public Comparator<NoteEvent> noteComparator= new NoteComparator();
	final static public StartComparator startComparator= new StartComparator();
	final static public EndComparator endComparator= new EndComparator();

	private static final long serialVersionUID = 1L;

    transient MidiEvent startEvent;
    transient MidiEvent endEvent;

    int note;
    int velocity;
    long duration;
    
    boolean valid = false;
    
    /**
     * Constructor for creating a note event. For registering the new note event in the track use FrinikaTrackWrapper.add(MultiEvent evt)
     * @param track
     * @param startTick
     * @param note
     * @param velocity
     * @param channel
     * @param duration
     * @deprecated
     */
    public NoteEvent(FrinikaTrackWrapper track, long startTick, int note, int velocity, int channel, long duration)
    {
    		super(track, startTick);
    		this.note = note;
    		this.velocity = velocity;
    		this.channel = channel;
    		this.duration = duration;
    }
    
    /**
     * 
     * @param group
     * @param startTick
     * @param note
     * @param velocity
     * @param channel
     * @param duration
     */
    public NoteEvent(MidiPart part, long startTick, int note, int velocity, int channel, long duration)
    {
    		super(part, startTick);
    		this.note = note;
    		this.velocity = velocity;
    		this.channel = channel;
    		this.duration = duration; 		
    }

    /**
     * The process generating the NoteEvent should first supply it start event, 
     * and when ready the end event - to form a complete Note event
     * @param startEvent
     * 
     */
    NoteEvent(MidiPart part, MidiEvent startEvent)
    {
        super(part,startEvent.getTick());
        this.startEvent = startEvent;
        ShortMessage shm = (ShortMessage)startEvent.getMessage();
        note = shm.getData1();
        velocity = shm.getData2();
        channel = shm.getChannel();
        startTick = startEvent.getTick();
    }
    
    public void setEndEvent(MidiEvent endEvent) {
        this.endEvent = endEvent;
        duration = endEvent.getTick()-startTick;
        if (duration < 0 ) {
        	System.out.println(" NEGATIVE LENGTH NOTE FIXED");
        	duration=0;
        	endEvent.setTick(startEvent.getTick());
        }
        valid = true;
    }

    
    public long getEndTick() {
    	return startTick+duration;
    }
    
    public int getNote()
    {
        return note;
    }
    
    public String getNoteName()
    {
        return VirtualKeyboard.getNoteString(note);
    }
    
    public void setNote(int note)
    {
    	if (this.note == note ) return;
        this.note = note;
    }
    
    public int getVelocity()
    {
        return velocity;
    }
    
    public void setVelocity(int velocity)
    {
        this.velocity = velocity;
    }

    /**
     * @return Returns the duration.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration The duration to set.
     */
    public void setDuration(long duration) {
    	if (duration < 0 ) {
    		System.out.println(" Sorry but I won't make a note negative length ");
    		duration=0;
    		return;
    	}
        this.duration = duration;
    }
 
    @SuppressWarnings("deprecation") void commitRemoveImpl() // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()
    {
      	if (zombie) {
    		try {
				throw new Exception(" Attempt to remove a zombie note from the track ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
      	if (startEvent == null) {
    		try {
				throw new Exception(" Attempt to remove a note with null start from the track ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println(" You can ignore this exception  . . . . ");
				e.printStackTrace();
			}
    	} else {
    		getTrack().remove(startEvent);
    	}

      	if (endEvent == null) {
    		try {
				throw new Exception(" Attempt to remove a note with null end from the track ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else {
    		getTrack().remove(endEvent);
    	}
        zombie=true;
    }
    
    @SuppressWarnings("deprecation")
    public void commitAddImpl() // Jens, renamed to be able to handle notification of CommitListeners in MultiEvent, see MultiEvent.commitXxx()
    {
    
    	if (part.lane == null) return;
        try
        {
            ShortMessage shm = new ShortMessage();
            shm.setMessage(ShortMessage.NOTE_ON,channel,note,velocity);
            startEvent = new MidiEvent(shm,startTick);
            getTrack().add(startEvent);
         
            shm = new ShortMessage();
            shm.setMessage(ShortMessage.NOTE_ON,channel,note,0);
            endEvent = new MidiEvent(shm,startTick+duration);
            getTrack().add(endEvent);
        } catch(InvalidMidiDataException e)
        {
            e.printStackTrace();
     
        }
        zombie=false;
    }
    
	public void restoreFromClone(EditHistoryRecordable object) {
		NoteEvent note = (NoteEvent)object;
		this.part = note.part;
		this.startTick = note.startTick;
		this.velocity = note.velocity;
		this.channel = note.channel;
		this.duration = note.duration;
		this.note = note.note;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.frinika.sequencer.model.MultiEvent#setValue(int)
	 */
	public void setValue(int val) {
		this.velocity=val;
	}
	
	public int getValue() {
		return velocity;
	}

	public long rightTickForMove() {
		return startTick+duration;
	}

	
	public boolean isDrumHit() {
		return duration == 0;
	}
	
	/**
	 * PJL HACK NOT DO NOT USE
	 *
	 */
	public void validate() {
		if (endEvent == null) {
			System.err.println("Fixing null end event" );
			ShortMessage shm=new ShortMessage();
			
            try {
				shm.setMessage(ShortMessage.NOTE_ON,channel,note,0);
			} catch (InvalidMidiDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			duration=0;
			endEvent=new MidiEvent(shm,startTick); 
		}
	}
	
	/**
	 * 
	 * Utility comparators for sorted sets. Notes are never equaly unless they are the same object.
	 * 
	 * @author pjl
	 *
	 */
	static public class StartComparator implements Comparator<NoteEvent> {

		public int compare(NoteEvent o1, NoteEvent o2) {
			if (o1.startTick > o2.startTick) return 1;
			if (o1.startTick < o2.startTick) return -1;
			if (o1.startTick + o1.duration> o2.startTick+ o2.duration) return 1;
			if (o1.startTick + o1.duration< o2.startTick+ o2.duration) return -1;
			if (o1.note > o2.note) return 1;
			if (o1.note < o2.note) return -1;
	//		if (o1.part.colorID > o2.part.colorID) return 1;
	//		if (o1.part.colorID < o2.part.colorID) return -1;
			return o1.compareTo(o2);   // it should never come to this ?			
		}
	}
	
	
	static public class EndComparator implements Comparator<NoteEvent> {

		public int compare(NoteEvent o1, NoteEvent o2) {
			if (o1.startTick + o1.duration> o2.startTick+ o2.duration) return 1;
			if (o1.startTick + o1.duration< o2.startTick+ o2.duration) return -1;
			if (o1.startTick > o2.startTick) return 1;
			if (o1.startTick < o2.startTick) return -1;
			if (o1.note > o2.note) return 1;
			if (o1.note < o2.note) return -1;
	//		if (o1.part.colorID > o2.part.colorID) return 1;
	//		if (o1.part.colorID < o2.part.colorID) return -1;
			return o1.compareTo(o2);   // it should never come to this ?			
		}
	}
	
	
	static public class NoteComparator implements Comparator<NoteEvent> {

		public int compare(NoteEvent o1, NoteEvent o2) {
			if (o1.note > o2.note) return 1;
			if (o1.note < o2.note) return -1;
			if (o1.startTick > o2.startTick) return 1;
			if (o1.startTick < o2.startTick) return -1;
			if (o1.startTick + o1.duration> o2.startTick+ o2.duration) return 1;
			if (o1.startTick + o1.duration< o2.startTick+ o2.duration) return -1;
	//		if (o1.part.colorID > o2.part.colorID) return 1;
	//		if (o1.part.colorID < o2.part.colorID) return -1;
			return o1.compareTo(o2);   // it should never come to this ?			
		}
	}
	
}
