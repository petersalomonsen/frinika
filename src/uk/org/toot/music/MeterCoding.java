// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package uk.org.toot.music;

/**
 * A MeterCoding helps in the representation of time signatures as ints.
 * By definition a time signature specifies the timing of a bar so there can
 * only be one time signature per bar.
 * The value of the time signature is equal to to tick length of the bar.
 * Only X/4, X/8 and X/16 time signatures are currently represented.
 * X/4 % 24 == 0 else
 * X/8 % 12 == 0 else
 * X/16 % 6 == 0
 * Compound time signatures are not represented, they take the same time as
 * normal time signatures but are divided into triplets, which is an orthogonal
 * issue. We don't care where notes are in a bar, just how long the bar is.
 * 
 * 
 * @author st
 *
 * 0	invalid
 * 6	1/16	so x/16 TWO = 6
 * 12   1/8		so x/8 TWO = 12
 * 24	1/4		so x/4 TWO = 24
 * 36	3/8
 * 48   2/4		simple (recursive binary subdivision)
 * 60	5/8		
 * 72	3/4
 * 84	7/8
 * 96	4/4		simple
 * 108	9/8
 * 120	5/4
 * 132	11/8
 * 144	6/4
 * 156	13/8
 * 168	7/4
 * 180	15/8
 * 192	8/4		simple but uncommon
 * 204	17/8
 * 216  9/4
 * 228  19/8
 * 240	10/4
 * 252	21/8
 */
public abstract class MeterCoding
{
	public final static int COMMON_TIME = createMeter(4, 4);
	
	public static int createMeter(int upper, int lower) {
		if ( lower != 4 && lower != 8 && lower != 16 )
			throw new IllegalArgumentException("lower must be 4, 8 or 16");
		int sig = upper * 96 / lower;
		if ( sig < 6 || sig > 255 )
			throw new IllegalArgumentException(upper+"/"+lower+" is invalid");
		return sig;
	}
	
	public static String asString(int coded) {
		int upper;
		int lower;
		if ( (coded % 24) == 0 ) {
			lower = 4;
			upper = coded / 24;
		} else if ( (coded % 12) == 0 ) {
			lower = 8;
			upper = coded / 12;
		} else if ( (coded % 6) == 0 ) {
			lower = 16;
			upper = coded / 6;
		} else {
			return "?/?";
		}
		return upper+"/"+lower;
	}
}
