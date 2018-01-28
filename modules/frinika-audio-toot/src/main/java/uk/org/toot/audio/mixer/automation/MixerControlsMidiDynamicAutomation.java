// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import uk.org.toot.audio.core.AudioControlsChain; // !!!
import uk.org.toot.audio.mixer.MixerControls;
import uk.org.toot.control.*;
import javax.sound.midi.*;

import static uk.org.toot.control.automation.ControlSysexMsg.*;

/**
 * Redefines the dynamic API in terms of MidiMessages.
 **/
abstract public class MixerControlsMidiDynamicAutomation
    extends MixerControlsConciseDynamicAutomation
{
//    private boolean _debugStore = false;

    public MixerControlsMidiDynamicAutomation(MixerControls controls) {
        super(controls);
    }

    protected Control findControl(String name, MidiMessage msg) {
        if ( !isControl(msg) ) return null;
        AudioControlsChain strip = getStrip(name);
        if ( strip == null ) return null;
        return findControl(strip, getProviderId(msg), getModuleId(msg),
            getInstanceIndex(msg), getControlId(msg));
    }

    protected void read(String name, MidiMessage msg) {
        if ( !isControl(msg) ) return;
        read(name, getProviderId(msg), getModuleId(msg), getInstanceIndex(msg),
            getControlId(msg), getValue(msg));
    }

    protected void write(String name, int providerId, int moduleId, int instanceIndex,
        				 int controlId, int value) {
        try {
	        MidiMessage msg = createControl(
   		        providerId, moduleId, instanceIndex, controlId, value);
      	  	write(name, msg);
        } catch ( InvalidMidiDataException imde ) {
        }
    }

    abstract protected void write(String name, MidiMessage msg);
}
