// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.mixer.automation;

import uk.org.toot.audio.mixer.MixerControls;
import javax.sound.midi.MidiMessage;

import static uk.org.toot.control.automation.ControlSysexMsg.*;

/**
 * Writes MIDI automation data to System.out for testing purposes
 **/
public class TestMixerControlsMidiDynamicAutomation
    extends MixerControlsMidiDynamicAutomation 
{
    public TestMixerControlsMidiDynamicAutomation(MixerControls controls) {
        super(controls);
    }

    protected void write(String name, MidiMessage msg) {
        System.out.println(name+
            "vId="+getProviderId(msg)+", mId="+getModuleId(msg)+", ii="+getInstanceIndex(msg)+
            ", cId="+getControlId(msg)+", "+getValue(msg));
    }
}
