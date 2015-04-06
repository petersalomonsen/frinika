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

public class TweakableDouble extends Tweakable {

	/**
	 * 
	 * @param min
	 * @param max
	 * @param val
	 * @param step
	 * @param label
	 */
	public TweakableDouble(double min, double max, double val, double step,
			String label) {
		super(label, new Double(val), new Double(min), new Double(max),
				new Double(step));
	}

	/**
	 * 
	 * @param c
	 * @param min
	 * @param max
	 * @param val
	 * @param step
	 * @param label
	 */
	public TweakableDouble(Collection<Tweakable> c, double min, double max, double val,
			double step, String label) {
		super(c, label, new Double(val), new Double(min), new Double(max),
				new Double(step));
	}

	/**
	 * 
	 */
	public void set(String s) {
		try {
			n = new Double(s);
			setChanged();
			notifyObservers();
		} catch (Exception e) {
		} // TODO

	}

}
