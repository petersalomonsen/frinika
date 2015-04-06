/*
 * Created on Jul 15, 2005
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

import java.util.HashMap;
import java.util.HashSet;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/**
 * A cache object to keep hold of notes that are currently on.
 * @author Peter Johan Salomonsen
 *
 */
public class NoteOnCache {
    private HashMap<Receiver,HashSet<Integer>> pendingNoteOffs = new HashMap<Receiver,HashSet<Integer>>();
        
    public final void interceptMessage(MidiMessage msg, Receiver receiver)
    {
        try
        {
            ShortMessage shm = (ShortMessage)msg;
            if(shm.getCommand() == ShortMessage.NOTE_ON)
            {
                if(shm.getData2()==0)
                {
                    pendingNoteOffs.get(receiver).remove(shm.getChannel() << 8 | shm.getData1());
                }
                else
                {
                    if(!pendingNoteOffs.containsKey(receiver))
                        pendingNoteOffs.put(receiver,new HashSet<Integer>());
                 
                    pendingNoteOffs.get(receiver).add(shm.getChannel() << 8 | shm.getData1());
                }
            }
        } catch(Exception e) {}
    }
        
    /**
     * Send note-off to all pending notes
     *
     */
    public final void releasePendingNoteOffs()
    {
        for(Receiver recv : pendingNoteOffs.keySet())
        {
            for (int note : pendingNoteOffs.get(recv)) {
                ShortMessage shm = new ShortMessage();
                try {
                    shm.setMessage(ShortMessage.NOTE_ON, (note >> 8) & 0xf,
                            note & 0xff, 0);
                    
                        recv.send(shm, -1);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }
            }
        }
        pendingNoteOffs.clear();
    }
}
