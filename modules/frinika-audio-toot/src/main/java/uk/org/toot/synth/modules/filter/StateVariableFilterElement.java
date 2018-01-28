// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

/*
 * Classic digital state variable filter as designed by Chamblerin.
 * 2x oversampled to assist resonance stability and cutoff tracking.
 * Denormal handling is the responsibility of the user.
 * In some applications multiple elements are used in parallel and
 * denormal handling is then more efficient by not being performed here.
 * In other cases a single element is internal to a Voice which is
 * terminated prior to reaching silence.
 */
public class StateVariableFilterElement
{
	private float prev = 0f;
	private float low, high, band, notch;

	boolean bp = false;
	float mix = 0f;
	
	public float filter(float in, float freq, float damp) {
		float i1 = (prev + in) * 0.5f; // linearly interpolated double sampled
		prev = in;
		notch = i1 - damp * band;
		low   = low + freq * band;								
		high  = notch - low;									
		band  = freq * high + band; // - drive*band*band*band;	
		notch = in - damp * band;
		low   = low + freq * band;								
		high  = notch - low;									
		band  = freq * high + band; // - drive*band*band*band;	
		return bp ? band : (1f-mix)*low + mix*high;					
	}

}
