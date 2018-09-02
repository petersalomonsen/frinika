// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control.automation;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.*;
import uk.org.toot.control.automation.SnapshotAutomation;

/**
 * Implements the snapshot automation API in terms of standard midi files.
 * @author st
 *
 */
public class MidiFileSnapshotAutomation implements SnapshotAutomation
{
	private MidiSequenceSnapshotAutomation auto;
    protected File snapshotPath;
    
    /**
     * The file extension for snapshots
     */
    private String extension;

    public MidiFileSnapshotAutomation(MidiSequenceSnapshotAutomation auto, String ext) {
    	this.auto = auto;
    	extension = ext;
    }

    public void setPath(File path) {
        snapshotPath = path;
        if ( path != null ) {
        	snapshotPath.mkdirs();
        }
    }
    
    public void configure(String name) {
        File file = getSnapshotFile(name);
        if ( file == null ) return;
        if ( !file.exists() ) return;
        try {
            auto.configureSequence(MidiSystem.getSequence(file));
        } catch ( InvalidMidiDataException imde ) {
            System.err.println("Failed to configure Snapshot "+name);
        } catch ( IOException ioe ) {
            System.err.println("Failed to configure or read Snapshot file "+file.getPath());
        }
    }

    public void recall(String name) {
        File file = getSnapshotFile(name);
        if ( file == null ) return;
        if ( !file.exists() ) return;
        try {
            auto.recallSequence(MidiSystem.getSequence(file));
        } catch ( InvalidMidiDataException imde ) {
            System.err.println("Failed to recall Snapshot "+name);
        } catch ( IOException ioe ) {
            System.err.println("Failed to read Snapshot file "+file.getPath());
        }
    }

    public void store(String name) {
        File file = getSnapshotFile(name);
        if ( file == null ) return;
        if ( file.exists() ) {
            file.delete(); // !!! !!! confirmation?
        }
        Sequence snapshot = null;
        try {
            snapshot = auto.storeSequence(name);
            if ( snapshot != null ) {
            	file.createNewFile();
            	MidiSystem.write(snapshot, 1, file);
            }
        } catch ( IOException ioe ) {
            System.err.println("Failed to create or write Snapshot file "+file.getPath());
        }
    }

    public String[] list() {
        if ( snapshotPath == null ) return null;
        return snapshotPath.list();
    }

    protected File getSnapshotFile(String name) {
        if ( snapshotPath == null ) return null;
        return new File(snapshotPath, name+extension);
    }

}
