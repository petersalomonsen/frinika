/*
 * Created on 30.aug.2005
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
package com.frinika.sequencer.midieventprocessor;
import com.frinika.sequencer.MidiEventProcessor;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.NoteEvent;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class ScaleVelocity extends MidiEventProcessor {

    int amount;
    
    public ScaleVelocity(int amount)
    {
        if(amount<0)
            amount = 100;
        this.amount = amount;
    }
    
    @Override
    public void processNoteEvent(NoteEvent event) {
        int newVel = (int)(event.getVelocity()*((float)amount/100f));
        
        if(newVel>127)
            event.setVelocity(127);
        else
            event.setVelocity(newVel);    
    }

    @Override
    public void processControllerEvent(ControllerEvent event) {
        // TODO Auto-generated method stub
        
    }

}
