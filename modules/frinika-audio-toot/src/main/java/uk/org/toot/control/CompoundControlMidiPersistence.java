// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.midi.*;

import uk.org.toot.control.automation.MidiPersistence;
// !!! !!!
import static uk.org.toot.control.automation.ControlSysexMsg.*;

public class CompoundControlMidiPersistence implements CompoundControlPersistence
{
    private File root;

    public CompoundControlMidiPersistence(File root) {
        this.root = root;
    }

    /*#public List getPresets(CompoundControl c);*/
    public List<String> getPresets(CompoundControl c) {
        File dir = new File(root, path(c));
        List<String> names = new java.util.ArrayList<String>();
        if ( !dir.exists() || !dir.isDirectory() ) return names;
        File[] files = dir.listFiles();
        for ( File file : files ) {
            if ( file.isDirectory() ) continue;
            names.add(file.getName());
        }
        return names; 
    }

    public void loadPreset(CompoundControl c, String name) {
        int providerId = c.getProviderId();
        int moduleId = c.getId();
   	    File path = new File(root, path(c));
        File file = new File(path, name);
        if ( !file.exists() ) return;
        try {
	        Sequence sequence = MidiSystem.getSequence(file);
            Track track = sequence.getTracks()[0];
            for ( int i = 0; i < track.size(); i++ ) {
                MidiMessage msg = track.get(i).getMessage();
                if ( !isControl(msg) ) continue;
                if ( getProviderId(msg) != providerId ) continue;
                if ( getModuleId(msg) != moduleId ) continue;
                Control control = c.deepFind(getControlId(msg));
                if ( control == null ) continue;
                // for sanity we ignore bypass controls
                if ( control instanceof CompoundControl.BypassControl ) continue;
                control.setIntValue(getValue(msg));
            }
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        } catch ( InvalidMidiDataException imde ) {
            imde.printStackTrace();
        }
    }

    public void savePreset(CompoundControl c, String name) {
        int providerId = c.getProviderId();
        int moduleId = c.getId();
        try {
	        Sequence sequence = new Sequence(Sequence.PPQ, 1);
    	    Track track = sequence.createTrack();
        	MidiPersistence.store(providerId, moduleId, 0, c, track);
    	    File path = new File(root, path(c));
            path.mkdirs();
        	MidiSystem.write(sequence, 0, new File(path, name));
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
        } catch ( InvalidMidiDataException imde ) {
            imde.printStackTrace();
        }
    }

	protected String path(CompoundControl c) {
        // <domain>/<providerId>/<moduleId> e.g. audio/1/27
        return c.getPersistenceDomain()+File.separator+c.getProviderId()+File.separator+c.getId();
    }

/*	protected String path(int providerId, int moduleId, String name) {
        // <providerId>/<moduleId>/name
        return path(providerId, moduleId)+File.separator+name;
    } */

}
