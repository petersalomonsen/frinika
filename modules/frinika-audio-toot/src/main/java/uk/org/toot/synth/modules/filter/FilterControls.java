// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.misc.Localisation.getString;
import static uk.org.toot.synth.modules.filter.FilterControlIds.*;

import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlLaw;
import uk.org.toot.control.FloatControl;
import uk.org.toot.control.LinearLaw;

public class FilterControls extends CompoundControl 
	implements FilterVariables
{
    private final static ControlLaw SEMITONE_LAW = new LinearLaw(-48, 96, "semitones");
    
	private FloatControl cutoffControl;
	private FloatControl resonanceControl;
	
	private float cutoff, resonance;
	
	protected int idOffset = 0;
	
	private int sampleRate = 44100;
	
	public FilterControls(int id, int instanceIndex, String name, final int idOffset) {
		super(id, instanceIndex, name);
		this.idOffset = idOffset;
		createControls();
		deriveSampleRateIndependentVariables();
		deriveSampleRateDependentVariables();
	}

    @Override
    protected void derive(Control c) {
		switch ( c.getId()-idOffset ) {
		case FREQUENCY: cutoff = deriveCutoff(); break;
		case RESONANCE: resonance = deriveResonance(); break;
		}		
	}
	
	protected void createControls() {
		add(cutoffControl = createCutoffControl());
		add(resonanceControl = createResonanceControl());
	}

	protected void deriveSampleRateIndependentVariables() {
		resonance = deriveResonance();
		cutoff = deriveCutoff();
	}

	protected float deriveResonance() {
		return resonanceControl.getValue();
	}

	protected void deriveSampleRateDependentVariables() {
	}

	protected float deriveCutoff() {
		return cutoffControl.getValue();
	}

	protected FloatControl createCutoffControl() {
        return new FloatControl(FREQUENCY+idOffset, getString("Cutoff"), SEMITONE_LAW, 1f, 0f);
	}

	protected FloatControl createResonanceControl() {
        return new FloatControl(RESONANCE+idOffset, getString("Resonance"), LinearLaw.UNITY, 0.01f, 0.25f);
	}

	public float getCutoff() {
		return cutoff;
	}

	public float getResonance() {
		return resonance;
	}

	public void setSampleRate(int rate) {
		if ( sampleRate != rate ) {
			sampleRate = rate;
			deriveSampleRateDependentVariables();
		}
	}

}
