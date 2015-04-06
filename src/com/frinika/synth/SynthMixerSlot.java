/*
 * Created on Dec 3, 2004
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 2007-08-15: Mixer sliders are now obsolete since this is now used to define a soundbank
 *
 * @author Peter Johan Salomonsen
 *
 */
public class SynthMixerSlot implements InstrumentNameListener {
    private static final long serialVersionUID = 1L;

    JComboBox synthCB;
    JSlider volSlider;
    JSlider panSlider;
    JLabel instrumentName = new JLabel(" ");
    
    final class SynthRegisterEntry
    {
            Class synthClass;
            String synthName;
            public SynthRegisterEntry(Class synthClass, String synthName)
            {
                    this.synthClass = synthClass;
                    this.synthName = synthName;
            }

            public String toString()
            {
                    return(synthName);
            }
    }

    /**
     * Frinika legacy softsynth
     */
    class SynthRegister extends HashMap<Class,SynthRegisterEntry>
    {
        private static final long serialVersionUID = 1L;

        public SynthRegister()
        {
            this.put(com.frinika.synth.synths.Organ.class,
                            new SynthRegisterEntry(com.frinika.synth.synths.Organ.class,"Organ"));
            this.put(com.frinika.synth.synths.Analogika.class,
                            new SynthRegisterEntry(com.frinika.synth.synths.Analogika.class,"Analogika"));
            this.put(com.frinika.synth.synths.MySampler.class,
                            new SynthRegisterEntry(com.frinika.synth.synths.MySampler.class,"mySampler"));
            this.put(com.frinika.contrib.boblang.FrinikaBezierSynth.class,
                    new SynthRegisterEntry(com.frinika.contrib.boblang.FrinikaBezierSynth.class,"BezierSynth"));
        }
    };

    final SynthRegister synthRegister = new SynthRegister();

    int synthNo;
    SynthRack ms;

    public SynthMixerSlot(JPanel synthListPanel, GridBagConstraints gc, SynthRack ms, int synthNo)
    {
        this.ms = ms;
        this.synthNo = synthNo;

        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.RELATIVE;    
        gc.anchor = GridBagConstraints.WEST;
        
        synthListPanel.add(new JLabel("Program "+synthNo),gc);
        synthListPanel.setBorder(new LineBorder(Color.LIGHT_GRAY,1));
        synthCB = new JComboBox();
        synthCB.addItem("");
        for(SynthRegisterEntry synthEntry : synthRegister.values())
                synthCB.addItem(synthEntry);

        synthCB.addItemListener(new ItemListener() {

        public void itemStateChanged(ItemEvent e) {
            if(e.getStateChange() == ItemEvent.SELECTED)
                        {
            try {
                                        SynthMixerSlot.this.ms.setSynth(SynthMixerSlot.this.synthNo,(Synth)(((SynthRegisterEntry)e.getItem()).synthClass.getConstructors()[0].newInstance(new Object[]{SynthMixerSlot.this.ms})));
                                } catch (Exception ex) {
                SynthMixerSlot.this.ms.setSynth(SynthMixerSlot.this.synthNo,null);
                                }
                        }
                }

        });


        if(ms.getSynth(synthNo)!=null)
            instrumentName.setText(ms.getSynth(synthNo).getInstrumentName());
        synthListPanel.add(instrumentName,gc);
        synthListPanel.add(synthCB,gc);

        JButton showSynthGUIButton = new JButton("Edit");
        showSynthGUIButton.addMouseListener(new MouseAdapter() {
                            /* (non-Javadoc)
                             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                             */
                            public void mouseClicked(MouseEvent e) {
                                    if( SynthMixerSlot.this.ms.synths[SynthMixerSlot.this.synthNo] != null )   // PJL
                                            SynthMixerSlot.this.ms.synths[SynthMixerSlot.this.synthNo].showGUI();
                            }
                    } );

        gc.gridwidth = GridBagConstraints.REMAINDER;

        synthListPanel.add(showSynthGUIButton,gc);
        
        if(ms.getSynth(synthNo)!=null)
            setSynth(ms.getSynth(synthNo));
    }
	
    public void setSynth(Synth synth)
    {
        ItemListener listener = synthCB.getItemListeners()[0];
        synthCB.removeItemListener(listener);

        if(synth!=null)
        {
            synthCB.setSelectedItem(synthRegister.get(synth.getClass()));
            synth.addInstrumentNameListener(this);
        }
        else
            synthCB.setSelectedIndex(0);

        synthCB.addItemListener(listener);
    }
	
    public void setVolume(int volume)
    {
		volSlider.setValue(volume);
    }
    
    public void setPan(int pan)
    {
        panSlider.setValue(pan);
    }

    /* (non-Javadoc)
     * @see com.petersalomonsen.mystudio.mysynth.InstrumentNameListener#instrumentNameChange(java.lang.String)
     */
    public void instrumentNameChange(Synth synth, String instrumentName) {
        this.instrumentName.setText(instrumentName+" ");
    }
}
