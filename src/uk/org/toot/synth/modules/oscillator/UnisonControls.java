// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

import java.awt.Color;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.IntegerControl;
import uk.org.toot.control.IntegerLaw;
import uk.org.toot.control.LinearLaw;

import static uk.org.toot.synth.modules.oscillator.OscillatorIds.UNISON_ID;
import static uk.org.toot.misc.Localisation.*;

/**
 * This class provided controls for number of oscillators, pitch spread.
 * @author st
 */
public class UnisonControls extends CompoundControl implements UnisonVariables
{
	public final static int OSC_COUNT = 0;
	public final static int PITCH_SPREAD = 1;
	public final static int PHASE_SPREAD = 2;
	
	private final static IntegerLaw OSC_COUNT_LAW = new IntegerLaw(1, 32, "");
	private final static LinearLaw SPREAD_LAW = LinearLaw.UNITY;
	
	private IntegerControl oscillatorCountControl;
	private FloatControl pitchSpreadControl;
	private FloatControl phaseSpreadControl;
	
	private int idOffset;
	private int oscillatorCount;
	private float pitchSpread;
	private float phaseSpread;

	public UnisonControls(final int idOffset) {
		super(UNISON_ID, 0, getString("Unison"));
		this.idOffset = idOffset;
		createControls();
		deriveSampleRateIndependentVariables();
	}
	
    @Override
    protected void derive(Control c) {
		switch ( c.getId() - idOffset ) {
		case OSC_COUNT: oscillatorCount = deriveOscillatorCount(); break;
		case PITCH_SPREAD: pitchSpread = derivePitchSpread(); break;
		case PHASE_SPREAD: phaseSpread = derivePhaseSpread(); break;
		}    	
    }
    
	private void createControls() {
		add(oscillatorCountControl = createOscillatorCountControl());
		add(pitchSpreadControl = createPitchSpreadControl());
		add(phaseSpreadControl = createPhaseSpreadControl());
		derive(oscillatorCountControl);
		derive(pitchSpreadControl);
		derive(phaseSpreadControl);
	}
	
	protected IntegerControl createOscillatorCountControl() {
		IntegerControl control = new IntegerControl(OSC_COUNT+idOffset, getString("Oscs"), OSC_COUNT_LAW, 1f, 1);
		control.setInsertColor(Color.CYAN.darker());
		return control;
	}
	
	protected FloatControl createPitchSpreadControl() {
		FloatControl control = new FloatControl(PITCH_SPREAD+idOffset, getString("Detune"), SPREAD_LAW, 0.01f, 0f);
		control.setInsertColor(Color.MAGENTA.darker());
		return control;
	}
	
	protected FloatControl createPhaseSpreadControl() {
		FloatControl control = new FloatControl(PHASE_SPREAD+idOffset, getString("Phase"), SPREAD_LAW, 0.01f, 1f);
		control.setInsertColor(Color.BLUE);
		return control;
	}
	
	private void deriveSampleRateIndependentVariables() {
		oscillatorCount = deriveOscillatorCount();
		pitchSpread = derivePitchSpread();
		phaseSpread = derivePhaseSpread();
	}

	protected int deriveOscillatorCount() {
		return oscillatorCountControl.getUserValue();
	}
	
	protected float derivePitchSpread() {
		return pitchSpreadControl.getValue();
	}
	
	protected float derivePhaseSpread() {
		return phaseSpreadControl.getValue();
	}
	
	public int getOscillatorCount() {
		return oscillatorCount;
	}

	public float getPitchSpread() {
		return pitchSpread;
	}

	public float getPhaseSpread() {
		return phaseSpread;
	}
}
