// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.mixerui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import uk.org.toot.control.*;
//import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.SwingUtilities;
import java.util.Observer;
import java.util.Observable;
//import java.util.Set;
import uk.org.toot.audio.mixer.MixerControls;
import uk.org.toot.control.ControlSelector;
import uk.org.toot.swingui.controlui.PanelFactory;
import uk.org.toot.swingui.audioui.AudioCompoundControlPanel;
import uk.org.toot.swingui.audioui.AudioCompoundStripPanel;

import static uk.org.toot.audio.mixer.MixerControlsIds.*;

public class MixerSectionPanel extends AudioCompoundStripPanel
{
    protected Observer controlObserver;
    protected int sectionId;
    protected MouseListener mouseHandler;

    public MixerSectionPanel(MixerControls mixerControls, int id, PanelFactory panelFactory) {
        super(mixerControls, panelFactory);
        sectionId = id;
        controlObserver = new Observer() {
            public void update(Observable obs, Object arg) {
                if ( arg == null || arg instanceof MixerControls.Mutation ) {
					SwingUtilities.invokeLater(new Updater(arg));
                }
            }
        };
        mouseHandler = new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
                AudioCompoundControlPanel strip =
                    (AudioCompoundControlPanel)e.getComponent();
                setCurrentStripName(strip.getName());
            }
    	};
    }

    protected void processMutation(MixerControls.Mutation m) {
        CompoundControl cc = m.getControl();
        if ( cc.getId() != sectionId ) return;
        int op = m.getOperation();
        if ( op == MixerControls.Mutation.ADD ) {
        	setupStrip(cc);
//            repaint(); // ???
        } else if ( op == MixerControls.Mutation.REMOVE ) {
            reset(); // !!! brute force
        }
    }

    protected void dispose() {
        mouseHandler = null;
        super.dispose();
    }

    protected void setup() {
//        System.out.print('R');
        removeAll(); // !!! brute force change
//        System.out.print('S');
//        int cnt = 0;
        for ( Control control : controls.getControls() ) {
            if ( control instanceof CompoundControl &&
                control.getId() != sectionId ) continue;
            setupStrip((CompoundControl)control);
//            cnt++;
//	        Thread.yield(); // ???
        }
//        System.out.println("MSP setup id "+sectionId+" ("+cnt+" controls)");
    }

    protected void setupStrip(CompoundControl stripControls) {
        AudioCompoundControlPanel faderPanel =
            new FaderPanel(stripControls, controlSelector, panelFactory);
//            new AudioCompoundControlPanel(stripControls, BoxLayout.Y_AXIS, controlSelector, panelFactory, false, true);
//        faderPanel.addMouseListener(mouseHandler); // !!! !!! !!!! cleanup !!!
        if ( sectionId != CHANNEL_STRIP ) {
        	JLabel label = new JLabel(stripControls.getName());
			label.setAlignmentX(0.5f);
            faderPanel.add(label);
        }
        add(faderPanel);
    }

    public void addNotify() {
        super.addNotify();
        controls.addObserver(controlObserver);
    }

    public void removeNotify() {
        controls.deleteObserver(controlObserver);
        if ( doDispose ) controlObserver = null;
        super.removeNotify();
    }

    // provided for simple event handling by overriding
    protected void setCurrentStripName(String name) {
    }

    protected class FaderPanel extends AudioCompoundControlPanel
    {
        public FaderPanel(CompoundControl stripControls, ControlSelector controlSelector, PanelFactory panelFactory) {
            super(stripControls, BoxLayout.Y_AXIS, controlSelector, panelFactory, false, true);
        }

        public void addNotify() {
	        super.addNotify();
            addMouseListener(mouseHandler);
        }

        public void removeNotify() {
            removeMouseListener(mouseHandler);
	        super.removeNotify();
        }

    }

    protected class Updater implements Runnable
    {
        private Object obj;

        public Updater(Object o) {
            obj = o;
        }

		public void run() {
            if ( obj == null ) reset();
            else if ( obj instanceof MixerControls.Mutation ) {
                processMutation((MixerControls.Mutation)obj);
                revalidate();
                repaint();
            }
        }
	}
}
