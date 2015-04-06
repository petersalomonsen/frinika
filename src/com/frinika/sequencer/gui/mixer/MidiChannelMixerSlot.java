/*
 * Created on Mar 19, 2006
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
package com.frinika.sequencer.gui.mixer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.frinika.synth.Synth;

/**
 * A generic mixer slot for midichannels both for softsynths and external midi devices
 * @author Peter Johan Salomonsen
 */
public class MidiChannelMixerSlot extends JPanel {
    private static final long serialVersionUID = 1L;

    JSlider volSlider;
    JSlider panSlider;
    JTextField instrumentName = new JTextField("");
            
    public MidiChannelMixerSlot(final MidiDevice device, final MidiChannel midiChannel)
    {        
        setBorder(new LineBorder(Color.LIGHT_GRAY,1));
        setLayout(new GridBagLayout());
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = GridBagConstraints.REMAINDER;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        
        add(instrumentName,gc);
        
        if(midiChannel instanceof Synth)
        {
            instrumentName.setText(((Synth)midiChannel).getInstrumentName());
            
            JButton showSynthGUIButton = new JButton("Edit");
            showSynthGUIButton.addMouseListener(new MouseAdapter() {
                /* (non-Javadoc)
                 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
                 */
                public void mouseClicked(MouseEvent e) {
                    ((Synth)midiChannel).showGUI();
                }
            } );
            add(showSynthGUIButton,gc);
        }
        
        JCheckBox muteCB = new JCheckBox("Mute");
        muteCB.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED)
                    midiChannel.setMute(true);
                else
                    midiChannel.setMute(false);
            }});
        
        gc.fill = GridBagConstraints.NONE;
        add(muteCB,gc);
        
        volSlider = new JSlider(JSlider.VERTICAL,0,127,100);
        volSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                midiChannel.controlChange(7,volSlider.getValue());
            }});

        gc.fill = GridBagConstraints.NONE;
        add(new JLabel("Volume"),gc);
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.VERTICAL;

        add(volSlider,gc);
        
        panSlider = new JSlider(JSlider.HORIZONTAL,0,127,64);
        panSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                midiChannel.controlChange(10,panSlider.getValue());
            }});

        gc.weighty = 0.0;
        gc.fill = GridBagConstraints.NONE;
        panSlider.setPreferredSize(new Dimension(panSlider.getPreferredSize().width/2,
                panSlider.getPreferredSize().height) );
        add(new JLabel("Pan"),gc);
        add(panSlider,gc);
    }
        
    public void setVolume(int volume)
    {
        volSlider.setValue(volume);
    }
    
    public void setPan(int pan)
    {
        panSlider.setValue(pan);
    }
}
