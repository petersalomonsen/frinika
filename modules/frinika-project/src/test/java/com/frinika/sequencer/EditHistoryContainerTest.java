package com.frinika.sequencer;

import com.frinika.project.FrinikaProjectContainer;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.project.AbstractProjectContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test to test EditHistoryContainer
 *
 * @author Peter Johan Salomonsen
 */
public class EditHistoryContainerTest {

    MidiPart part;
    AbstractProjectContainer proj;

    @Before
    protected void setUp() throws Exception {

//      Create the audio context
        //new AudioContext();
        // Create the project container
        proj = new FrinikaProjectContainer();

        // Create a lane
        com.frinika.sequencer.model.MidiLane lane = proj.createMidiLane();

        // Create a MidiPart
        part = new MidiPart(lane);
    }

    @Test
    public void testMultiEvents() {
        // Add note

        NoteEvent noteEvent = new NoteEvent(part, 0, 60, 100, 0, 128);
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
        Assert.assertEquals(noteEvent, part.getMultiEvents().first());
        Assert.assertEquals(10, noteEvent.getVelocity());
        proj.getEditHistoryContainer().undo();
        Assert.assertEquals(100, noteEvent.getVelocity());
        proj.getEditHistoryContainer().undo();
        Assert.assertEquals(0, part.getMultiEvents().size());

        // Redo everything
        proj.getEditHistoryContainer().redo();
        Assert.assertEquals(100, noteEvent.getVelocity());
        Assert.assertEquals(noteEvent, part.getMultiEvents().first());
        proj.getEditHistoryContainer().redo();
        Assert.assertEquals(10, noteEvent.getVelocity());
        proj.getEditHistoryContainer().redo();
        Assert.assertEquals(0, part.getMultiEvents().size());

        // Undo everything again
        proj.getEditHistoryContainer().undo();
        Assert.assertEquals(noteEvent, part.getMultiEvents().first());
        Assert.assertEquals(10, noteEvent.getVelocity());
        proj.getEditHistoryContainer().undo();
        Assert.assertEquals(100, noteEvent.getVelocity());
        proj.getEditHistoryContainer().undo();
        Assert.assertEquals(0, part.getMultiEvents().size());
    }
}
