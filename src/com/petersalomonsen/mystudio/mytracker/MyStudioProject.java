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
package com.petersalomonsen.mystudio.mytracker;

import java.io.Serializable;

import com.frinika.project.settings.ProjectSettings;
import com.frinika.synth.settings.SynthSettings;
import com.petersalomonsen.mystudio.mysynth.SynthSetup;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class MyStudioProject implements ProjectSettings,Serializable {
    public static final long serialVersionUID = -1339532546408332747l;
    
    private byte[] sequence;
    private SynthSetup synthSetup;
    
    public byte[] getSequence() {
        // TODO Auto-generated method stub
        return sequence;
    }
    public void setSequence(byte[] sequence) {
        this.sequence = sequence;
    }
    public SynthSettings getSynthSettings() {
        return synthSetup;
    }
    public void setSynthSettings(SynthSettings synthSettings) {
        this.synthSetup = (SynthSetup)synthSettings;
    }
}
