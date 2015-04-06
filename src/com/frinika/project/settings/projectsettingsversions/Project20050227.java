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
package com.frinika.project.settings.projectsettingsversions;

import java.io.Serializable;

import com.frinika.project.settings.ProjectSettings;
import com.frinika.synth.settings.SynthSettings;

/**
 * For backwards compatibility (Frinika 0.1.0 format)
 * @author Peter Johan Salomonsen
 *
 */
public class Project20050227 implements ProjectSettings,Serializable {
    private static final long serialVersionUID = 1L;
    
    private byte[] sequence;
    private SynthSettings synthSettings;
    /**
     * @return Returns the sequence.
     */
    public byte[] getSequence() {
        return sequence;
    }
    /**
     * @param sequence The sequence to set.
     */
    public void setSequence(byte[] sequence) {
        this.sequence = sequence;
    }
    /**
     * @return Returns the synthSettings.
     */
    public SynthSettings getSynthSettings() {
        return synthSettings;
    }
    /**
     * @param synthSettings The synthSettings to set.
     */
    public void setSynthSettings(SynthSettings synthSettings) {
        this.synthSettings = synthSettings;
    }
}
