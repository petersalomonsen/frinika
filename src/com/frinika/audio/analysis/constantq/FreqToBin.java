/*
 * Created on Jan 23, 2007
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

package com.frinika.audio.analysis.constantq;

/*
 *    Helper to convert frequency to bin number for constant Q stuff
 * 
 * 
 */
public class FreqToBin {
	
	private double minF;
	private double binsPerOctave;
	
	
	public FreqToBin(double minF,double binsPerOctave){
		this.minF=minF;
		this.binsPerOctave=binsPerOctave;
		
	}
	
	public double getBin(double f) {
		
		// f = minF *2^(bin/bPo)
		//   bin/bPo * ln(2) = ln(f/minF);
		
		return binsPerOctave*Math.log(f/minF)/Math.log(2);
	}

	public static void main(String args[]) {
		double minF=55.0;
		double binsPerOctave=12;
		FreqToBin f2b=new FreqToBin(minF, binsPerOctave);
		
		for (double f=40;f < 400;f +=5) {
		System.out.println(f+ " " +f2b.getBin(f));
		}
	}
	
}
