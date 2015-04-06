
/*
 *
 * Copyright (c) 2006 P.J.Leonard
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
package com.frinika.codeexamples;

import com.frinika.global.FrinikaConfig;
import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.sequencer.gui.mixer.SynthWrapper;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.project.FrinikaAudioSystem;
import com.frinika.project.gui.ProjectFrame;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

/**
 *
 * @author pjl
 */
public class midiPlayer {

    public static void main(String args[]) throws Exception {


        FrinikaAudioSystem.getAudioServer();
        Synthesizer dev = null;
        for (MidiDevice.Info inf : MidiSystem.getMidiDeviceInfo()) {
            if (inf.getName().equals("Gervill")) {
                dev = (Synthesizer) MidiSystem.getMidiDevice(inf);
            //   dev.open();
            }
        }


        SynthWrapper sw = new SynthWrapper(null, dev);



        // String sf="GeneralUser GS 1.4.sf2";
        //String sf = "ChoriumRevA.SF2";

        // this needs to be after the project constructor (opens the device ?)
     //   Soundbank sbk = MidiSystem.getSoundbank(
     //           new File(FrinikaConfig.SOUNDFONT_DIRECTORY + "/" + sf));
        // dev.open();
     //   dev.loadAllInstruments(sbk);


        URL url=new URL("http://www.notz.com/music/jazz/midi/stolen.mid");

       //  URL url = new URL("http://www.notz.com/music/jazz/midi/rndmdngt.mid");

        //     File file = new File("/home/pjl/MIDI/www.alisdair.com/skylark.mid");


        System.out.println(" Load midi into a new project ");
        ProjectContainer proj = new ProjectContainer(MidiSystem.getSequence(url), null);
        proj.addMidiOutDevice(sw);

        for (Lane lane : proj.getLanes()) {
            if (lane instanceof MidiLane) {
                ((MidiLane) lane).setMidiDevice(sw);
            }
        }

        // either autoplay or fire up a project frame
        if (true) {
            final long endTick = proj.getSequence().getTickLength();

            System.out.println(" End tick= " + endTick);



            proj.getAudioServer().start();
            proj.getSequencer().start();

            proj.getSequencer().addSongPositionListener(new SongPositionListener() {

                public void notifyTickPosition(long tick) {
                    if (tick > endTick) {
                        try {
                            Thread.sleep(1000); // let the sound die down
                        } catch (InterruptedException ex) {
                            Logger.getLogger(midiPlayer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        // if you don't have the projec frame then uncomment
                        System.exit(0);
                    }
                }

                public boolean requiresNotificationOnEachTick() {
                    return false;
                }
            });
        } else {
            
            proj.getAudioServer().start();
            new ProjectFrame(proj);
        }
    }
}
