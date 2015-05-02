// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

import java.awt.Color;

/**
 * An implementation of this interface is used by LawControl to select
 * a default color for the knob insert.
 * @author st
 */
public interface InsertColorer
{
	Color getColor(LawControl control);
}
