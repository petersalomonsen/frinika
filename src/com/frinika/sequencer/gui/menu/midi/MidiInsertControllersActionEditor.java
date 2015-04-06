/*
 * Created on February 10, 2007
 * 
 * Copyright (c) 2007 Jens Gulden
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

package com.frinika.sequencer.gui.menu.midi;

import com.frinika.gui.OptionsDialog;
import com.frinika.gui.OptionsEditor;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.ControllerSelector;
import com.frinika.sequencer.gui.TimeFormat;
import com.frinika.sequencer.gui.TimeSelector;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MultiEvent;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * GUI-component for setting options of a MidiInsertControllersAction.
 *
 * (Created with NetBeans 5.5 gui-editor, see corresponding .form file.)
 *
 * @see MidiInsertControllersAction
 * @author Jens Gulden
 */
public class MidiInsertControllersActionEditor extends JPanel implements OptionsEditor {
    
    private MidiInsertControllersAction action;
    private ProjectFrame frame;
    private TimeSelector startTimeSelector;
    private TimeSelector lengthTimeSelector;
    private TimeSelector resolutionTimeSelector;
    private ControllerSelector controllerSelector;
    private Map<AbstractButton, MidiInsertControllersAction.ControllerFunction> functionButtons;
    private Map<MidiInsertControllersAction.ControllerFunction, JComponent> functions;
    
    /** Creates new form MidiInsertControllersActionEditor */
    public MidiInsertControllersActionEditor(ProjectFrame frame, MidiInsertControllersAction action) {
        super();
        this.frame = frame;
        this.action = action;
        initComponents();
        ProjectContainer project = frame.getProjectContainer();
        startTimeSelector = new TimeSelector(project, TimeFormat.BAR_BEAT_TICK);
        startTimeSelectorPanel.add(startTimeSelector);
        lengthTimeSelector = new TimeSelector(project, TimeFormat.BEAT_TICK);
        lengthTimeSelectorPanel.add(lengthTimeSelector);
        resolutionTimeSelector = new TimeSelector(project, TimeFormat.BEAT_TICK);
        resolutionTimeSelectorPanel.add(resolutionTimeSelector);
        controllerSelector = new ControllerSelector();
        controllerSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	MidiInsertControllersActionEditor.this.action.controller = controllerSelector.getControllerType(); // must be passed directly on each change to allow Functions gui to react
            }
        });
        controllerSelectorPanel.add(controllerSelector);
        Collection<MidiInsertControllersAction.ControllerFunction> ff = action.getAvailableControllerFunctions();
        functions = new HashMap<MidiInsertControllersAction.ControllerFunction, JComponent>();
        functionButtons = new HashMap<AbstractButton, MidiInsertControllersAction.ControllerFunction>();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = 3;
        gbc.insets.right = 3;
        for (MidiInsertControllersAction.ControllerFunction function : ff) {
            String name = function.getName();
            Icon icon = function.getIcon(50, 30);
            JRadioButton rb = new JRadioButton(name);
            functionsButtonGroup.add(rb);
            functionButtons.put(rb, function);
            rb.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JRadioButton rb = (JRadioButton)e.getSource();
                    if (rb.isSelected()) {
                    	MidiInsertControllersAction.ControllerFunction f = functionButtons.get(rb);
                        JComponent gui = functions.get(f);
                        if (gui == null) {
                            gui = f.createGUI();
                            if (gui == null) {
                                gui = new JPanel(new GridBagLayout());
                                gui.add(new JLabel("(this function has no options)"), new GridBagConstraints());
                            }
                            functions.put(f, gui);
                        }
                        functionOptionsPanel.removeAll();
                        functionOptionsPanel.add(gui);
                        Container parent = MidiInsertControllersActionEditor.this.getParent();
                        if (parent != null) {
                        	functionOptionsPanel.validate();
                        	((OptionsDialog)parent.getParent().getParent().getParent().getParent().getParent()).validate(); // hack, obviously
                        	functionOptionsPanel.repaint();
                        }
                    }
                }
            });
            functionsPanel.add(rb, gbc);
            JLabel label;
            if ( icon != null) {
                label = new JLabel(icon);
            } else {
                label = new JLabel(name);
            }
            functionsPanel.add(label, gbc);
            functionsPanel.add(new JPanel(), gbc); // spacer
        }
        functionsButtonGroup.getElements().nextElement().setSelected(true); // first one by default
    }
    
    public void refresh() {
    	long start = frame.getProjectContainer().getSequencer().getTickPosition();
        startTimeSelector.setTicks(start);
        lengthTimeSelector.setTicks(action.length);
        resolutionTimeSelector.setTicks(action.resolution);
        MidiInsertControllersAction.ControllerFunction f = action.function;
        for (AbstractButton radiobutton : functionButtons.keySet()) {
        	MidiInsertControllersAction.ControllerFunction ff = functionButtons.get(radiobutton);
            if (ff == f) {
                radiobutton.setSelected(true);
            }
        }
        MultiEvent first = action.events.iterator().next();
        controllerSelector.setControllerList(((MidiLane)first.getMidiPart().getLane()).getControllerList());
        controllerSelector.setControllerType(action.controller);
        controllerSelector.addPseudoController("(Note)", -1);
    }
    
    public void update() {
        action.start = startTimeSelector.getTicks();
        action.length = lengthTimeSelector.getTicks();
        action.resolution = resolutionTimeSelector.getTicks();
        action.controller = controllerSelector.getControllerType();
        for (AbstractButton radiobutton : functionButtons.keySet()) {
        	if (radiobutton.isSelected()) {
                    action.function = functionButtons.get( radiobutton );
        	}
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        functionsButtonGroup = new javax.swing.ButtonGroup();
        headPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        startTimeSelectorPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lengthTimeSelectorPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        controllerSelectorPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        resolutionTimeSelectorPanel = new javax.swing.JPanel();
        functionsPanel = new javax.swing.JPanel();
        functionOptionsPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        headPanel.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Insert at");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        headPanel.add(jLabel1, gridBagConstraints);

        startTimeSelectorPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        headPanel.add(startTimeSelectorPanel, gridBagConstraints);

        jLabel2.setText("Length");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        headPanel.add(jLabel2, gridBagConstraints);

        lengthTimeSelectorPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        headPanel.add(lengthTimeSelectorPanel, gridBagConstraints);

        jLabel4.setText("Controller");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        headPanel.add(jLabel4, gridBagConstraints);

        controllerSelectorPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        headPanel.add(controllerSelectorPanel, gridBagConstraints);

        jLabel3.setText("at each");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        headPanel.add(jLabel3, gridBagConstraints);

        resolutionTimeSelectorPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 3, 5, 3);
        headPanel.add(resolutionTimeSelectorPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(headPanel, gridBagConstraints);

        functionsPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 3, 8, 3);
        add(functionsPanel, gridBagConstraints);

        functionOptionsPanel.setLayout(new java.awt.GridBagLayout());

        functionOptionsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(functionOptionsPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controllerSelectorPanel;
    private javax.swing.JPanel functionOptionsPanel;
    private javax.swing.ButtonGroup functionsButtonGroup;
    private javax.swing.JPanel functionsPanel;
    private javax.swing.JPanel headPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel lengthTimeSelectorPanel;
    private javax.swing.JPanel resolutionTimeSelectorPanel;
    private javax.swing.JPanel startTimeSelectorPanel;
    // End of variables declaration//GEN-END:variables
    
}
