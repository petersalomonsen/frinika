/*
 * Created on Sep 20, 2013
 *
 * Copyright (c) 2004-2013 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.sequencer.gui.tracker;

import com.frinika.FrinikaMain;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NoteEvent;
import java.awt.Dimension;
import javax.swing.JFrame;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class TrackerPanelTest {
    public static void main(String[] args) throws Exception {
	System.out.println(System.getProperty("java.version"));
	FrinikaMain.configureUI();
	JFrame frame = new JFrame();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setSize(new Dimension(800,600));
	// Create the project container
	ProjectContainer proj = new ProjectContainer();
//	proj.getAudioServer().start();
	// Create a lane
	com.frinika.sequencer.model.MidiLane lane = proj.createMidiLane();

	ProjectFrame pf = new ProjectFrame(proj);
	pf.setVisible(false);
	// Create a MidiPart
        MidiPart part = new MidiPart(lane);
// Add some notes
        part.add(new NoteEvent(part, 0,60, 100, 0, 128));
        part.add(new NoteEvent(part, 128,61, 100, 0, 128));
        part.add(new NoteEvent(part, 256,62, 100, 0, 128));
        part.add(new NoteEvent(part, 512,63, 100, 0, 128));
        part.add(new NoteEvent(part, 768,64, 100, 0, 128));
        part.setBoundsFromEvents();
	
	TrackerPanel tp = new TrackerPanel(proj.getSequence(),pf);
	tp.setPart(part);
	frame.add(tp);
	frame.setVisible(true);
    }
}
