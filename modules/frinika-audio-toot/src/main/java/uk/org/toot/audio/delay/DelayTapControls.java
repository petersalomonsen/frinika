// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import uk.org.toot.control.*;

import static uk.org.toot.misc.Localisation.*;

public class DelayTapControls extends CompoundControl
    implements DelayTap
{
    private FloatControl delayControl;
    private FloatControl levelControl;

    // because we're used more than once
    // our user has to tell us our id
    // which should be incremented by 2 for each one of us
    public DelayTapControls(int id, ControlLaw law) {
        super(id, ""); // ??? ??? id ??? and used below
        // add delay control (ms)
        delayControl = new FloatControl(id, getString("Delay"), law, 0.1f, law.getMaximum()/4); // !!! initial value
        add(delayControl);
        // add feedback control
        levelControl = new FloatControl(id+1, getString("Level"), LinearLaw.UNITY, 0.01f, 0f);
        add(levelControl);
    }

    public boolean isAlwaysVertical() { return true; }

    public float getDelayMilliseconds() {
        return delayControl.getValue();
    }

    public float getLevel() {
        return levelControl.getValue();
    }
}
