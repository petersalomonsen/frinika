/*
 * Created on February 11, 2007
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

import com.frinika.global.FrinikaConfig;
import static com.frinika.localization.CurrentLocale.getMessage;

import com.frinika.gui.AbstractDialog;
import com.frinika.gui.OptionsEditor;
import com.frinika.tootX.midi.MidiInDeviceManager;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.sequencer.SwingSongPositionListenerWrapper;
import com.frinika.sequencer.gui.TimeFormat;
import com.frinika.sequencer.gui.TimeSelector;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.midi.MidiMessageListener;
import com.frinika.sequencer.model.MidiLane;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.*;

/**
 * GUI-dialog of a MidiStepRecordAction.
 * 
 * Unlike other dialogs used in this package, this dialog is non-modal, thus 
 * once the dialog is opened, it remains 'floating' above the main window while
 * elements in the main-window remain editable.
 *
 * (Created with NetBeans 5.5 gui-editor, see corresponding .form file.)
 *
 * @see MidiStepRecordAction
 * @author Jens Gulden
 */
public class MidiStepRecordActionDialog extends AbstractDialog implements OptionsEditor, SongPositionListener, SelectionListener, MidiMessageListener {
    
    public final static long AUTO_RECORD_DELAY_INTERVAL = 1500; // ms after no new notes have been etered to commit an auto-record
    private final static Font BUFFER_TEXT_FIELD_FONT_NORMAL = new Font("DialogInput", Font.PLAIN, 12);
    private final static Font BUFFER_TEXT_FIELD_FONT_ITALICS = new Font("DialogInput", Font.ITALIC, 12);
    
    private MidiStepRecordAction action;
    private ProjectFrame frame;
    private TimeSelector positionTimeSelector;
    private TimeSelector stepTimeSelector;
    //private boolean autoRecord = false;
    private boolean bufferDirty = false;
    private MidiLane monitoredLane = null; // reference for attaching this as MidiMessageListener
    private Collection<Integer> currentlyPressedNotes = new HashSet<Integer>();
    private AutoRecordThread autoRecordThread = null;
    
    /** Creates new form MidiStepRecordActionDialog */
    public MidiStepRecordActionDialog(ProjectFrame frame, MidiStepRecordAction action) {
        super(frame, getMessage("sequencer.midi.step_record"), false); // non-modal
        this.frame = frame;
        this.action = action;
        MidiInDeviceManager.open(FrinikaConfig.getMidiInDeviceList());
        ProjectContainer project = frame.getProjectContainer();
        initComponents();
        positionTimeSelector = new TimeSelector(frame.getProjectContainer(), TimeFormat.BAR_BEAT_TICK);
        stepTimeSelector = new TimeSelector(frame.getProjectContainer(), TimeFormat.NOTE_LENGTH, true);
        stepTimeSelector.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            	MidiStepRecordActionDialog.this.action.step = stepTimeSelector.getTicks();
            }
        });
        positionTimeSelectorPanel.add(positionTimeSelector);
        stepPanel.add(stepTimeSelector);
        
        bufferTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                setBufferDirty(true);
            }
        });
        
        /*// close on esc:
        final String ESC_CANCEL = "esc-cancel";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESC_CANCEL);
        getRootPane().getActionMap().put(ESC_CANCEL, new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                close();
            }
        });*/
        
        project.getSequencer().addSongPositionListener(new SwingSongPositionListenerWrapper(this));
        
        project.getSequencer().addMidiMessageListener(this);

        project.getMidiSelection().addSelectionListener(this);
        
        this.getRootPane().setDefaultButton(recordButton);
        pack();
        refresh();
    }

    public void refresh() {
        action.position = frame.getProjectContainer().getSequencer().getTickPosition();
        stepTimeSelector.setTicks(action.step);
        positionTimeSelector.setTicks(action.position);
        lengthDiffSpinner.setValue(action.lengthDiff);
        velocitySpinner.setValue(action.velocity);
        autoRecordCheckBox.setSelected(action.autoRecord);
        refreshAutoRecord();
        
        // this MidiListener is for the vertical virtual keyboard of the pianoroll
        
        refreshPart();
    }
    
    private void refreshPart() {
        if (monitoredLane != null) {
            monitoredLane.removeMidiMessageListener(this);
        }
        action.part = frame.getProjectContainer().getMidiSelection().getMidiPart();
        if (action.part != null) {
            monitoredLane = (MidiLane)action.part.getLane();
            if (monitoredLane != null) {
                monitoredLane.addMidiMessageListener(this);
            }
        }
    }
    
    public void update() {
        action.step = stepTimeSelector.getTicks();
        action.position = positionTimeSelector.getTicks();
        action.lengthDiff = (Integer)lengthDiffSpinner.getValue();
        action.velocity = (Integer)velocitySpinner.getValue();
    }
    
    @Override
    public void hide() {
        if (monitoredLane != null) {
            monitoredLane.removeMidiMessageListener(this);
        }
        super.hide();
    }
    
    /**
     * Moves note from temporary buffer to track/lane.
     */
    void record() {
    	//MidiPart part = frame.getProjectContainer().getMidiSelection().getMidiPart();
    	refreshPart();
        String buffer = getBuffer();
        String actuallyRecorded = action.stepRecord(buffer);
        if (actuallyRecorded != null) {
        	setBuffer(actuallyRecorded);
        	setBufferDirty(false);
        	currentlyPressedNotes.clear();
        }
    }
    
    void clear() {
        setBuffer("");
    }
    
    void undo() {
        frame.getProjectContainer().getEditHistoryContainer().getUndoMenuItem().doClick();
    }
    
    void close() {
        hide();
    }
    
    /**
     * Implementation of MidiMessageListener.midiMessage()
     */
    public void midiMessage(MidiMessage message) {
    	if (message instanceof ShortMessage) {
            ShortMessage shm = (ShortMessage)message;
            int cmd = shm.getCommand();
            if ( cmd == ShortMessage.NOTE_ON || cmd == ShortMessage.NOTE_OFF ) {
                int velocity = shm.getData2();
        	if ( (velocity != 0) && (cmd == ShortMessage.NOTE_ON) ) {
                    if (this.isVisible()) {
                        if (currentlyPressedNotes.isEmpty()) {
                            clear();
                        }
        		int note = shm.getData1();
                        currentlyPressedNotes.add(note);
        		this.addToBuffer(note);
                        if ( action.autoRecord ) {
                            startAutoRecordInterval();
                        }
                    }
        	} else {
                    if (this.isVisible()) {
        		int note = shm.getData1();
                        currentlyPressedNotes.remove(note);
                        if ( ! currentlyPressedNotes.isEmpty() ) {
                            this.removeFromBuffer(note); // last single one remains
                        }
                    }
        	}
            }
    	}
    }
    
    public void selectionChanged(SelectionContainer selection) {
    	refreshPart(); // to replug input device if necessary
    }
    
    public void notifyTickPosition(long tick) {
        action.position = tick;
        positionTimeSelector.setTicks(tick);
    }
    
    public boolean requiresNotificationOnEachTick() {
        return false;
    }
    
    public void setBuffer(String s) {
        setBufferDirty(true);
        bufferTextField.setText(s);
    }
    
    public String getBuffer() {
        return bufferTextField.getText();
    }
    
    public void addToBuffer(String s) {
        String buffer = getBuffer();
        if ( buffer.toLowerCase().indexOf( s.toLowerCase() ) == -1 ) { // not yet there
            if (buffer.length() > 0) {
                buffer += " ";
            }
            buffer += s;
            setBuffer(buffer);
        }
    }
    
    public void removeFromBuffer(String s) {
        String buffer = getBuffer() + " ";
        s = s + " ";
        int i = buffer.toLowerCase().indexOf( s.toLowerCase() );
        if ( i != -1 ) {
            buffer = buffer.substring(0, i) + buffer.substring(i + s.length());
            setBuffer(buffer.trim());
        }
    }
    
    public void addToBuffer(int note) {
    	addToBuffer( MidiStepRecordAction.formatNote(note) );
    }
    
    public void removeFromBuffer(int note) {
    	removeFromBuffer( MidiStepRecordAction.formatNote(note) );
    }
    
    private void setBufferDirty(boolean dirty) {
        if ( dirty == this.bufferDirty ) return;
        this.bufferDirty = dirty;
        if ( dirty ) {
            bufferTextField.setFont(BUFFER_TEXT_FIELD_FONT_NORMAL);
        } else { // not dirty: italic font signalizes 'committed'
            bufferTextField.setFont(BUFFER_TEXT_FIELD_FONT_ITALICS);
            bufferTextField.selectAll(); // so user can continue typing next note(s) without clearing the textfield
        }
        bufferTextField.repaint();
    }
    
    /*private void clear() {
        
    }*/
    
    private void refreshAutoRecord() {
        action.autoRecord = autoRecordCheckBox.isSelected();
        if ( action.autoRecord && this.bufferDirty ) {
            record(); // initial one right at the time when checkbox set
        }
        //recordButton.setEnabled( ! autoRecord );
    }
    
    private synchronized void startAutoRecordInterval() {
        // wait a second, if no new notes, then auto-record
        long now = System.currentTimeMillis();
        long end = now + AUTO_RECORD_DELAY_INTERVAL; // TODO: configurable by user
        if (autoRecordThread == null) {
            autoRecordThread = new AutoRecordThread();
            autoRecordThread.endTime = end;
            autoRecordThread.start();
        } else {
            autoRecordThread.endTime = end;
        }
    }
    
    private synchronized void stopAutoRecordInterval() {
        autoRecordThread = null;
    }
    
    // --- inner class ---
    
    private class AutoRecordThread extends Thread {
        
        long endTime;
        
        @Override
        public void run() {
            try {
                while ( (autoRecordThread == this) && (System.currentTimeMillis() < endTime) ) { // endTime might get increased while looping
                    Thread.sleep(50);
                }
                
                if (autoRecordThread == this) { // not stopped?
                    recordButton.doClick(); // do auto-record
                }
            } catch (InterruptedException ex) {
                //ex.printStackTrace();
                //nop
            }
            stopAutoRecordInterval();
            // Thread exit
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

        mainPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        stepPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        positionTimeSelectorPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lengthDiffSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        velocitySpinner = new javax.swing.JSpinner();
        bufferTextField = new javax.swing.JTextField();
        buttonsPanel = new javax.swing.JPanel();
        autoRecordCheckBox = new javax.swing.JCheckBox();
        recordButton = new javax.swing.JButton();
        undoButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridBagLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        mainPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel1.setText("Step");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(jLabel1, gridBagConstraints);

        stepPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 8);
        mainPanel.add(stepPanel, gridBagConstraints);

        jLabel4.setText("Position");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mainPanel.add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mainPanel.add(positionTimeSelectorPanel, gridBagConstraints);

        jLabel2.setText("Length rel.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(jLabel2, gridBagConstraints);

        lengthDiffSpinner.setModel(new javax.swing.SpinnerNumberModel(action.lengthDiff, -999, 999, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(lengthDiffSpinner, gridBagConstraints);

        jLabel3.setText("Velocity");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(jLabel3, gridBagConstraints);

        velocitySpinner.setModel(new javax.swing.SpinnerNumberModel(action.velocity, 1, 127, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        mainPanel.add(velocitySpinner, gridBagConstraints);

        bufferTextField.setFont(BUFFER_TEXT_FIELD_FONT_NORMAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 3, 10, 3);
        mainPanel.add(bufferTextField, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        autoRecordCheckBox.setText("auto step");
        autoRecordCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoRecordCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        autoRecordCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                autoRecordCheckBoxStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 3, 3);
        buttonsPanel.add(autoRecordCheckBox, gridBagConstraints);

        recordButton.setForeground(java.awt.Color.red);
        recordButton.setMnemonic('P');
        recordButton.setText("  Step  ");
        recordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recordButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        buttonsPanel.add(recordButton, gridBagConstraints);

        undoButton.setMnemonic('U');
        undoButton.setText(" Undo ");
        undoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 3);
        buttonsPanel.add(undoButton, gridBagConstraints);

        clearButton.setMnemonic('C');
        clearButton.setText(" Clear ");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 3);
        buttonsPanel.add(clearButton, gridBagConstraints);

        closeButton.setMnemonic((char)27);
        closeButton.setText(" Close ");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        buttonsPanel.add(closeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        mainPanel.add(buttonsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(mainPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        stopAutoRecordInterval();
        clear();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        stopAutoRecordInterval();
        close();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void recordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recordButtonActionPerformed
        stopAutoRecordInterval();
        record();
    }//GEN-LAST:event_recordButtonActionPerformed

    private void undoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoButtonActionPerformed
        stopAutoRecordInterval();
        undo();
    }//GEN-LAST:event_undoButtonActionPerformed

    private void autoRecordCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoRecordCheckBoxStateChanged
        refreshAutoRecord();
    }//GEN-LAST:event_autoRecordCheckBoxStateChanged
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoRecordCheckBox;
    private javax.swing.JTextField bufferTextField;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSpinner lengthDiffSpinner;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel positionTimeSelectorPanel;
    private javax.swing.JButton recordButton;
    private javax.swing.JPanel stepPanel;
    private javax.swing.JButton undoButton;
    private javax.swing.JSpinner velocitySpinner;
    // End of variables declaration//GEN-END:variables
    
}
