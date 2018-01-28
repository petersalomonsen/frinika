// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;

import uk.org.toot.audio.core.AudioControls;
import uk.org.toot.audio.core.AudioControlsChain;
import uk.org.toot.audio.dynamics.GainReductionIndicator;
import uk.org.toot.control.*;
import uk.org.toot.swingui.audioui.meterui.GainReductionIndicatorPanel;
import uk.org.toot.swingui.controlui.*;
import uk.org.toot.control.ControlSelector;

public class AudioPanelFactory extends ControlPanelFactory
{
    protected boolean canBypass(CompoundControl control) {
        if ( control instanceof AudioControls ) {
            return ((AudioControls)control).canBypass();
        }
        return false;
    }

    protected JComponent createHeader(final CompoundControl control, final int axis) {
        boolean isShort = false;
        String title = control.getName();
        if ( isMinimised(title) || axis == BoxLayout.Y_AXIS) {
            title = shorten(title);
            isShort = true;
        }
        // make a button 'border' if control is bypassable
        if ( canBypass(control) ) {
            final JButton button = new BypassButton(title, control,
                ((uk.org.toot.audio.core.AudioControls)control).getBypassControl());
	        // !!! !!! we need vertical button if axis == X_AXIS and isMinimised(control)
            if ( isShort ) {
                button.setToolTipText(control.getName());
            }
//     		Border border = BorderFactory.createCompoundBorder(button.getBorder(), BUTTON_BORDER);
	        button.setBorder(BorderFactory.createRaisedBevelBorder());
            return button;
        }
        if ( control instanceof AudioControlsChain ) {
            JPanel hdr = new JPanel();
            hdr.setLayout(new BoxLayout(hdr, axis));
            JLabel titleLabel = new JLabel(title, JLabel.CENTER);
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            hdr.add(titleLabel);
            if ( axis == BoxLayout.X_AXIS ) {
                hdr.add(Box.createHorizontalStrut(16));
            }
            hdr.add(new SourceLabel((AudioControlsChain)control, axis));
            return hdr;
        }
        return super.createHeader(control, axis);
    }

    // add panels to parent, typically for 'hidden' indicators
    // such as compressor meters.
    // These panels are added below the header but above non-hidden controls
    // into a Y_AXIS BoxLayout.
    protected void createTop(JComponent parent, CompoundControl control, int axis) {
        for ( Control c : control.getControls() ) {
            if ( !c.isHidden() || !c.isIndicator() ) continue;
            parent.add(createComponent(c, axis, false));
        }
    }

    protected JComponent createCompoundComponent(CompoundControl c, int axis,
        	ControlSelector s, PanelFactory f, boolean hasBorder, boolean hasHeader) {
//        boolean hb = hasBorder && !c.isAlwaysHorizontal(); // ControlRow hack !!! !!!
        return new AudioCompoundControlPanel(c, axis, s, f, hasBorder, hasHeader);
	}

    public JComponent createComponent(Control control, int axis, boolean hasHeader) {
    	if ( control instanceof GainReductionIndicator ) {
            return new GainReductionIndicatorPanel((FloatControl)control);
    	}
    	return super.createComponent(control, axis, hasHeader);
    }
    
    static protected class SourceLabel extends JLabel implements Observer
    {
        private AudioControlsChain chain;
        private int axis;

        public SourceLabel(AudioControlsChain chain, int axis) {
            super(" ", JLabel.CENTER);
            this.chain = chain;
            this.axis = axis;
            setAlignmentX(Component.CENTER_ALIGNMENT);
            if ( axis == BoxLayout.Y_AXIS ) {
	   	   		setFont(getFont().deriveFont(10f));
            }
            labelSource(chain.getSourceLabel(), chain.getSourceLocation());
        }

	    public void addNotify() {
    	    super.addNotify();
       		chain.addObserver(this);
	    }

    	public void removeNotify() {
        	chain.deleteObserver(this);
        	super.removeNotify();
    	}

        public void update(Observable obs, Object obj) {
           	if ( obj == null ) {
        	    labelSource(chain.getSourceLabel(), chain.getSourceLocation());
        	}
        }

	    protected void labelSource(String label, String location) {
    	    if ( label == null ) label = " ";
        	if ( axis == BoxLayout.Y_AXIS ) {
            	boolean tooLong = label.length() > 8;
    	    	setText(tooLong ? label.substring(0, 8) : label);
            	setToolTipText(tooLong ? "("+label+") "+location : location);
        	} else {
            	setText(label);
        	}
    	}
    }

    protected class BypassButton extends JButton implements Observer, ActionListener
    {
        private BooleanControl bypassControl;
        private AudioControls controls;
        private String name;

        public BypassButton(String title, CompoundControl cc, BooleanControl control) {
            super(title);
            bypassControl = control;
            controls = (AudioControls)cc;
            name = cc.getName();
            update(null, null);
        }

        public Dimension getMaximumSize() {
            Dimension d = super.getMaximumSize();
            if ( !AudioPanelFactory.this.isMinimised(name) ) {
            	d.width = 512; //parent.getWidth();
            }
   	        return d;
       	}

	    public void addNotify() {
    	    super.addNotify();
       		bypassControl.addObserver(this);
            addActionListener(this);
	    }

    	public void removeNotify() {
        	removeActionListener(this);
		    bypassControl.deleteObserver(this);
        	super.removeNotify();
    	}

	    public void actionPerformed(ActionEvent ae) {
            bypassControl.setValue(!bypassControl.getValue());
            ((AudioControls)controls).setBypassed(isSelected()); // !!! !!! !!!
        }

        public void update(Observable obs, Object obj) {
            setForeground(bypassControl.getValue() ? Color.GRAY : Color.BLACK);
            setSelected(bypassControl.getValue());
        }
    }
}
