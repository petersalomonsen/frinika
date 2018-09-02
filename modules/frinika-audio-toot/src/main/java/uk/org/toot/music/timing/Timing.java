// Copyright (C) 2008 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.timing;

/**
 * Constants that represent specific note lengths in the 24ppqn system used.
 * @author st
 *
 */
public interface Timing 
{
	final static int PPQN = 24;
	
	final static int WHOLE_NOTE = 96;
	final static int HALF_NOTE = 48;
	final static int QUARTER_NOTE = 24;
	final static int EIGHTH_NOTE = 12;
	final static int SIXTEENTH_NOTE = 6;
	final static int THIRTYSECOND_NOTE = 3;
	
	final static int DOTTED_HALF_NOTE = 72;
	final static int DOTTED_QUARTER_NOTE = 36;
	final static int DOTTED_EIGHTH_NOTE = 18;
	final static int DOTTED_SIXTEENTH_NOTE = 9;
	
	final static int HALF_NOTE_TRIPLET = 32;
	final static int QUARTER_NOTE_TRIPLET = 16;
	final static int EIGHTH_NOTE_TRIPLET = 8;
	final static int SIXTEENTH_NOTE_TRIPLET = 4;
	final static int THIRTYSECOND_NOTE_TRIPLET = 2;
	final static int SIXTYFOURTH_NOTE_TRIPLET = 1;
}
