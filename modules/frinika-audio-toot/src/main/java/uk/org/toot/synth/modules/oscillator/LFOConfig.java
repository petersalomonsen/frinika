// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth.modules.oscillator;

/**
 * Avoids passing lots of stuff into constructors
 * @author st
 *
 */
public class LFOConfig 
{
	public float rateMin = 0.01f;
	public float rateMax = 1f;
	public float rate = 0.1f;
	public float deviationMax = 0f;
	public float deviation = 0f;
}
