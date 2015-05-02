// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.mixerui;

import javax.swing.JComponent;
import javax.swing.JPanel;

import uk.org.toot.control.*;
import uk.org.toot.audio.fader.*;
import uk.org.toot.swingui.audioui.*;
import uk.org.toot.swingui.audioui.faderui.*;
//import uk.org.toot.swingui.audioui.meterui.*;
import uk.org.toot.swingui.controlui.PanelFactory;

public class MixerPanelFactory extends AudioPanelFactory
{
    public JComponent createComponent(Control control, int axis, boolean hasHeader) {
        if ( control instanceof FaderControl ) {
            JPanel faderPanel = new FaderPanel((FaderControl)control, isFaderRotary(control));
            faderPanel.setAlignmentY(0.25f); // ??? !!!
            return faderPanel;
        }
        return super.createComponent(control, axis, hasHeader);
    }

    @Override
    protected JComponent createCompoundComponent(CompoundControl c, int axis,
        	ControlSelector s, PanelFactory f, boolean hasBorder, boolean hasHeader) {
//        boolean hb = hasBorder && !c.isAlwaysHorizontal(); // ControlRow hack !!! !!!
    	JComponent comp = super.createCompoundComponent(c, axis, s, f, hasBorder, hasHeader);
		NativeSupport ns = c.getNativeSupport();
    	if ( comp instanceof JPanel && ns != null && ns.canAddUI() ) {
    		ns.addUI((JPanel)comp);
    	}
        return comp;
	}

    public boolean isFaderRotary(Control control) {
        return true;
    }
}
