/*
 * Created on Jan 16, 2005
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class Overdrive {
    static final float divisor[];
    
    static
    {
        divisor = new float[128];
        for(int n = 0;n < divisor.length;n++)
        {
            divisor[n] = (float)(Math.log(n*2)/Math.log(2));
        }
    }

    static final float atan[];
    static
    {
        atan = new float[65536];
        {
            for(float n=-10f;n<10f;n+=20f/65536f)
                atan[(int)((n*3276.8f)+32768)] = (float)Math.atan(n);
        }
    }
    
    static final float process(float signal, int amount)
    {
        signal*=amount;
        if(signal>=10f)
            signal = 10f - (20f/65536f);
        else if(signal<-10f)
            signal = -10f;
     
        return (float)(atan[(int)((signal*3276.7f)+32768)] / divisor[amount]);      
    }
 
    public static void main(String[] args)
    {
        long start = System.currentTimeMillis();
        for(float n=-10f;n<10f;n+=20f/65536f)
            process(n,20);
        System.out.println((System.currentTimeMillis()-start));
    }
}
