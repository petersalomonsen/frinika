// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)

package com.frinika.tootX.gui;

import com.frinika.tootX.midi.MidiLearnIF;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.org.toot.audio.fader.FaderControl;
import uk.org.toot.control.BooleanControl;
import uk.org.toot.control.CompoundControl;
import uk.org.toot.control.Control;
import uk.org.toot.control.ControlSelector;
import uk.org.toot.control.EnumControl;
import uk.org.toot.control.FloatControl;
import uk.org.toot.swingui.audioui.AudioCompoundControlPanel;
import uk.org.toot.swingui.audioui.AudioPanelFactory;
import uk.org.toot.swingui.audioui.faderui.FaderPanel;
import uk.org.toot.swingui.controlui.BooleanControlPanel;
import uk.org.toot.swingui.controlui.BooleanIndicatorPanel;
import uk.org.toot.swingui.controlui.EnumControlPanel;
import uk.org.toot.swingui.controlui.FloatControlPanel;
import uk.org.toot.swingui.controlui.PanelFactory;

public class FrinikaMixerPanelFactory extends AudioPanelFactory
{
	
	ControlFocus  focus=null;
	
	
	public FrinikaMixerPanelFactory(MidiLearnIF midiLearnIF) {
		focus=new ControlFocus(midiLearnIF);
	}
	
	
    public JComponent createComponent(Control control, int axis, boolean hasHeader) {
   
    //	System.out.println(" Creating Mixer Component " + control + "  " + control.getAnnotation() );
    
    	if ( control instanceof FaderControl ) {
    		
    		
    //		System.out.println(" FrinikaFader");
  
    		JPanel faderPanel = new FaderPanel((FaderControl)control, isFaderRotary(control));
            
            focus.addComponent(faderPanel);
            
            faderPanel.setAlignmentY(0.25f); // ??? !!!
            
            return faderPanel;
        }
    
        if ( control instanceof CompoundControl ) {
    //    	System.out.println(" FrinikaCompound");
            CompoundControl cc = (CompoundControl)control;
            int a = axis;
            if ( cc.isAlwaysVertical() ) {
                a = BoxLayout.Y_AXIS;
            } else if ( cc.isAlwaysHorizontal() ) {
                a = BoxLayout.X_AXIS;
            }
            // brute force service provider lookup
            // expected slow-down but it still seems fast! causes sound glitches
//	        JComponent comp = ControlPanelServices.createControlPanel(cc, a, null, this, axis == BoxLayout.X_AXIS, hasHeader);
//    	    if ( comp != null ) return comp;
            // default compound UI
            return createCompoundComponent(cc, a, null, this, true, hasHeader);
        } else if ( control instanceof FloatControl ) {
            JPanel floatPanel;
            if ( control.isIndicator() ) {
                // !!! !!! !!!
                floatPanel = new uk.org.toot.swingui.audioui.meterui.GainReductionIndicatorPanel((FloatControl)control);
            } else {
      //      	System.out.println(" FrinikaFLoat");
	            floatPanel = new FloatControlPanel((FloatControl)control, axis);
    	        floatPanel.setAlignmentY(0.25f); // ??? !!!
            }
            focus.addComponent(floatPanel);
            return floatPanel;
        } else if ( control instanceof BooleanControl ) {
            if ( control.isIndicator() ) {
                return new BooleanIndicatorPanel((BooleanControl)control);
            } else {
              	return new BooleanControlPanel((BooleanControl)control);
            }
        } else if ( control instanceof EnumControl ) {
            if ( control.isIndicator() ) {
                JLabel label = new JLabel(((EnumControl)control).getValue().toString());
				label.setBorder(BorderFactory.createEmptyBorder(3, 1, 2, 2));
                label.setAlignmentX(0.5f);
                return label;
            } else {
	            return new EnumControlPanel((EnumControl)control);
            }
        } else { // !!! !!!
        }
        return null;
    }

    
    protected JComponent createCompoundComponent(CompoundControl c, int axis,
        	ControlSelector s, PanelFactory f, boolean hasBorder, boolean hasHeader) {
//        boolean hb = hasBorder && !c.isAlwaysHorizontal(); // ControlRow hack !!! !!!
        return new AudioCompoundControlPanel(c, axis, s, f, hasBorder, hasHeader);
	}
    
    
    public boolean isFaderRotary(Control control) {
        return true;
    }
 
   
}
