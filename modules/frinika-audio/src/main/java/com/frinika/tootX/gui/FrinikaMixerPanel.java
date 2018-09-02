// Copyright (C) 2006 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org/LICENSE_1_0.txt)
package com.frinika.tootX.gui;

import com.frinika.tootX.midi.MidiLearnIF;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import uk.org.toot.audio.mixer.MixerControls;
import uk.org.toot.audio.mixer.MixerControlsIds;
import uk.org.toot.audio.mixer.automation.AutomationControls;
import uk.org.toot.control.*;
import uk.org.toot.control.automation.SnapshotAutomation;
import uk.org.toot.swingui.DisposablePanel;
import uk.org.toot.swingui.audioui.AudioCompoundControlPanel;
import uk.org.toot.swingui.audioui.mixerui.FullMixerPanel;
import uk.org.toot.swingui.audioui.mixerui.PopupButton;
import uk.org.toot.swingui.audioui.mixerui.SnapshotAutomationPopupMenu;
import uk.org.toot.swingui.controlui.PanelFactory;

public class FrinikaMixerPanel extends DisposablePanel {

    private String currentStripName;
    /**
     * @link aggregationByValue
     * @supplierCardinality 1
     */
    private JPanel stripPanel;
    protected MixerControls mixerControls;
    protected ControlSelector topControlSelector;
//	private ControlSelector controlSelector;

    /**
     * @link aggregationByValue
     * @supplierCardinality 1 */
    protected FullMixerPanel fullMixerPanel;
    protected PanelFactory topPanelFactory;

    private Observer mutationObserver = null;

    public FrinikaMixerPanel(final MixerControls mixerControls, MidiLearnIF midiLearnIF) {
        this.mixerControls = mixerControls;
        topPanelFactory = new FrinikaMixerPanelFactory(midiLearnIF) {
            @Override
            protected boolean toggleMinimised(String name) {
                boolean ret = super.toggleMinimised(name);
                FrinikaMixerPanel.this.build(currentStripName);
                return ret;
            }

            @Override
            protected boolean addGlue() {
                return true;
            }

            @Override
            protected boolean canEdit() {
                return true;
            }
        };
        setLayout(new BorderLayout());
        fullMixerPanel = new FullMixerPanel(mixerControls) {
            @Override
            protected void setCurrentStripName(String name) {
                if (!name.equals(currentStripName)) {
                    currentStripName = name;
                    FrinikaMixerPanel.this.build(currentStripName);
                }
            }

            @Override
            protected void setCurrentBusName(String name) {
                super.setCurrentBusName(name);
                // not required with selecter
//        		CompactMixerPanel.this.build(currentStripName);
            }

            @Override
            protected void addTools(JToolBar bar) {
                // add top panel show button
                FrinikaMixerPanel.this.addTools(bar);
                super.addTools(bar);
            }
        };
        ControlSelector controlSelector = fullMixerPanel.new SingleBusSelector();
        fullMixerPanel.setControlSelector(controlSelector);
        topControlSelector = new ControlSelector() {
            @Override
            public boolean select(Control c) {
                if (c.getId() == AutomationControls.AUTOMATION_ID) {
                    return false;
                }
                return true;
            }
        };
        add(fullMixerPanel, BorderLayout.CENTER);
        setup();
        mutationObserver = new Observer() {
            @Override
            public void update(Observable obs, Object arg) {
                if (arg instanceof MixerControls.Mutation) {
                    // SwingUtilities.invokeLater() ??? seems to work w/o !!!
                    MixerControls.Mutation m = (MixerControls.Mutation) arg;
                    if (m.getOperation() == MixerControls.Mutation.REMOVE
                            && m.getControl().getName().equals(currentStripName)) {
                        // if the current strip is deleted, use Master strip
                        FrinikaMixerPanel.this.build(mixerControls.getMainBusControls().getName());
                    }
                }
            }
        };
    }

    @Override
    protected void dispose() {
        // anonymous inner classes maintain a reference to this
        // so their references must be nulled to enable their garbage collection
        // which in turn enables this to be garbage collected
        topPanelFactory = null;
        fullMixerPanel = null;
        if (stripPanel != null) {
            stripPanel.removeAll();
            stripPanel = null;
        }

        topControlSelector = null;
        removeAll();
    }

    JToggleButton channelButton;

    protected void addTools(JToolBar bar) {

        final String hideString, showString;
        hideString = "Hide.Upper.Panel";
        showString = "Show.Upper.Panel";
        channelButton = new JToggleButton();

        boolean showUpper = false;
        channelButton.setIcon(createUpperPanelIcon(!showUpper));
        channelButton.setSelectedIcon(createUpperPanelIcon(showUpper));
        channelButton.setSelected(showUpper);
        channelButton.setToolTipText(hideString);

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stripPanel.setVisible(channelButton.isSelected());
                channelButton.setToolTipText(
                        channelButton.isSelected() ? hideString : showString);
            }
        };
        channelButton.addActionListener(actionListener); // !!! !!! remove?
        bar.add(channelButton);
        SnapshotAutomation snapshotAutomation
                = mixerControls.getSnapshotAutomation();
        if (snapshotAutomation != null) {
            JPopupMenu snapshotPopup = new SnapshotAutomationPopupMenu(snapshotAutomation);
            JButton snapshotButton = new PopupButton(snapshotPopup);
            snapshotButton.setIcon(createSnapshotIcon(false));
            snapshotButton.setToolTipText("Snapshot.Automation");
            bar.add(snapshotButton);
        }
    }

    protected Icon createUpperPanelIcon(boolean selected) {
        int w = 24;
        int h = 20;
        Image image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // draw icon
        g.setColor(getBackground());
        g.fillRect(0, 0, w - 1, h - 1);
        g.setColor(Color.BLACK);
        g.drawLine(0, 0, w - 1, 0);
        int y = 0;
        if (!selected) {
            y = 6;
            g.drawLine(0, y, w - 1, y); // upper panel
        }
        g.drawLine(0, h - 1, w - 1, h - 1);
        for (int x = 3; x < w; x += 5) {
            g.drawLine(x, h - 1, x, y); // strips
        }
        g.dispose();
        return new ImageIcon(image);
    }

    protected Icon createSnapshotIcon(boolean selected) {
        int w = 20;
        int h = 16;
        Image image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // draw icon
        g.setColor(getBackground());
        g.fillRect(0, 0, w - 1, h - 1);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, w - 1, h - 1);
        g.drawOval(w - h + 3, 4, h - 8, h - 8);
        g.setColor(Color.DARK_GRAY);
        g.fillOval(w - h + 3, 4, h - 8, h - 8);
        g.setColor(Color.red);
        g.drawRect(3, 3, 2, 2);
        g.dispose();
        return new ImageIcon(image);
    }

    protected void setup() {
        stripPanel = null; // needs setting here, not with decl
        CompoundControl stripControls = mixerControls.getStripControls(MixerControlsIds.MAIN_STRIP, 0);
        if (stripControls != null) {
            build(stripControls.getName());
        }
    }

    private int lastHeight;

    protected void build(String name) {
        CompoundControl stripControls = mixerControls.getStripControls(name);
        if (stripControls != null) {
            lastHeight = 0;
            if (stripPanel != null) {
                lastHeight = stripPanel.getSize().height;
                remove(stripPanel);
            }
            stripPanel
                    = new AudioCompoundControlPanel(stripControls, BoxLayout.X_AXIS,
                            topControlSelector, topPanelFactory, true, true) {
                @Override
                public Dimension getPreferredSize() {
                    Dimension d = super.getPreferredSize();
                    if (d.height < lastHeight) {
                        d.height = lastHeight;
                    }
                    return d;
                }

            };
            stripPanel.setVisible(channelButton.isSelected());
//        	stripPanel.add(Box.createRigidArea(new Dimension(0, lastHeight)));
            if (stripPanel != null) {
                add(stripPanel, BorderLayout.NORTH); // !!! should have horiz scrollbar if necessary
                currentStripName = name;
            }
            revalidate();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        mixerControls.addObserver(mutationObserver);
    }

    @Override
    public void removeNotify() {
        mixerControls.deleteObserver(mutationObserver);
        if (doDispose) {
            mutationObserver = null;
        }
        super.removeNotify();
    }
}
