// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.core;

import uk.org.toot.control.*;

/**
 * AudioControls are composite Controls which control an AudioProcess, defined
 * in 'user' terms. They add a bypass control.
 */
public abstract class AudioControls extends CompoundControl
{
	private CompoundControl.BypassControl bypassControl = null;

    public AudioControls(int id, String name) {
        this(id, name, 127);
    }

    public AudioControls(int id, String name, int bypassId) {
        super(id, name);
        if ( canBypass() ) {
            bypassControl = new BypassControl(bypassId);
            add(bypassControl);
        }
    }

    public boolean hasOrderedFrequencies() { return false; }

    public boolean canBeMinimized() { return true; }
    
	public boolean canBypass() { return true; }

    public void setBypassed(boolean state) {
        if ( canBypass() && bypassControl != null ) {
        	bypassControl.setValue(state);
        }
    }

    public boolean isBypassed() {
        if ( bypassControl == null ) return false;
        return bypassControl.getValue();
    }

    public BooleanControl getBypassControl() {
        return bypassControl;
    }
    
    public String getPersistenceDomain() { return "audio"; }
}
