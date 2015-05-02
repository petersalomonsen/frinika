// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.dsp.FastMath.*;

public class DualStateVariableFilter extends AbstractFilter
{
	private DualStateVariableFilterElement element;
	private DualStateVariableFilterConfig config;
	private float res;

	public DualStateVariableFilter(DualStateVariableFilterVariables variables) {
		super(variables);
		element = new DualStateVariableFilterElement();
	}
	
	public float update() {
		config = ((DualStateVariableFilterVariables)vars).getType();
		res = vars.getResonance();
		return vars.getCutoff();
	}

	/*
	 * res    = resonance 0 to 1; 
	 * drive  = internal distortion 0 to 0.1
	 * freq   = 2.0*sin(PI*MIN(0.25, fc/(fs*2)));  // the fs*2 is because it's double sampled
	 * damp   = MIN(2.0*(1.0 - pow(res, 0.25)), MIN(2.0, 2.0/freq - freq*0.5)); 
	 */
	public float filter(float sample, float fn1) {
		// Thanks to Laurent de Soras for the stability limit
		float f1 = 2f * sin((float)(Math.PI * min(0.24f, fn1*0.25f)));
		config.freq1 = f1;
		config.damp1 = min(res, min(1.9f, 2f/f1 - f1*0.5f));
		if ( config.type2 > FilterType.OFF && config.f2ratio != 1f ) {
			float fn2 = fn1 * config.f2ratio;
			float f2 = 2f * sin((float)(Math.PI * min(0.24f, fn2*0.25f)));
			config.freq2 = f2;
			config.damp2 = min(res, min(1.9f, 2f/f2 - f2*0.5f));
		} else {
			config.freq2 = config.freq1;
			config.damp2 = config.damp1;
		}
		return element.filter(sample, config);
	}

}
