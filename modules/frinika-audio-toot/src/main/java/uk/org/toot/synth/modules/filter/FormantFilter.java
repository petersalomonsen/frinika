// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import java.util.List;

/**
 * A composition of StateVariableFilterElements.
 * They have individual frequencies and levels and a shared resonance.
 * The frequencies can be shifted by a shared factor, 2 octaves up and down.
 * @author st
 *
 */
public class FormantFilter
{
	private final FormantFilterVariables vars;
	private List<StateVariableFilterElement> filters;
	private float[] fn;
	private float[] level;
	private float res;
	private final int nBands;
	
	public FormantFilter(FormantFilterVariables vars) {
		this.vars = vars;
		nBands = vars.size();
		filters = new java.util.ArrayList<StateVariableFilterElement>();
		for ( int i = 0; i < nBands; i++) {
			StateVariableFilterElement element = new StateVariableFilterElement();
			element.bp = true;
			filters.add(element);
		}
		fn = new float[nBands];
		level = new float[nBands];
	}
	
	public float filter(float sample) {
		float out = 0;
		int i = 0;
		for ( StateVariableFilterElement filter : filters ) {
			out += filter.filter(sample, fn[i], res) * level[i];
			i += 1;
		}
		return out;
	}

	public void setSampleRate(int rate) {
		vars.setSampleRate(rate);
	}

	public void update() {
		float fshift = vars.getFreqencyShift();
		res = 1f - vars.getResonance(); // 1 is min res, 0 is max res !
		for ( int i = 0; i < nBands; i++ ) {
			level[i] = vars.getLevel(i);
			fn[i] = vars.getFrequency(i) * fshift;
		}	
	}
}
