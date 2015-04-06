/*
 * Created on 23-Feb-2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
 *
 * http://www.frinika.com
 *
 * This file is part of Frinika.
 *
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.util.tweaks;

import java.util.*;

abstract public class Tweakable extends Observable{
	Number n;

	Comparable min;

	Comparable max;

	String label;

	Number stepSize;

	Tweakable(String label, Number n, Comparable min, Comparable max,
			Number stepSize) {
		this.label = label;
		this.n = n;
		this.stepSize = stepSize;
		this.min = min;
		this.max = max;
	}

	Tweakable(Collection<Tweakable> c, String label, Number n, Comparable min,
			Comparable max, Number step) {
		this(label, n, min, max, step);
		c.add(this);
	}

	public String getLabel() {
		return label;
	}

	public Number getNumber() {
		return n;
	}

	public int intValue() {
		return n.intValue();
	}

	public double doubleValue() {
		return n.doubleValue();
	}

	public Comparable getMinimum() {
		return min;
	}

	public Comparable getMaximum() {
		return max;
	}

	public Number getStepSize() {
		return stepSize;
	}

	public abstract void set(String s);

	public void set(Number n) {
		this.n = n;
		setChanged();
		notifyObservers();
	}

	public String toString() {
		return n.toString();
	}
}
