// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.audio.delay;

import java.awt.Color;
import java.util.List;

import org.tritonus.share.sampled.TVolumeUtils;

import uk.org.toot.control.Control;
import uk.org.toot.control.EnumControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;
import uk.org.toot.control.LogLaw;

import static uk.org.toot.misc.Localisation.*;
import static uk.org.toot.audio.delay.DelayIds.TEMPO_DELAY_ID;

public class TempoDelayControls extends AbstractDelayControls
	implements TempoDelayProcess.Variables
{
	private final static LinearLaw DUCKING_LAW = new LinearLaw(0, 20, "dB");
	private final static LogLaw CUTOFF_LAW = new LogLaw(200, 20000, "Hz");
	
	private final static int DUCKING_ID = 0;
	private final static int LOWPASS_ID = 1;
	
	private FloatControl duckingControl, lowpassControl;
	private float ducking, lowpassCoeff;	
	private float delayFactor = 1f;
	
	public TempoDelayControls() {
		super(TEMPO_DELAY_ID, getString("BPM.Delay"));
		ControlColumn col = new ControlColumn();
		col.add(new DelayFactorControl());
		col.add(createFeedbackControl());
		col.add(createMixControl());
		add(col);
		ControlColumn col2 = new ControlColumn();
		col2.add(duckingControl = createDuckingControl());
		col2.add(lowpassControl = createLowpassControl());
		add(col2);
		derive(duckingControl);
		derive(lowpassControl);
	}

    @Override
    protected void derive(Control c) {
    	switch ( c.getId() ) {
    	case DUCKING_ID:
    		// for 0dB .. 40dB ducking should be 1 .. 0.01
    		ducking = (float)TVolumeUtils.log2lin(-duckingControl.getValue()); 
    		break;
    	case LOWPASS_ID: 
    		float freq = lowpassControl.getValue();
    		lowpassCoeff = 1f - (float)Math.exp(-2.0*Math.PI*freq/44100); // !!!
    		break;
    	default: super.derive(c); break;
    	}
    }
    
	protected FloatControl createDuckingControl() {
		FloatControl control = new FloatControl(DUCKING_ID, "Ducking", DUCKING_LAW, 0.01f, 0f);
		control.setInsertColor(Color.LIGHT_GRAY);
		return control;
	}
	
	protected FloatControl createLowpassControl() {
		FloatControl control = new FloatControl(LOWPASS_ID, "Filter", CUTOFF_LAW, 1f, 8000f);
		control.setInsertColor(Color.YELLOW);
		return control;
	}
	
	public float getDucking() {
		return ducking;
	}
	
	public float getLowpassCoefficient() {
		return lowpassCoeff;
	}
	
	public float getDelayFactor() {
		return delayFactor;
	}

	public float getMaxDelayMilliseconds() {
		return 2000; // !!!
	}

	private static List<NamedFactor> factors = new java.util.ArrayList<NamedFactor>();
	private static NamedFactor defaultFactor = new NamedFactor(1f, "1/4");

	static {
		factors.add(new NamedFactor(0.25f, "1/16"));
		factors.add(new NamedFactor(0.5f/3, "1/8T"));
		factors.add(new NamedFactor(0.5f, "1/8"));
		factors.add(new NamedFactor(1f/3, "1/4T"));
		factors.add(defaultFactor);
		factors.add(new NamedFactor(2f/3, "1/2T"));
		factors.add(new NamedFactor(2f, "1/2"));
	};
	
	protected class DelayFactorControl extends EnumControl
	{
		
		public DelayFactorControl() {
			super(DELAY_FACTOR_ID, getString("Delay"), defaultFactor);
		}

		@Override
		public List getValues() {
			return factors;
		}
		
		@Override
		public void setValue(Object value) {
			super.setValue(value);
			delayFactor = ((NamedFactor)value).getFactor();
		}
		
		@Override
		public boolean hasLabel() { return true; }
	}

	protected static class NamedFactor
	{
		private float factor;
		private String name;
		
		public NamedFactor(float factor, String name) {
			this.factor = factor;
			this.name = name;
		}
		
		public float getFactor() { return factor; }
		
		public String toString() { return name; }
	}
	
}
