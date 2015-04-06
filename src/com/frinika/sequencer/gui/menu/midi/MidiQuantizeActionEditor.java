/*
 * Created on February 7, 2007
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

import com.frinika.gui.AbstractDialog.MoreLessButtonListener;
import com.frinika.gui.OptionsEditor;
import com.frinika.sequencer.gui.SliderNumberEditable;
import com.frinika.sequencer.gui.TimeSelector;
import com.frinika.sequencer.midi.groovepattern.GroovePattern;
import com.frinika.sequencer.midi.groovepattern.GroovePatternManager;
import com.frinika.sequencer.midi.groovepattern.gui.GroovePatternManagerDialog;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * GUI-component for setting options of a MidiQuantizeAction.
 *
 * (Created with NetBeans 5.5 gui-editor, see corresponding .form file.)
 *
 * @see MidiQuantizeAction
 * @author Jens Gulden
 */
class MidiQuantizeActionEditor extends JPanel implements OptionsEditor {
    
    private MidiQuantizeAction action;
    private int[] ticks;
    private MoreLessButtonListener moreLessButtonListener = null;
    private SliderNumberEditable intensitySlider;
    private SliderNumberEditable swingSlider;
    private SliderNumberEditable smudgeSlider;
    private SliderNumberEditable velocitySlider;
    
    /** Creates new form MidiQuantizeActionEditorPanel */
    public MidiQuantizeActionEditor(MidiQuantizeAction action) {
        super();
        this.action = action;
        initComponents();
        
        intensitySlider = createSlider();
        intensitySliderPanel.add(intensitySlider);
        
        swingSlider = createSlider();
        swingSliderPanel.add(swingSlider);
        
        SliderNumberEditable slider = new SliderNumberEditable(50f, 0f, 100f, 1f, null, "%", SwingConstants.HORIZONTAL);
    	slider.setMinorTickSpacing(5);
    	slider.setMajorTickSpacing(25);
    	slider.setPaintLabels(true);
    	slider.setPaintTicks(true);
    	slider.setPaintTrack(true);
        smudgeSlider = slider;
        smudgeSliderPanel.add(smudgeSlider);
        
        velocitySlider = createSlider();
        velocitySliderPanel.add(velocitySlider);
        
        // TODO react on mnemonics for sliders (currently only displayed via labels)
    	ticks = new int[TimeSelector.NOTE_LENGTH_FACTORS.length];
        for (int i = 0; i < TimeSelector.NOTE_LENGTH_FACTORS.length; i++) {
            ticks[i] = (int)Math.round(action.getProjectFrame().getProjectContainer().getSequence().getResolution() * 4 * TimeSelector.NOTE_LENGTH_FACTORS[i]);
        }
        resolutionList.setListData(TimeSelector.NOTE_LENGTH_NAMES);
    }
    
    private static SliderNumberEditable createSlider() {
        SliderNumberEditable slider = new SliderNumberEditable(0f, -100f, 100f, 1f, null, "%", SwingConstants.HORIZONTAL);
    	slider.setMinorTickSpacing(10);
    	slider.setMajorTickSpacing(50);
    	slider.setPaintLabels(true);
    	slider.setPaintTicks(true);
    	slider.setPaintTrack(true);
    	//slider.setSnapToTicks(true);
        return slider;
    }
    
    public void update() { // gui to model
        action.q.interval = this.ticks[ resolutionList.getSelectedIndex() ];
        action.q.intensity = (float)intensitySlider.getValue() / intensitySlider.getMaximum();
        action.q.quantizeNoteStart = noteStartCheckBox.isSelected();
        action.q.quantizeNoteLength = noteLengthCheckBox.isSelected();
        action.q.swing = (float)swingSlider.getValue() / 100;
        boolean grooveQuantize = grooveQuantizeCheckBox.isSelected();
        Object o = groovePatternComboBox.getSelectedItem();
        if (grooveQuantize && (o instanceof GroovePattern)) { //&& (o != null) 
        	action.q.groovePattern = (GroovePattern)o;
        } else {
        	action.q.groovePattern = null;
        }
        action.q.smudge = (float)smudgeSlider.getValue() / 100;
        action.q.velocity = (float)velocitySlider.getValue() / 100;
    }
    
    public void refresh() { // model to gui
    	if (moreLessButtonListener == null) { // need to initialize after dialog is there
    		moreLessButtonListener = action.getDialog().registerMoreLessButtonPanel(moreLessButton, morePanel);
    	}
    	
        int currentIndex = 4;
        for (int i = 0; i < TimeSelector.NOTE_LENGTH_FACTORS.length; i++) {
            if (ticks[i] == action.q.interval) {
                currentIndex = i;
            }
        }
        
        resolutionList.setSelectedIndex(currentIndex);
        resolutionList.ensureIndexIsVisible(currentIndex);
    	
        intensitySlider.setValue((int)(100 * action.q.intensity));
        noteStartCheckBox.setSelected(action.q.quantizeNoteStart);
        noteLengthCheckBox.setSelected(action.q.quantizeNoteLength);
    	
        swingSlider.setValue((int)(100 * action.q.swing));
        
        refreshGroovePatternComboBox();
        
        grooveQuantizeCheckBox.setSelected( (action.q.groovePattern != null) );
       	groovePatternComboBox.setSelectedItem( action.q.groovePattern );
        
        smudgeSlider.setValue((int)(100 * action.q.smudge));
        velocitySlider.setValue((int)(100 * action.q.velocity));
        
        grooveQuantizeCheckBoxStateChanged(null);
    }
    
    private void refreshGroovePatternComboBox() {
        GroovePatternManager gpm = GroovePatternManager.getInstance();
        Vector items = new Vector(); // untyped: mixed String / GroovePattern
        items.add("- Presets -");
        for (GroovePattern gp : gpm.getPresetGroovePatterns()) {
            items.add(gp);
        }
        items.add("- User Patterns -");
        for (GroovePattern gp : gpm.getUserGroovePatterns()) {
            items.add(gp);
        }
        groovePatternComboBox.setModel(new DefaultComboBoxModel(items));
    }
    
    private void openGroovePatternManagerDialog() {
    	GroovePatternManagerDialog.showDialog(action.getProjectFrame());
    	refreshGroovePatternComboBox();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        resolutionListScrollPane = new javax.swing.JScrollPane();
        resolutionList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        intensitySliderPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        noteStartCheckBox = new javax.swing.JCheckBox();
        noteLengthCheckBox = new javax.swing.JCheckBox();
        moreLessButton = new javax.swing.JButton();
        morePanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        swingSliderPanel = new javax.swing.JPanel();
        grooveQuantizeCheckBox = new javax.swing.JCheckBox();
        groovePatternComboBox = new javax.swing.JComboBox();
        groovePatternManagerButton = new javax.swing.JButton();
        smudgeLabel = new javax.swing.JLabel();
        smudgeSliderPanel = new javax.swing.JPanel();
        velocityLabel = new javax.swing.JLabel();
        velocitySliderPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setDisplayedMnemonic('R');
        jLabel1.setText("Resolution");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

        jPanel1.setLayout(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        add(jPanel1, gridBagConstraints);

        resolutionListScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        resolutionList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                resolutionListValueChanged(evt);
            }
        });

        resolutionListScrollPane.setViewportView(resolutionList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        add(resolutionListScrollPane, gridBagConstraints);

        jLabel2.setDisplayedMnemonic('I');
        jLabel2.setText("Intensity");
        jLabel2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jLabel2FocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel2, gridBagConstraints);

        intensitySliderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(intensitySliderPanel, gridBagConstraints);

        jLabel3.setText("Apply to");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel3, gridBagConstraints);

        noteStartCheckBox.setMnemonic('n');
        noteStartCheckBox.setSelected(action.q.quantizeNoteStart);
        noteStartCheckBox.setText("note start times");
        noteStartCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noteStartCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        noteStartCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                noteStartCheckBoxStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(noteStartCheckBox, gridBagConstraints);

        noteLengthCheckBox.setMnemonic('l');
        noteLengthCheckBox.setSelected(action.q.quantizeNoteLength);
        noteLengthCheckBox.setText("note lengths");
        noteLengthCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noteLengthCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        noteLengthCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                noteLengthCheckBoxStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(noteLengthCheckBox, gridBagConstraints);

        moreLessButton.setMnemonic('M');
        moreLessButton.setText("<< Less");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        add(moreLessButton, gridBagConstraints);

        morePanel.setLayout(new java.awt.GridBagLayout());

        morePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel4.setDisplayedMnemonic('S');
        jLabel4.setText("Swing");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 2, 0, 5);
        morePanel.add(jLabel4, gridBagConstraints);

        swingSliderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        morePanel.add(swingSliderPanel, gridBagConstraints);

        grooveQuantizeCheckBox.setMnemonic('G');
        grooveQuantizeCheckBox.setText("Groove Quantize");
        grooveQuantizeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        grooveQuantizeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        grooveQuantizeCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                grooveQuantizeCheckBoxStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 2, 5);
        morePanel.add(grooveQuantizeCheckBox, gridBagConstraints);

        groovePatternComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 2, 5);
        morePanel.add(groovePatternComboBox, gridBagConstraints);

        groovePatternManagerButton.setMnemonic('P');
        groovePatternManagerButton.setText("Patterns...");
        groovePatternManagerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groovePatternManagerButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 2, 2);
        morePanel.add(groovePatternManagerButton, gridBagConstraints);

        smudgeLabel.setText("Smudge");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 2, 5);
        morePanel.add(smudgeLabel, gridBagConstraints);

        smudgeSliderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 2, 5);
        morePanel.add(smudgeSliderPanel, gridBagConstraints);

        velocityLabel.setText("Velocity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 2, 5);
        morePanel.add(velocityLabel, gridBagConstraints);

        velocitySliderPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 2, 2, 5);
        morePanel.add(velocitySliderPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(morePanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void jLabel2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jLabel2FocusGained
        intensitySlider.requestFocus();
    }//GEN-LAST:event_jLabel2FocusGained

    private void grooveQuantizeCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_grooveQuantizeCheckBoxStateChanged
        boolean sel = grooveQuantizeCheckBox.isSelected();
        groovePatternComboBox.setEnabled(sel);
        smudgeSlider.setEnabled(sel);
        velocitySlider.setEnabled(sel);
        smudgeLabel.setEnabled(sel);
        velocityLabel.setEnabled(sel);        
    }//GEN-LAST:event_grooveQuantizeCheckBoxStateChanged

    private void groovePatternManagerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groovePatternManagerButtonActionPerformed
        openGroovePatternManagerDialog();
    }//GEN-LAST:event_groovePatternManagerButtonActionPerformed

    private void resolutionListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_resolutionListValueChanged
        action.q.interval = this.ticks[ resolutionList.getSelectedIndex() ];
    }//GEN-LAST:event_resolutionListValueChanged

    private void noteStartCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_noteStartCheckBoxStateChanged
        action.q.quantizeNoteStart = noteStartCheckBox.isSelected();
    }//GEN-LAST:event_noteStartCheckBoxStateChanged

    private void noteLengthCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_noteLengthCheckBoxStateChanged
        action.q.quantizeNoteLength = noteLengthCheckBox.isSelected();
    }//GEN-LAST:event_noteLengthCheckBoxStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox groovePatternComboBox;
    private javax.swing.JButton groovePatternManagerButton;
    private javax.swing.JCheckBox grooveQuantizeCheckBox;
    private javax.swing.JPanel intensitySliderPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton moreLessButton;
    private javax.swing.JPanel morePanel;
    private javax.swing.JCheckBox noteLengthCheckBox;
    private javax.swing.JCheckBox noteStartCheckBox;
    private javax.swing.JList resolutionList;
    private javax.swing.JScrollPane resolutionListScrollPane;
    private javax.swing.JLabel smudgeLabel;
    private javax.swing.JPanel smudgeSliderPanel;
    private javax.swing.JPanel swingSliderPanel;
    private javax.swing.JLabel velocityLabel;
    private javax.swing.JPanel velocitySliderPanel;
    // End of variables declaration//GEN-END:variables
    
}
