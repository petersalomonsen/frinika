// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package com.frinika.tootX;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import uk.org.toot.audio.mixer.MixerControls;

import uk.org.toot.audio.mixer.automation.MixerControlsMidiSequenceSnapshotAutomation;

public class MixerControlsMidiStreamSnapshotAutomation
    extends MixerControlsMidiSequenceSnapshotAutomation
 //   implements MixerControlsSnapshotAutomation
{

    public MixerControlsMidiStreamSnapshotAutomation(
        MixerControls controls) {
        super(controls);
    }

    public void load(InputStream in) {  
    	
        try {
          	Sequence seq=MidiSystem.getSequence(in);
        	configureSequence(seq);
            recallSequence(seq);
                  	
        } catch ( Exception imde ) {
        	imde.printStackTrace();
        }
    }

    public void store(OutputStream out) {
 
    	Sequence snapshot =  storeSequence("Mixer");
        try {
            MidiSystem.write(snapshot, 1, out);
        } catch ( IOException ioe ) {
        	ioe.printStackTrace();
            System.err.println("Failed to create or write Snapshot file ");
        }
    }

	

   
}
