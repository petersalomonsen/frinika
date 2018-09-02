// Copyright (C) 2010 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.synth;

public class ControlChange
{
	private int controller;
	private int value;
	
	public ControlChange(int controller, int value) {
		this.controller = controller;
		this.value = value;
	}
	
	public int getController() { return controller; }
	
	public int getValue() { return value; }
}