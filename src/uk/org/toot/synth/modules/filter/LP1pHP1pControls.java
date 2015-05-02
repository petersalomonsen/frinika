// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.misc.Localisation.getString;

import java.awt.Color;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LogLaw;

public class LP1pHP1pControls extends CompoundControl 
	implements LP1pHP1pVariables
{
    private final static ControlLaw LP_LAW = new LogLaw(1f, 16f, "");
    private final static ControlLaw HP_LAW = new LogLaw(0.25f, 4f, "");
	private final static int LPRATIO = 0;
	private final static int HPRATIO = 1;
	
	private FloatControl lpRatioControl;
	private FloatControl hpRatioControl;
	
	private float hpRatio, lpRatio;
	
	protected int idOffset = 0;
	
	private int sampleRate = 44100;
	
	public LP1pHP1pControls(int id, int instanceIndex, String name, final int idOffset) {
		super(id, instanceIndex, name);
		this.idOffset = idOffset;
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
	}

	protected void derive(Control c) {
		switch ( c.getId() - idOffset ) {
		case LPRATIO: lpRatio = deriveLPRatio(); break;
		case HPRATIO: hpRatio = deriveHPRatio(); break;
		}		
	}
	
	protected void createControls() {
		add(lpRatioControl = createLPRatioControl());
		add(hpRatioControl = createHPRatioControl());
	}

	protected void deriveSampleRateIndependentVariables() {
		lpRatio = deriveLPRatio();
		hpRatio = deriveHPRatio();
	}

	protected void deriveSampleRateDependentVariables() {
	}

	protected float deriveLPRatio() {
		return lpRatioControl.getValue();
	}

	protected float deriveHPRatio() {
		return hpRatioControl.getValue();
	}

	protected FloatControl createLPRatioControl() {
        FloatControl control = new FloatControl(LPRATIO+idOffset, getString("LP"), LP_LAW, 1f, 4);
        control.setInsertColor(Color.yellow);
        return control;		
	}

	protected FloatControl createHPRatioControl() {
        FloatControl control = new FloatControl(HPRATIO+idOffset, getString("HP"), HP_LAW, 0.1f, 1f);
        control.setInsertColor(Color.yellow);
        return control;		
	}

	public void setSampleRate(int rate) {
		if ( sampleRate != rate ) {
			sampleRate = rate;
			deriveSampleRateDependentVariables();
		}
	}

	public float getHighPassRatio() {
		return hpRatio;
	}

	public float getLowPassRatio() {
		return lpRatio;
	}

}
