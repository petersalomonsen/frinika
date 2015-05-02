// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.audioui.mixerui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import uk.org.toot.control.*;
import uk.org.toot.audio.meter.*;
import uk.org.toot.audio.mixer.*;
import java.awt.BorderLayout;
import javax.swing.*;
import java.util.Observer;
import java.util.Observable;
import uk.org.toot.audio.mixer.MixerControls;
import uk.org.toot.swingui.DisposablePanel;
import uk.org.toot.swingui.audioui.meterui.KMeterPanel;
import uk.org.toot.control.ControlSelector;
import uk.org.toot.swingui.controlui.PanelFactory;
import static javax.swing.ScrollPaneConstants.*;

import static uk.org.toot.audio.mixer.MixerControlsIds.*;
import static uk.org.toot.audio.mixer.automation.AutomationControls.AUTOMATION_ID;
import static uk.org.toot.misc.Localisation.*;

public class FullMixerPanel extends DisposablePanel
{
    protected MixerControls mixerControls;
    protected ControlSelector controlSelector;
    protected JToolBar toolBar;
    protected JPanel stripsPanel;

    /**
     * @link aggregationByValue
     * @label channels
     * @supplierCardinality 1 
     */
    protected MixerSectionPanel channelsPanel;

	/**
	 * @link aggregationByValue
	 * @label groups
	 * @supplierCardinality 1
	 */
	protected MixerSectionPanel groupsPanel;

	/**
	 * @link aggregationByValue
	 * @label fx
	 * @supplierCardinality 1
	 */
	protected MixerSectionPanel fxPanel;

	/**
	 * @link aggregationByValue
	 * @label aux
	 * @supplierCardinality 1
	 */
	protected MixerSectionPanel auxPanel;

	/**
	 * @link aggregationByValue
	 * @label main
	 * @supplierCardinality 1
	 */
	protected MixerSectionPanel masterPanel;

    /**
     * @link aggregationByValue
     * @supplierCardinality 0..1
     * @label global controls 
     */
    protected MixerSectionPanel controlsPanel;

	protected KMeterPanel meterPanel;

    protected JLabel statusLabel;
    protected String currentBusName;
    protected PanelFactory panelFactory;

    private Observer statusObserver = null;

    public FullMixerPanel(MixerControls mixerControls) {
        this.mixerControls = mixerControls;
        currentBusName = mixerControls.getMainBusControls().getName();
        statusLabel = new JLabel("");
        statusObserver = new Observer() {
           	public void update(Observable obs, Object arg) {
               	if ( arg != null && arg instanceof Control ) {
               		updateStatusLabel((Control)arg);
				}
           	}
        };
        toolBar = new JToolBar();
        addTools(toolBar);
        panelFactory = new MixerPanelFactory() {
            public boolean isFaderRotary(Control c) {
                return !currentBusName.startsWith(c.getParent().getName());
            }
        };
        channelsPanel = new MixerSectionPanel(mixerControls, CHANNEL_STRIP, panelFactory) {
       	    protected void setCurrentStripName(String name) {
           	    FullMixerPanel.this.setCurrentStripName(name);
            }
   	    };
        groupsPanel = new MixerSectionPanel(mixerControls, GROUP_STRIP, panelFactory) {
       	    protected void setCurrentStripName(String name) {
           	    FullMixerPanel.this.setCurrentStripName(name);
           	}
        };
        fxPanel = new MixerSectionPanel(mixerControls, FX_STRIP, panelFactory) {
       	    protected void setCurrentStripName(String name) {
           	    FullMixerPanel.this.setCurrentStripName(name);
           	}
        };
        auxPanel = new MixerSectionPanel(mixerControls, AUX_STRIP, panelFactory) {
       	    protected void setCurrentStripName(String name) {
           	    FullMixerPanel.this.setCurrentStripName(name);
           	}
        };
        masterPanel = new MixerSectionPanel(mixerControls, MAIN_STRIP, panelFactory) {
       	    protected void setCurrentStripName(String name) {
           	    FullMixerPanel.this.setCurrentStripName(name);
            }
   	    };
        controlsPanel = new MixerSectionPanel(mixerControls, CONTROL_STRIP_ID, panelFactory) {
       	    protected void setCurrentStripName(String name) {
           	    FullMixerPanel.this.setCurrentStripName(name);
            }
   	    };
        meterPanel = new KMeterPanel(mixerControls.getBusControls(currentBusName).getMeterControls(), BoxLayout.Y_AXIS);
        setLayout(new BorderLayout());
        stripsPanel = new JPanel();
        stripsPanel.setLayout(new BoxLayout(stripsPanel, BoxLayout.X_AXIS));
        setup();
        add(stripsPanel, BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);
    }

    protected void dispose() {
        panelFactory = null;
		channelsPanel = null;
        groupsPanel = null;
        fxPanel = null;
        auxPanel = null;
        masterPanel = null;
        controlsPanel = null;
        meterPanel.dispose();
        meterPanel = null;
        stripsPanel.removeAll();
		stripsPanel = null;
        removeAll();
        toolBar.removeAll();
        toolBar = null;
        controlSelector = null;
    }

    public void addNotify() {
        super.addNotify();
        mixerControls.addObserver(statusObserver);
    }

    public void removeNotify() {
        mixerControls.deleteObserver(statusObserver);
        if ( doDispose ) statusObserver = null;
        super.removeNotify();
    }

    public void setControlSelector(ControlSelector selector) {
        channelsPanel.setControlSelector(selector);
        groupsPanel.setControlSelector(selector);
        fxPanel.setControlSelector(selector);
        auxPanel.setControlSelector(selector);
        masterPanel.setControlSelector(selector);
        controlSelector = selector; // !!! !!! for resetting !!! !!!
    }

    protected void updateStatusLabel(final Control c) {
    	SwingUtilities.invokeLater(
    		new Runnable() {
    			public void run() {
    		        // want the path from mixerControls to c
    		        statusLabel.setText(c.getControlPath(mixerControls, ", ")+"  "+c.getValueString());    				
    			}
    		}
   		);
    }

    protected void addTools(JToolBar bar) {
        bar.add(Box.createGlue());
        bar.add(Box.createHorizontalStrut(20));
        bar.add(statusLabel);
        bar.add(Box.createGlue());
        bar.add(new BusCombo(mixerControls)); // !!! under runs !!! !!!
        bar.add(Box.createHorizontalStrut(10));
		addSectionButtons(bar);
    }

    public class BusCombo extends JComboBox
    {
        public BusCombo(MixerControls mixerControls) {
            addItem(mixerControls.getMainBusControls());
            for ( BusControls busControls : mixerControls.getFxBusControls() ) {
                addItem(busControls);
            }
            for ( BusControls busControls : mixerControls.getAuxBusControls() ) {
                addItem(busControls);
            }
            addActionListener(
                new ActionListener() {
                	public void actionPerformed(ActionEvent ae) {
                    	setCurrentBusName(getSelectedItem().toString());
                	}
            	}
            );
        }

        public Dimension getMaximumSize() {
            Dimension size = super.getPreferredSize();
            size.width = 100;
            return size;
        }
    }

    protected void setCurrentBusName(String name) {
        currentBusName = name;
        setControlSelector(controlSelector); // !!! !!!
        MeterControls mc = mixerControls.getBusControls(currentBusName).getMeterControls();
        if ( mc != null ) {
        	meterPanel.setMeterControls(mc);
        }
    }

    protected void addSectionButtons(JToolBar bar) {
        final JToggleButton groupsButton, fxButton, auxButton;
        final String groupsString, fxString, auxString;
        groupsString = getString("Groups");
        fxString = getString("FX");
        auxString = getString("Aux");
        groupsButton = new JToggleButton(groupsString, false);
        fxButton = new JToggleButton(fxString, false);
        auxButton = new JToggleButton(auxString, false);
        ActionListener actionListener = new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
                String cmd = e.getActionCommand();
				if ( cmd.equals(groupsString) ) {
        			groupsPanel.setVisible(groupsButton.isSelected());
    			} else if ( cmd.equals(fxString) ) {
        			fxPanel.setVisible(fxButton.isSelected());
    			} else if ( cmd.equals(auxString) ) {
        			auxPanel.setVisible(auxButton.isSelected());
    	 		}
    		}
        };
        groupsButton.addActionListener(actionListener); // !!! !!! remove?
        fxButton.addActionListener(actionListener); // !!! !!! remove?
        auxButton.addActionListener(actionListener); // !!! !!! remove?
        bar.add(groupsButton);
        bar.add(fxButton);
        bar.add(auxButton);
    }

    protected void setup() {
        groupsPanel.setVisible(false);
        fxPanel.setVisible(false);
        auxPanel.setVisible(false);
        JScrollPane scrollPane = new JScrollPane(channelsPanel,
            VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_ALWAYS);
        int inc = 64; //masterPanel.getComponent(0).getSize().width; !!!
        scrollPane.getHorizontalScrollBar().setUnitIncrement(inc);
       	stripsPanel.add(scrollPane);
        stripsPanel.add(Box.createHorizontalGlue());
       	stripsPanel.add(groupsPanel);
        stripsPanel.add(fxPanel);
        stripsPanel.add(auxPanel);
       	stripsPanel.add(masterPanel);
        stripsPanel.add(meterPanel);
        stripsPanel.add(controlsPanel);
    }

    protected void setCurrentStripName(String currentStrip) {
//        System.out.println("Full "+currentStrip);
    }

    protected boolean useButtonGroup() { return true; }

    public class SingleBusSelector implements ControlSelector
    {
        public boolean select(Control control) {
            // false if not a bus (or automation)
            int id = control.getId();
            if ( id == AUTOMATION_ID ) return true; // !!! automation hack
    		if ( id != MAIN_BUS && id != AUX_BUS && id != FX_BUS )
                return false;
		    boolean isCurrentBus = currentBusName.startsWith(control.getName());
//System.out.println("SingleBusSelector: "+control.getName()+"/"+currentBusName+" "+isCurrentBus);
            return isCurrentBus;
        }
    }

    static public class UserSelector implements ControlSelector
    {
        public boolean select(Control control) {
            // return false if no button for the control
            // return the button enable state
            return false;
        }
    }
}
