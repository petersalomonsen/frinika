/*
 * Created on Jan 26, 2005
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
package com.frinika.tracker;

import javax.swing.JComboBox;

import com.frinika.synth.GlobalInstrumentNameListener;
import com.frinika.synth.SynthRack;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class ChannelComboBox extends JComboBox implements GlobalInstrumentNameListener {
    private SynthRack synth;

    public ChannelComboBox(SynthRack synth)
    {
        this.synth = synth;
        for(int n=0;n<16;n++)
        {
            String itemName = n+"";
            if(synth.getSynth(n)!=null)
            {
                itemName+=" "+synth.getSynth(n).getInstrumentName();
            }
            addItem(n+"");
        }
        setSelectedIndex(0);
        synth.addGlobalInstrumentNameListener(this);
    }

    /* (non-Javadoc)
     * @see com.petersalomonsen.mystudio.mysynth.GlobalInstrumentNameListener#instrumentNameChange(int, java.lang.String)
     */
    public void instrumentNameChange(int synthIndex, String instrumentName) {
        boolean isSelected = false;
        if(getSelectedIndex()==synthIndex)
            isSelected = true;

        removeItemAt(synthIndex);
        insertItemAt(synthIndex+" "+instrumentName,synthIndex);
        
        if(isSelected)
            setSelectedIndex(synthIndex);
    }
}
