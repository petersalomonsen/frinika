/*
 * Created on Apr 8, 2005
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
package com.frinika.sequencer.gui.virtualkeyboard;

import java.util.Vector;
import java.awt.event.KeyEvent;

import javax.sound.midi.Receiver;


/**
 * Thread for tracking when a key is pressed and when it is actually released - seperating
 * key auto repeat events. This makes you able to hold a note using the computer keyboard, and make
 * it sustains its actual length (until you release the key, regardless of auto repeat events).
 * 
 * @author Peter Johan Salomonsen
 */
public class NoteKeyThread extends Thread {
    private Receiver receiver;
    int noteNumber;
    int velocity;
    int channel;
    
    NoteKeyThread[] noteKeyThreads;
    
    Vector<KeyEvent> keyEvents = new Vector<KeyEvent>();
    
    public NoteKeyThread(NoteKeyThread[] noteKeyThreads,Receiver receiver, int noteNumber, int channel, int velocity)
    {
        this.receiver = receiver;
        this.noteNumber = noteNumber;
        this.velocity = velocity;
        this.channel = channel;
        this.noteKeyThreads = noteKeyThreads;
        
        VirtualKeyboard.noteOn(receiver,noteNumber,channel,velocity);
        start();        
    }

    public synchronized void addKeyEvent(KeyEvent evt)
    {
        keyEvents.add(evt);
        notify();
    }
    
    public synchronized void run()
    {
        boolean keepPlay = true;
        
        while(keepPlay)
        {
            if(keyEvents.size() == 0)
            {
                try                
                {
                    wait();
                } catch(InterruptedException ex) {}
            }        
            do
            {
                if(keyEvents.get(0).getID()==KeyEvent.KEY_RELEASED)
                    keepPlay = false;
                else
                    keepPlay = true;
                keyEvents.remove(0);
                try{ wait(10); } catch(Exception ex) {};
            }
            while(keyEvents.size()>0);
        }
        
        VirtualKeyboard.noteOff(receiver,noteNumber,channel);
        noteKeyThreads[noteNumber] = null;
    }
}
