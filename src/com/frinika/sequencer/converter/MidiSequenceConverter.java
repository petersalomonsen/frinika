/*
 * Created on Feb 12, 2006
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
package com.frinika.sequencer.converter;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.frinika.sequencer.midi.message.TempoMessage;

/**
 * Tools for converting Midi Sequence objects
 * 
 * @author Peter Johan Salomonsen
 */
public class MidiSequenceConverter {
    /** 
     * Convert a Midi Sequence from Frinika singleTrack sequences to MultiTrack. All tracks are split up
     * so that there is only one channel represented per track. Tracks are mapped to channel
     * in ascending channel order, and the initial track is left as a mastertrack (containing tempo events etc.)
     */
    public static Sequence splitChannelsToMultiTrack(Sequence sequence)
    {
       System.out.println("Scanning sequence with "+sequence.getTracks().length+" tracks.");
       Track track = sequence.getTracks()[0];
       boolean[] channelsUsed = new boolean[16];
      
       // First scan all channels used
       
       for(int n=0;n<track.size();n++)
       {
           MidiEvent event = track.get(n);
           if(event.getMessage() instanceof ShortMessage)
           {
               ShortMessage message = (ShortMessage)event.getMessage();
               channelsUsed[message.getChannel()] = true;
           }
       }
       
       System.out.print("Channels used: ");
       for(int n=0;n<channelsUsed.length;n++)
       {
           if(channelsUsed[n])
               System.out.print(n+" ");
       }
       System.out.println();
       
       Integer[] channelToTrackMapping = new Integer[16];
       int tracksCreated = 0;
       
       // Then create tracks for channels in ascending order
       for(int n=0;n<channelsUsed.length;n++)
       {
           if(channelsUsed[n])
           {
               sequence.createTrack();
               channelToTrackMapping[n] = tracksCreated++;
           }
       }
       
       System.out.println("Created "+tracksCreated+" new tracks.");
       // Insert events into new tracks
       
       for(int n=0;n<track.size();n++)
       {
           MidiEvent event = track.get(n);
           if(event.getMessage() instanceof ShortMessage)
           {
               // Add event to new track
               ShortMessage message = (ShortMessage)event.getMessage();
               sequence.getTracks()[channelToTrackMapping[message.getChannel()] + 1].add(event);  // +1 since we don't want to overwrite the mastertrack
               
               // Remove it from the single track
               track.remove(event);
               n--;
           }
       }
     
       System.out.println("Events moved into new tracks. Initial track kept as mastertrack for tempo change etc.");
       return sequence;
    }
 
    /**
     * Find the first tempo meta message and return the tempo value
     * @return
     */
    public static float findFirstTempo(Sequence sequence) throws Exception
    {
    		for(Track track : sequence.getTracks())
    		{
    			for(int n=0;n<track.size();n++)
    			{
    				MidiEvent event = track.get(n);

    				if(event.getMessage() instanceof MetaMessage
    						&& ((MetaMessage)event.getMessage()).getType()==0x51)
    				{
    					float tempo = new TempoMessage((MetaMessage)event.getMessage()).getBpm();
    					System.out.println("Found tempomessage "+tempo+" bpm");
    					return tempo;
    				}
    			}
    		}
    		throw new Exception("No tempo message found");
    }    
}
