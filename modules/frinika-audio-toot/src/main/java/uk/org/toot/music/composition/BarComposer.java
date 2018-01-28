// Copyright (C) 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.music.composition;

public interface BarComposer 
{
	/**
	 * Create a 4/4 bar of notes in the specified Keys, changed
	 * at the specified times.
	 * @param BarContext the context for compsing this bar
	 */
	public abstract int[] composeBar(BarContext barContext);
}
