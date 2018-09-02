// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import uk.org.toot.audio.core.AudioControls;

import static uk.org.toot.audio.mixer.MixerControlsIds.*;

/**
 * Currently unused.
 * Eventually for Snap/Keep options?
 */
// !!! it isn't really an AudioControl is it?
public class GlobalDynamicAutomationControls extends AudioControls
{
    public GlobalDynamicAutomationControls() {
        super(CONTROL_STRIP_ID, "Auto");
    }
    
	public boolean canBypass() { return false; }
}
