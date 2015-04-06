/*
 * Created on Apr 15, 2007
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

package com.frinika.audio.analysis;

/**
 * A fast but low precison (error ~ 1x10^-6) floating sinTable. feel free to do
 * anything with this class. Alessandro Borges 2004
 */
import java.text.DecimalFormat;

public class FloatSinTable {
	static final double[] table = build(720);

	static double step;

	static double invStep;

	static int size = 0;
	
	/**
	 * build sin table with size elements
	 * 
	 * @param size
	 */
	final private static double[] build(int pSize) {
		size = pSize;
		double table[] = new double[size+1];
		step = (float) (2d * Math.PI / size);
		invStep = 1.0f / step;
		for (int i = 0; i < size+1; i++) {
			table[i] =  Math.sin(step * ((double) i));
		}
		return table;
	}

	/**
	 * calculates fast sin, but with low precision.
	 * 
	 * @param a
	 *            angle in radians
	 * @return sin of angle a
	 */
	final public static double sinFast(double a) {
		/*
		 * we need speed !
		 * 
		 * if (a == Double.NaN) return Double.NaN; if (a == 0.0d) return 0.0d;
		 */

		int index = ((int) (a / step));
		
		return table[index];
	}

	/**
	 * interpolated values of sin. Not so fast, but much more precise than
	 * 
	 * @param ang
	 *            angle in radians
	 * @return sin of angle a
	 */
	final public static double sin(float ang) {
		int indexA = (int) (ang / step);
		int indexB = (indexA + 1);
		if (indexB >= size)
			return table[indexA];

		double a = table[indexA];
		/*
		 * float b = table[indexB]; double real = Math.sin(ang); float w =
		 * (b-a); float x = ang - (indexA * step);//* invStep; w = w * x / step;
		 * a = a + w; return a ;
		 */
		return a + (table[indexB] - a) * (ang - (indexA * step)) * invStep;

	}

	/**
	 * Testing...
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
	//	FloatSinTable.build(4 * 360);
		DecimalFormat df = new DecimalFormat("0.########");
		int max = 10000000;

		float pi = (float) Math.PI;

		for (int i = 0; i < 20; i++) {
			float angle = (float) (Math.random() * Math.PI * 2f);
			double sinT = FloatSinTable.sin(angle);
			float sin = (float) Math.sin(angle);
			double delta = sin - sinT;
			System.out.println("sin " + df.format(angle) + " :\t" + sinT
					+ " : " + sin + " DELTA " + (delta));
		}

		long t1 = System.currentTimeMillis();
		for (int i = 0; i < max; i++) {
			double x = FloatSinTable.sin(pi);
		}
		long t2 = System.currentTimeMillis();
		System.out.println("tempo SinTable: " + (t2 - t1));

		long t3 = System.currentTimeMillis();
		for (int i = 0; i < max; i++) {
			float x = (float) Math.sin(pi);
		}
		long t4 = System.currentTimeMillis();
		System.out.println("tempo Math.sin: " + (t4 - t3));

		/** *** ********* */
		long t5 = System.currentTimeMillis();
		for (int i = 0; i < max; i++) {
			double x = FloatSinTable.sinFast(pi);
		}
		long t6 = System.currentTimeMillis();

		System.out.println("tempo Sintable fast: " + (t6 - t5));
	}
}
