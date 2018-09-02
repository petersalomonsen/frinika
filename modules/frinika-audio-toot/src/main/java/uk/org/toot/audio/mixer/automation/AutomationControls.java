// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import uk.org.toot.control.*;
import uk.org.toot.audio.core.AudioControls;
import java.awt.Color;

import static uk.org.toot.misc.Localisation.*;

/**
 * The relevant controls for snapshot and dynamic automation.
 * @author st
 *
 */
public class AutomationControls extends AudioControls
{
    public static final int AUTOMATION_ID = 119; // !!! !!! huh ? !!! !!!
    // note negative to prevent automation
    public static final int AUTOMATION_READ_ID = -1;
    public static final int AUTOMATION_WRITE_ID = -2;

    public static final int AUTOMATION_RECALL_ID = -3;
    public static final int AUTOMATION_STORE_ID = -4;

    private BooleanControl readControl;
    private BooleanControl writeControl;

    private BooleanControl recallControl;
    private BooleanControl storeControl;

    protected AutomationControls() {
        super(AUTOMATION_ID, "Auto");
    }

    public void ensureDynamicControls() {
        if ( readControl != null ) return;
        ControlRow readWrite = new ControlRow();
        readWrite.add(readControl = new ReadControl());
        readWrite.add(writeControl = new WriteControl());
        add(readWrite);
    }

    public void ensureSnapshotControls() {
        if ( recallControl != null ) return;
        ControlRow recallStore = new ControlRow();
        recallStore.add(recallControl = new RecallControl());
        recallStore.add(storeControl = new StoreControl());
        add(recallStore);
    }

    public boolean canRead() {
        if ( readControl == null ) return true;
        return readControl.getValue();
    }

    public boolean canWrite() {
        if ( writeControl == null ) return true;
        return writeControl.getValue();
    }

    public boolean canRecall() {
        if ( recallControl == null ) return true;
        return recallControl.getValue();
    }

    public boolean canStore() {
        if ( storeControl == null ) return true;
        return storeControl.getValue();
    }

	public boolean canBypass() { return false; }

    public boolean canBeDeleted() { return false; }

    public boolean canBeMoved() { return false; }

    public boolean canBeMovedBefore() { return false; }

    public boolean canBeInsertedBefore() { return false; }

    public boolean isAlwaysVertical() { return true; }

    static private class ReadControl extends BooleanControl
    {
        public ReadControl() {
            super(AUTOMATION_READ_ID, getString("Read"), false);
            setAnnotation(getName().substring(0, 1).toLowerCase());
	        setStateColor(true, Color.YELLOW);
        }
    }

    static private class WriteControl extends BooleanControl
    {
        public WriteControl() {
            super(AUTOMATION_WRITE_ID, getString("Write"), false);
            setAnnotation(getName().substring(0, 1).toLowerCase());
	        setStateColor(true, Color.RED);
        }
    }

    static private class RecallControl extends BooleanControl
    {
        public RecallControl() {
            super(AUTOMATION_RECALL_ID, getString("Recall"), true);
            setAnnotation(getName().substring(0, 1));
	        setStateColor(true, new Color(255, 255, 175));
        }
    }

    static private class StoreControl extends BooleanControl
    {
        public StoreControl() {
            super(AUTOMATION_STORE_ID, getString("Store.As"), true);
            setAnnotation(getName().substring(0, 1));
	        setStateColor(true, Color.pink);
        }
    }
}


