// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.dsp.FastMath.*;

public class StateVariableFilter extends AbstractFilter
{
	private StateVariableFilterElement element;
	private float res;

	public StateVariableFilter(StateVariableFilterVariables variables) {
		super(variables);
		element = new StateVariableFilterElement();
	}
	
	public float update() {
		res = vars.getResonance();
		element.mix = ((StateVariableFilterVariables)vars).getModeMix();
		element.bp = ((StateVariableFilterVariables)vars).isBandMode();
		return vars.getCutoff();
	}

	/*
	 * res    = resonance 0 to 1; 
	 * drive  = internal distortion 0 to 0.1
	 * freq   = 2.0*sin(PI*MIN(0.25, fc/(fs*2)));  // the fs*2 is because it's double sampled
	 * damp   = MIN(2.0*(1.0 - pow(res, 0.25)), MIN(2.0, 2.0/freq - freq*0.5)); 
	 */
	public float filter(float sample, float f) {
		// the /4 is because it's double sampled
		float f1 = 2f * sin((float)(Math.PI * min(0.24f, f*0.25f)));  
		// Thanks to Laurent de Soras for the stability limit
		return element.filter(sample, f1, min(res, min(1.9f, 2f/f1 - f1*0.5f)));
	}

}
