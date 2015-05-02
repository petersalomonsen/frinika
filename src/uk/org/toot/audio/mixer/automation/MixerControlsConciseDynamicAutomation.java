// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import uk.org.toot.audio.core.*;
import uk.org.toot.control.*;
import uk.org.toot.audio.mixer.MixerControls;

/**
 * Converts the superclass Control/value API to individual parameters required
 * to specify the Control path concisely as required for MIDI Sysex
 * representation. BUT, not MIDI-specific.
 **/
abstract public class MixerControlsConciseDynamicAutomation
    extends MixerControlsDynamicAutomation
{
    protected MixerControlsConciseDynamicAutomation(MixerControls controls) {
        super(controls);
    }

    protected void write(Control c) {
        if ( c.isIndicator() ) return; // don't automate indicators
        int value = c.getIntValue();
        if ( value < 0 ) return; // illegal value
        int controlId = c.getId();
        if ( controlId < 0 ) return; // don't automate negative ids

        AudioControls module = findModule(c);
        int providerId = module.getProviderId();
        int moduleId = module.getId();
        int instanceIndex = module.getInstanceIndex();

        AudioControlsChain strip = (AudioControlsChain)module.getParent();

        AutomationControls ac = strip.find(AutomationControls.class);
        if ( ac == null || !ac.canWrite() ) return;

        write(strip.getName(), providerId, moduleId, instanceIndex, controlId, value);
    }

    protected boolean canRead(AudioControlsChain strip) {
        AutomationControls autoc = strip.find(AutomationControls.class);
   	    if ( autoc != null && !autoc.canRead() ) return false;
        return true;
    }

    protected void read(String name,
	        int providerId, int moduleId, int instanceIndex,
    	    int controlId, int value) {
        AudioControlsChain strip = getStrip(name);
        if ( strip == null ) return;
		if ( !canRead(strip) ) return; // read disabled
        Control c = findControl(strip, providerId, moduleId, instanceIndex, controlId);
        if ( c == null || c.getIntValue() == value ) return;
//        System.out.println("Recalling ["+providerId+":"+moduleId+":"+instanceIndex+":"+controlId+"] "+c.getControlPath()+" : "+value);
        // set its value
        super.read(c, value);
    }

    protected abstract void write(String name,
        int providerId, int moduleId, int instanceIndex,
        int controlId, int value);

    protected AudioControlsChain getStrip(String name) {
        CompoundControl cc = mixerControls.getStripControls(name);
        if ( !(cc instanceof AudioControlsChain) ) return null;
        return (AudioControlsChain)cc;
    }

    protected Control findControl(AudioControlsChain strip,
	        int providerId, int moduleId, int instanceIndex, int controlId) {
        // drill down to the specific control that matches other arguments
        // providerId, moduleId, instanceIndex, controlId !!! !!!
        // linear search of strip for providerId, moduleId, instanceId
		CompoundControl module = strip.find(providerId, moduleId, instanceIndex);
        if ( module == null ) return null;
        // tree search for controlId
        return module.deepFind(controlId);
    }

    protected AudioControls findModule(Control c) {
		// CoR: work back to AudioControls plugged into an AudioControlsChain strip
        CompoundControl cc = c.getParent();
        while ( !(cc.getParent() instanceof AudioControlsChain) ) {
            cc = cc.getParent();
        }
        return (AudioControls)cc;
    }
}
