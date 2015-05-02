// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import uk.org.toot.control.*;
import uk.org.toot.audio.core.*;
import uk.org.toot.audio.mixer.MixerControls;
import java.util.Observer;
import java.util.Observable;

/**
 * This class ensures all mixer strips have AutomationControls which are
 * initially empty. Subclasses implement ensureAutomationControls() to
 * ensure the correct controls are available.
 */
abstract public class BasicAutomation
{
    protected MixerControls mixerControls;
    private MutationObserver mutationObserver;

    /** @link dependency */
    /*#AutomationControls lnkAutomationControls;*/

    public BasicAutomation(MixerControls controls) {
        mixerControls = controls;
        // add automation controls to each initial strip
        for ( Control c : mixerControls.getControls() ) {
            if ( c instanceof AudioControlsChain ) {
                ensureControls((AudioControlsChain)c);
            }
        }
        // ensure dynamic strips are handled too
        mutationObserver = new MutationObserver();
        mixerControls.addObserver(mutationObserver);
    }

    protected void ensureControls(AudioControlsChain strip) {
        AutomationControls autoc = strip.find(AutomationControls.class);
        if ( autoc == null ) {
            autoc = new AutomationControls();
            strip.add(0, autoc); // insert at start of strip
        }
        ensureAutomationControls(autoc);
    }

    /**
     * Implement this method to ensure the correct sort of controls
     * are available.
     */
    abstract protected void ensureAutomationControls(AutomationControls c);

    protected class MutationObserver implements Observer
    {
        public void update(Observable obs, Object obj) {
            if ( obj instanceof MixerControls.Mutation ) {
                MixerControls.Mutation m = (MixerControls.Mutation)obj;
                if ( m.getOperation() == MixerControls.Mutation.ADD ) {
                    CompoundControl cc = m.getControl();
                    if ( cc instanceof AudioControlsChain ) {
                        ensureControls((AudioControlsChain)cc);
                    }
                }
            }
        }
    }
}
