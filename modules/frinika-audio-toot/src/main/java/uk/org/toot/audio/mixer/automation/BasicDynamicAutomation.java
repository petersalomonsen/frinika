// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import uk.org.toot.audio.mixer.MixerControls;

/**
 * This class ensures mixer strips have dynamic automation controls.
 */
abstract public class BasicDynamicAutomation extends BasicAutomation
{
    public BasicDynamicAutomation(MixerControls controls) {
        super(controls);
    }

    protected void ensureAutomationControls(AutomationControls autoc) {
        autoc.ensureDynamicControls();
    }
}
