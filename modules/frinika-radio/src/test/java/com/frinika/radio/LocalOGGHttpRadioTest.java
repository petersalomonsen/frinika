/*
 * Copyright (c) 2004-2010 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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

package com.frinika.radio;

import com.frinika.project.FrinikaProjectContainer;
import com.frinika.project.MidiDeviceDescriptor;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.SynthLane;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Peter Johan Salomonsen
 */
public class LocalOGGHttpRadioTest {

    public LocalOGGHttpRadioTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of startRadio method, of class LocalOGGHttpRadio.
     */
    @Test
    public void testStartRadio() throws Exception {
        final String[] returnedEncoding = new String[1];
        final byte[] returnedData = new byte[65536];

        FrinikaProjectContainer projectContainer = new FrinikaProjectContainer();

        MidiDevice dev = null;
        for(MidiDevice.Info info : MidiSystem.getMidiDeviceInfo())
        {
            if(info.getName().equals("Gervill"))
            {
                dev = MidiSystem.getMidiDevice(info);
                break;
            }
        }
        SynthWrapper sw = new SynthWrapper(projectContainer,dev);
        MidiDeviceDescriptor mdd = projectContainer.addMidiOutDevice(sw);
        System.out.println(mdd.getMidiDeviceName());
        SynthLane sl = projectContainer.createSynthLane(mdd);
        MidiLane ml =  projectContainer.createMidiLane();
        ml.setMidiDevice(sw);

        MidiPart p = (MidiPart) ml.createPart();
        NoteEvent ne = new NoteEvent(p, 0, 64, 100, 0, 10000);
        p.add(ne);
        ml.add(p);
        projectContainer.getAudioServer().start();
               
        final boolean[] readerDone = new boolean[] {false};
        LocalOGGHttpRadio.startRadio(projectContainer);
        new Thread() {

            @Override
            public void run() {
                try {
                    System.out.println("Get audio input stream");
                    
                    try (AudioInputStream ais = AudioSystem.getAudioInputStream(new URL("http://localhost:15000").openStream())) {
                        System.out.println("Got stream:" +ais.getFormat().getEncoding());
                        returnedEncoding[0] = ais.getFormat().getEncoding().toString();
                        
                        ais.read(returnedData);
                    }
                } catch (IOException | UnsupportedAudioFileException ex) {
                    Logger.getLogger(LocalOGGHttpRadioTest.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    readerDone[0] = true;
                }
            }

        }.start();
        projectContainer.getSequencer().setLoopStartPoint(0);
        projectContainer.getSequencer().setLoopEndPoint(128*4);
        projectContainer.getSequencer().setLoopCount(500);
        
        projectContainer.getSequencer().start();

        while(!readerDone[0])
            Thread.sleep(100);

        LocalOGGHttpRadio.stopRadio();
        assertEquals("VORBISENC",returnedEncoding[0]);
    }

    /**
     * Test of stopRadio method, of class LocalOGGHttpRadio.
     */
    @Test
    public void testStopRadio() {
    }

}