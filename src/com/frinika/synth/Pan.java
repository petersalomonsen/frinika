/*
 * Created on Mar 17, 2005
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

import com.frinika.audio.Decibel;

/**
 * 
 * @author Peter Johan Salomonsen
 *
 */
public class Pan {
    float leftLevel = 0;
    float rightLevel = 0;
    
    public Pan(final float position)
    {
        if(position == 0f)
            leftLevel = 1f;
        else if(position == 1f)
            rightLevel = 1f;
        else
        {
            leftLevel = Decibel.getAmplitudeRatio(-(float)(-20f*Math.log10(Math.sqrt(1-position))));
            rightLevel = Decibel.getAmplitudeRatio(-(float)(-20f*Math.log10(Math.sqrt(position))));
        }
    }

    /**
     * @return Returns the leftLevel.
     */
    public final float getLeftLevel() {
        return leftLevel;
    }
    

    /**
     * @return Returns the rightLevel.
     */
    public final float getRightLevel() {
        return rightLevel;
    }
    

}
