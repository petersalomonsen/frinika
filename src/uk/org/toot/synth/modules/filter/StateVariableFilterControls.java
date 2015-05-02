// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.modules.filter.FilterControlIds.*;

import java.awt.Color;

import uk.org.toot.control.BooleanControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class StateVariableFilterControls extends FilterControls
	implements StateVariableFilterVariables
{
	private FloatControl modeMixControl;
	private BooleanControl bandModeControl;
	
	private float modeMix;
	private boolean bandMode;
	
	public StateVariableFilterControls(int instanceIndex, String name, final int idOffset) {
		super(FilterIds.STATE_VARIABLE_FILTER_ID, instanceIndex, name, idOffset);
	}

	protected void derive(Control c) {
		switch ( c.getId() - idOffset ) {
		case MODE_MIX: modeMix = deriveModeMix();    break;
		case BAND_MODE: bandMode = deriveBandMode(); break;
		default: super.derive(c); break;
		}
	}

	protected void createControls() {
		super.createControls();
		add(modeMixControl = createModeMixControl());
		add(bandModeControl = createBandModeControl());
	}
	
	protected void deriveSampleRateIndependentVariables() {
		super.deriveSampleRateIndependentVariables();
		modeMix = deriveModeMix();
		bandMode = deriveBandMode();
	}
	
	// damp = MIN(2.0*(1.0 - pow(res, 0.25)), MIN(2.0, 2.0/freq - freq*0.5));
	// stability correction must be done in real-time
	protected float deriveResonance() {
		return (float)(2 * (1f - Math.pow(super.deriveResonance(), 0.25)));
	}

	protected float deriveModeMix() {
		return modeMixControl.getValue();
	}
	
	protected boolean deriveBandMode() {
		return bandModeControl.getValue();
	}
	
	protected FloatControl createModeMixControl() {
        FloatControl control = new FloatControl(MODE_MIX+idOffset, getString("Mix"), LinearLaw.UNITY, 0.01f, 0f);
        control.setInsertColor(Color.LIGHT_GRAY);
        return control;				
	}

	protected BooleanControl createBandModeControl() {
		BooleanControl control = new BooleanControl(BAND_MODE+idOffset, getString("Band.Pass"), bandMode);
		control.setAnnotation("B");
		control.setStateColor(true, Color.PINK);
		return control;
	}
	
	public float getModeMix() {
		return modeMix;
	}
	
	public boolean isBandMode() {
		return bandMode;
	}
}
