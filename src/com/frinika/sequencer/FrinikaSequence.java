/*
 * Created on Jul 5, 2005
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

package com.frinika.sequencer;

import java.util.Vector;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class FrinikaSequence extends Sequence {
    Vector<FrinikaTrackWrapper> frinikaTrackWrappers = new Vector<FrinikaTrackWrapper>();
        
    transient FrinikaSequencer sequencer;
    
    public FrinikaSequence(Sequence sequence) throws InvalidMidiDataException
	{
		super(sequence.getDivisionType(),sequence.getResolution());
	    
		for(Track track : sequence.getTracks())
        {
            FrinikaTrackWrapper trackWrapper = new FrinikaTrackWrapper(track);
            trackWrapper.setSequence(this);
            tracks.add(track);
            frinikaTrackWrappers.add(trackWrapper);  
        }
	    
	}

	public FrinikaSequence(float divisionType, int resolution, int tracks) throws InvalidMidiDataException {
		super(divisionType,resolution);
		for(int n=0;n<tracks;n++)
			createTrack();
	}   
	
//	/**
//	 * probably a bit naughty to do this ? PJL
//	 * 
//	 * @param seq
//	 */
//	public Vector<FrinikaTrackWrapper> addSequence(Sequence seq) {
//		Vector<FrinikaTrackWrapper> newTracks=new Vector<FrinikaTrackWrapper>();
//		for(Track track : seq.getTracks())
//        {
//            FrinikaTrackWrapper trackWrapper = new FrinikaTrackWrapper(track);
//            trackWrapper.setSequence(this);
//            tracks.add(track);
//            frinikaTrackWrappers.add(trackWrapper);  
//            newTracks.add(trackWrapper);
//        }		
//		return newTracks;
//	}
	
	@Override
	public Track createTrack() 
	{
		Track track = super.createTrack();
		FrinikaTrackWrapper trackWrapper = new FrinikaTrackWrapper(track);
		frinikaTrackWrappers.add(trackWrapper);
        trackWrapper.setSequence(this);
		return track;
	}

	public FrinikaTrackWrapper createFrinikaTrack() 
	{
		Track track = super.createTrack();
		FrinikaTrackWrapper trackWrapper = new FrinikaTrackWrapper(track);
		frinikaTrackWrappers.add(trackWrapper);
        trackWrapper.setSequence(this);
		return trackWrapper;
	}



	public Vector<FrinikaTrackWrapper> getFrinikaTrackWrappers() {
		return frinikaTrackWrappers;
	}
	      
    /**
     * @return Returns the sequencer.
     */
    public FrinikaSequencer getSequencer() {
        return sequencer;
    }

    /**
     * Will automatically be set when attached to a sequencer
     * @param sequencer The sequencer to set.
     */
    void setSequencer(FrinikaSequencer sequencer) {
        this.sequencer = sequencer;
    }

    /**
     * Returns a clone of this sequence suitable for Midi file export. What it does is to map the FTW channel setting to all the midi events
     * for the corresponding tracks
     * @return
     * @throws InvalidMidiDataException 
     */
    public Sequence export() throws InvalidMidiDataException
    {
        Sequence newSeq = new Sequence(getDivisionType(),getResolution());
        for(FrinikaTrackWrapper ftw : frinikaTrackWrappers)
        {
            Track track = newSeq.createTrack();
            for(int n=0;n<ftw.size();n++)
            {

                        MidiEvent sourceMidiEvent = ftw.get(n);
                MidiMessage msg = sourceMidiEvent.getMessage();
                if(msg instanceof ShortMessage)
                {
                    ShortMessage shm = (ShortMessage)msg;
                    ShortMessage nshm = new ShortMessage();
                    nshm.setMessage(shm.getCommand(),ftw.getMidiChannel(),shm.getData1(),shm.getData2());
                    msg = nshm;
                }
                MidiEvent newEvent = new MidiEvent(msg,sourceMidiEvent.getTick());
                track.add(newEvent);
            }
        }
        return newSeq;
    }
}
