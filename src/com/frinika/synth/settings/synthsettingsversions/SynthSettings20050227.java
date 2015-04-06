/*
 * Created on Jan 14, 2005
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
package com.frinika.synth.settings.synthsettingsversions;

import java.io.Serializable;

import com.frinika.synth.settings.SynthSettings;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SynthSettings20050227 implements SynthSettings,Serializable
{
    private static final long serialVersionUID = 1L;
    private String[] synthClassNames = new String[16];
    private Serializable[] synthSettings = new Serializable[16];
    /**
     * @return Returns the synthClassNames.
     */
    public String[] getSynthClassNames() {
        return synthClassNames;
    }
    /**
     * @param synthClassNames The synthClassNames to set.
     */
    public void setSynthClassNames(String[] synthClassNames) {
        this.synthClassNames = synthClassNames;
    }
    /**
     * @return Returns the synthSettings.
     */
    public Serializable[] getSynthSettings() {
        return synthSettings;
    }
    /**
     * @param synthSettings The synthSettings to set.
     */
    public void setSynthSettings(Serializable[] synthSettings) {
        this.synthSettings = synthSettings;
    }
    
	public boolean hasProgramChangeEvent() {
		return false;
	}
}
