// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import java.awt.Color;
import java.util.List;
import uk.org.toot.control.*;

import static uk.org.toot.misc.Localisation.*;

/**
 * Provides tap list per channel.
 * Implemented to experiment with per channel control handling.
 */
public class MultiTapDelayStereoControls extends AbstractDelayControls
    implements MultiTapDelayProcess.Variables
{
    private List<MultiTapDelayControls> perChannelControls;
    private float msMax;
    private final static ControlLaw DELAY_FACTOR_LAW = new LogLaw(0.2f, 5f, "");
    private FloatControl delayFactorControl;
    private float delay;

    public MultiTapDelayStereoControls() {
        this(3, 2000f); // 3 taps, 2 seconds max delay
    }

	public MultiTapDelayStereoControls(int ntaps, float ms) {
        super(DelayIds.MULTI_TAP_DELAY_ID, getString("Stereo.Multi.Tap.Delay"));
        msMax = ms;
        perChannelControls = new java.util.ArrayList<MultiTapDelayControls>();
        for ( int a = 0; a < 2 ; a++ ) {
            String name = (a == 0) ? getString("Left") : getString("Right"); // !!! !!!
            MultiTapDelayControls c = new MultiTapDelayControls(a*16, ntaps, msMax, name);
            add(c);
            perChannelControls.add(c);
        }
        delayFactorControl = new FloatControl(DELAY_FACTOR_ID, getString("Delay"), DELAY_FACTOR_LAW, 0.01f, 1f);
        delayFactorControl.setInsertColor(Color.RED.darker());
        add(delayFactorControl);
        derive(delayFactorControl);
        // feedback
        // mix
        add(createCommonControlColumn(false)); // no inverts
    }

    @Override
    protected void derive(Control c) {
    	switch ( c.getId() ) {
    	case DELAY_FACTOR_ID: delay = delayFactorControl.getValue(); break;
    	default: super.derive(c); break;
    	}
    }
    
    public float getMaxDelayMilliseconds() { return msMax * 5; }

    public List<DelayTap> getTaps(int chan) {
        // no validation, called on server thread, time critical
        return perChannelControls.get(chan).getTaps();
    }

    public int getChannelCount() { return 2; } // !!! !!!

    public float getDelayFactor() {
		return delay;
    }
}
