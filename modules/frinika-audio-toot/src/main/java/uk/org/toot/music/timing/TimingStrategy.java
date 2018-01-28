// Copyright (C) 2008 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.timing;

import java.util.BitSet;

public interface TimingStrategy 
{
	/**
	 * Return a BitSet containing a bit for each tick of a bar.
	 * @param nTicks the number of ticks in a bar
	 * @return BitSet of ticks, true if if a note starts on that tick
	 */
	BitSet createTiming(int nTicks);
}
