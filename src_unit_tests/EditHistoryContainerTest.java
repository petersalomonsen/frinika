import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NoteEvent;

import junit.framework.TestCase;
/*
 * Created on Mar 26, 2006
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

/**
 * Unit test to test EditHistoryContainer
 * @author Peter Johan Salomonsen
 */
public class EditHistoryContainerTest extends TestCase {

    MidiPart part;
    ProjectContainer proj;
    
    protected void setUp() throws Exception {
        super.setUp();
        
//      Create the audio context
        //new AudioContext();
        // Create the project container
        
        proj = new ProjectContainer();
        
        // Create a lane
        com.frinika.sequencer.model.MidiLane lane = proj.createMidiLane();

        // Create a MidiPart
        part = new MidiPart(lane);
    }

    public void testMultiEvents()
    {    
        // Add note
        
        NoteEvent noteEvent = new NoteEvent(part, 0,60, 100, 0, 128);
        proj.getEditHistoryContainer().mark("add event");
        part.add(noteEvent);
        proj.getEditHistoryContainer().mark("change event");
        part.remove(noteEvent);
        noteEvent.setVelocity(10);
        part.add(noteEvent);
        proj.getEditHistoryContainer().mark("remove event");
        part.remove(noteEvent);
        
        // Undo everything
        
        proj.getEditHistoryContainer().undo();
        assertEquals(noteEvent,part.getMultiEvents().first());
        assertEquals(10,noteEvent.getVelocity());
        proj.getEditHistoryContainer().undo();
        assertEquals(100,noteEvent.getVelocity());
        proj.getEditHistoryContainer().undo();
        assertEquals(0,part.getMultiEvents().size());

        // Redo everything
        
        proj.getEditHistoryContainer().redo();
        assertEquals(100,noteEvent.getVelocity());
        assertEquals(noteEvent,part.getMultiEvents().first());
        proj.getEditHistoryContainer().redo();
        assertEquals(10,noteEvent.getVelocity());
        proj.getEditHistoryContainer().redo();
        assertEquals(0,part.getMultiEvents().size());

        // Undo everything again
        
        proj.getEditHistoryContainer().undo();
        assertEquals(noteEvent,part.getMultiEvents().first());
        assertEquals(10,noteEvent.getVelocity());
        proj.getEditHistoryContainer().undo();
        assertEquals(100,noteEvent.getVelocity());
        proj.getEditHistoryContainer().undo();
        assertEquals(0,part.getMultiEvents().size());

    }
}
