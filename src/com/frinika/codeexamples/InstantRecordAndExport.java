package com.frinika.codeexamples;

/*
 * Created on Jun  22, 2008
 *
 * Copyright (c) 2004-2006 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
import java.io.File;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;

import com.frinika.project.ProjectContainer;
import javax.sound.sampled.AudioFileFormat.Type;
import com.frinika.tootX.midi.MidiInDeviceManager;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.tracker.filedialogs.ExportWavDialog;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioSystem;
import javax.swing.JFrame;

/**
 * Example of creating a project programatically, recording to a midi lane using a soft synth, and exporting directly to ogg
 * @author Peter Johan Salomonsen
 */
public class InstantRecordAndExport {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
            // Create the project container
            ProjectContainer proj = new ProjectContainer();
            proj.getAudioServer().start();
            // Create a lane
            com.frinika.sequencer.model.MidiLane lane = proj.createMidiLane();

            Synthesizer dev = null;
            for(MidiDevice.Info inf : MidiSystem.getMidiDeviceInfo())
            {
                if(inf.getName().equals("Gervill"))
                    dev = (Synthesizer) MidiSystem.getMidiDevice(inf);
            }

            
            SynthWrapper sw = new SynthWrapper(proj,dev);
            proj.addMidiOutDevice(sw);

            Soundbank sbk = MidiSystem.getSoundbank(new File("/home/peter/mystudio/soundfonts/NS_Piano.sf2"));
            sw.loadInstruments(sbk, new Patch[] { new Patch(0,0) });
            
            for(Instrument ins : sw.getLoadedInstruments())
                System.out.println(ins);
            // Assign the midi device to track
            lane.getTrack().setMidiDevice(sw);
            lane.setRecording(true);
            MidiInDeviceManager.setProject(proj);
            System.out.println("Press enter to start recording");
            System.in.read();

            proj.getSequencer().startRecording();
            System.out.println("Recording started, press enter to stop recording");
            System.in.read();
            
            proj.getSequencer().stop();
          //  Don't need to do this with new recording manager
    //        proj.getSequencer().deployTake(new int[] {0});

            long startTick = 0;
            long endTick = proj.getSequencer().getTickPosition()-1;

  
            Type type = null;
            for(Type t : AudioSystem.getAudioFileTypes())
                if(t.getExtension()
                .equals("ogg"))
                    
                    type = t;
            
            new ExportWavDialog(new JFrame(), proj,type, new File("/home/peter/mystudio/mytest.ogg"),startTick,endTick);
            System.exit(0);
        }
}
