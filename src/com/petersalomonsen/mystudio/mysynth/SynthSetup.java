/*
 * Created on Jan 14, 2005
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (mystudio@petersalomonsen.com)
 * 
 * http://www.petersalomonsen.com/mystudio
 * 
 * This file is part of MyStudio.
 * 
 * MyStudio is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * MyStudio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with MyStudio; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.petersalomonsen.mystudio.mysynth;

import java.io.Serializable;

import com.frinika.synth.settings.SynthSettings;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SynthSetup implements SynthSettings,Serializable
{
    private static final long serialVersionUID = 1L;
    String[] synthClasses = new String[16];
    Serializable[] synthSettings = new Serializable[16];
    
    public String[] getSynthClassNames() {
        return synthClasses;
    }
    public void setSynthClassNames(String[] synthClassNames) {
        this.synthClasses = synthClassNames;
    }
    public Serializable[] getSynthSettings() {
        return synthSettings;
    }
    public void setSynthSettings(Serializable[] synthSettings) {
        this.synthSettings = synthSettings;
    }
    
	public boolean hasProgramChangeEvent() {
		// TODO Auto-generated method stub
		return false;
	}
}
