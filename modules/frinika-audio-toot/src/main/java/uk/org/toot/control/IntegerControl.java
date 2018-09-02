// Copyright (C) 2009 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.control;

/**
 * An IntegerControl is actually a FloatControl which has an int-based API.
 * Because of this getUserValue() returns the user value, not getValue().
 * @author st
 *
 */
public class IntegerControl extends FloatControl
{
	public IntegerControl(int id, String name, IntegerLaw law, float precision, int initialValue) {
		super(id, name, law, precision, initialValue);
	}

	/**
	 * @return int - the user value
	 */
	public int getUserValue() {
		return Math.round(getValue());
	}
	
	/**
	 * Overridden to present an int display
	 */
	@Override
	public String getValueString() {
		return String.valueOf(getUserValue());
	}
}
