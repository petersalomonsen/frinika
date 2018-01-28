// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.filter;

import static uk.org.toot.synth.modules.filter.FilterType.*;

public class DualStateVariableFilterElement
{
	private float prev = 0f;
	private float low1, high1, band1, notch1;
	public float low, high, band, notch;
	
	public float filter(float in, DualStateVariableFilterConfig config) {
		float i1 = (prev + in) * 0.5f; // linearly interpolated double sampled
		prev = in;
		
		// first filter, interpolated sample
		notch1 = i1 - config.damp1 * band1;
		low1   = low1 + config.freq1 * band1;								
		high1  = notch1 - low1;									
		band1  = config.freq1 * high1 + band1; // - drive*band1*band1*band1;
		
		if ( config.type2 != OFF ) {
			// second filter
			switch ( config.type1 ) {
			case LOW: i1 = low1; break;
			case HIGH: i1 = high1; break;
			case NOTCH: i1 = notch1; break;
			case BAND: i1 = band1; break;
			case PEAK: i1 += band1; break;
			}		
			notch = i1 - config.damp2 * band;
			low   = low + config.freq2 * band;								
			high  = notch - low;									
			band  = config.freq2 * high + band; // - drive*band*band*band;
			// discarded due to downsampling
		}

		// first filter, real sample
		notch1 = in - config.damp1 * band1;
		low1   = low1 + config.freq1 * band1;								
		high1  = notch1 - low1;									
		band1  = config.freq1 * high1 + band1; // - drive*band1*band1*band1;	

		if ( config.type2 == OFF ) {
			switch ( config.type1 ) {
			case LOW: return low1;
			case HIGH: return high1;
			case NOTCH: return notch1;
			case BAND: return band1;
			case PEAK: return in + band1;			
			}
		}
		
		// second filter
		switch ( config.type1 ) {
		case LOW: i1 = low1; break;
		case HIGH: i1 = high1; break;
		case NOTCH: i1 = notch1; break;
		case BAND: i1 = band1; break;
		case PEAK: i1 = in + band1; break;
		}
		notch = i1 - config.damp2 * band;
		low   = low + config.freq2 * band;								
		high  = notch - low;									
		band  = config.freq2 * high + band; // - drive*band*band*band;
		
		switch ( config.type2 ) {
		case LOW: return low;
		case HIGH: return high;
		case NOTCH: return notch;
		case BAND: return band;
		case PEAK: 
			float tmp = i1 + band;
			return config.type1 == PEAK ? tmp * 0.5f : tmp; // hack for P4 gain boost!!!
		}
		return in; // shouldn't arrive here					
	}

}
