/*
 * Created on Nov 29, 2004
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
package com.frinika.synth;


/**
 * @author Peter Johan Salomonsen
 *
 */
public class PitchCents {
	
    static final float[] pitchCents = new float[2400];
	static
	{
		for(int n=0;n<pitchCents.length;n++)
		{				
			pitchCents[n] = (float)Math.pow(2.0,(((n-(pitchCents.length/2))/1200.0)) ); 
		}		
	}
	
	public static final float getPitchCent(int pitchCent)
	{
		return(pitchCents[pitchCent + (pitchCents.length/2)]);
	}
	
    public static final float getRealPitchCent(float pitchCent)
    {
        return (float) Math.pow(2.0,((pitchCent/1200.0)) );
    }
    
	public static void main(String[] args)
	{
		for(int n=-1200;n<1200;n++)
			System.out.println(getRealPitchCent(n));
	}
}
