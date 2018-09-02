// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.tool;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LogLaw;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.audio.tool.ToolIds.SINE_ID;

/**
 * @author st
 *
 */
public class SineControls extends AudioControls implements SineProcess.Variables
{
    private final static int FREQ = 0;
    
    private final static ControlLaw FREQ_LAW = new LogLaw(50f, 5000f, "Hz");

    private FreqControl freqControl;
    private float frequency;
    
	public SineControls() {
		super(SINE_ID, getString("Sine"));
        ControlColumn col = new ControlColumn();
        col.add(freqControl = new FreqControl());
        add(col);
	}
    
    @Override
    protected void derive(Control c) {
        switch ( c.getId() ) {
        case FREQ: frequency = deriveFrequency(); break;
        }
    }
    
    protected float deriveFrequency() {
        return freqControl.getValue();
    }
    
    public int getFrequency() {
        return (int)frequency;
    }
    
    private static class FreqControl extends FloatControl
    {
        private final static String[] names =
            { "91", "100", "300", "910", "1000", "3000" };
        
        public FreqControl() {
            super(FREQ, getString("Frequency"), FREQ_LAW, 1f, 910f);
        }

        public String[] getPresetNames() {
            return names;
        }

        public void applyPreset(String name) {
            setValue(Integer.parseInt(name));
        }        
    }
}
