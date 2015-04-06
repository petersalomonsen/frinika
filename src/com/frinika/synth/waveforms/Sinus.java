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
package com.frinika.synth.waveforms;


/**
 * @author Peter Johan Salomonsen
 *
 */
public class Sinus {
    static final float[] sinus = new float[(int)(2 *Math.PI * 1000)];
    static
    {           
        for(float n=0;n<sinus.length;n++)
        {               
            sinus[(int)n] = (float)Math.sin((n / (float)sinus.length) * Math.PI * 2.0); 
        }       
    }
    
	public static final float getSin(float degree)
	{
		return(sinus[(int)((degree*1000) % sinus.length)]);
	}
	
	public static void main(String[] args)
	{
        long start = System.currentTimeMillis();

        for(float n=0;n<12;n+=0.0001)
			getSin(n);
        System.out.println((System.currentTimeMillis()-start));
	}
}
