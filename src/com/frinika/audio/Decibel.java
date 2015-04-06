/*
 * Created on Oct 10, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.audio;

/**
 * @author Peter Johan Salomonsen
 *
 */
public final class Decibel {
	
	static final float[] amplitudeRatio = new float[11000];
	static
	{
		for(float x=-100;x<10;x+=0.01)
			amplitudeRatio[(int)(x*100)+10000] = (float)Math.pow(10,x/20.0); 
	}
	
/*	public static final float getPowerRatio(float dB)
	{
		return((float)Math.pow(10,dB/10.0));
	}*/
	
	public static final float getAmplitudeRatio(float dB)
	{
		return(amplitudeRatio[(int)(dB*100)+10000]);
	}
	
	public static void main(String[] args)
	{
        System.out.println(    getAmplitudeRatio(-96));

	}
}
