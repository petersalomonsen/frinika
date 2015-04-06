/*
 * Created on Jun 30, 2007
 *
 * Copyright (c) 2006-2007 Jens Gulden
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

package com.frinika.global;

import com.frinika.project.FrinikaAudioSystem;
import com.frinika.gui.util.PropertiesEditor;
import java.util.Collection;
import java.util.Vector;

import com.frinika.project.gui.ProjectFrame;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Content panel for the global configuration dialog. This dialog includes all
 * gui-elements for the global options dialog.
 *
 * As a special thing, the gui-elements just need to be named appropriately
 * and be declared non-private, then bound to appropriate config-fields in 
 * FrinikaConfig. NO ACTIVE CODE FOR GETTING / SETTING options from
 * the Config class needs to be implemented, as this is done by 
 * DefaultOptionsBinder.
 *
 * This has been initially created as a gui-form with Netbeans 5.5 GUI-builder, 
 * but as this is a shared class, this isn't likely to be kept. So feel free
 * to add code manually.
 *
 * Thank you I have started to add code manually PJL.
 *
 * @see DefaultOptionsBinder
 * @author Jens Gulden
 * 
 * 
 */
public class ConfigDialogPanel extends JPanel {
    
    private ProjectFrame frame;
    private PropertiesEditor audioPropertiesEditor;
	private JLabel jLabelAudioDirectory;
	JTextField textfieldAudioDirectory;
	private JButton buttonPickAudioDirectory;
	
	private JLabel jLabelSoundFontDirectory;
	JTextField textfieldSoundFontDirectory;
	private JButton buttonPickSoundFontDirectory;

        private JLabel jLabelPatchNameDirectory;
	JTextField textfieldPatchNameDirectory;
	private JButton buttonPickPatchNameDirectory;
        
        private JLabel jLabelDefaultSoundFont;
	JTextField textfieldDefaultSoundFont;
	private JButton buttonPickDefaultSoundFont;
    
    /** Creates new form ConfigDialogPanel */
    public ConfigDialogPanel(ProjectFrame frame) {
        this.frame = frame;
        initComponents();
        audioPropertiesEditor = new PropertiesEditor(FrinikaConfig.getProperties());
        audioPropertiesPanel.add(audioPropertiesEditor);
        //textfieldBufferSize.setMinimumSize(new Dimension(50, textfieldBufferSize.getMinimumSize().height));
        
        // not implemented yet:
        audioPanel.remove(labelOutputDevice);
        audioPanel.remove(comboboxOutputDevice);
        audioPanel.remove(buttonStopOutputDevice);
        audioPanel.remove(labelChannels);
        audioPanel.remove(comboboxChannels);
        audioPanel.remove(labelUnderrunTolerance);
        audioPanel.remove(spinnerUnderrunTolerance);
        audioPanel.remove(labelBits);
        audioPanel.remove(comboboxBits);
        audioPanel.remove(labelPriority);
        audioPanel.remove(spinnerPriority);
        userInterfacePanel.remove(labelRedrawRate);
        userInterfacePanel.remove(comboboxRedrawRate);
        
        refreshMidiInDevicesList();
 //       refreshAudioDevicesList();
    }
    
    private void refreshMidiInDevicesList() {
        final Vector<String> v = FrinikaConfig.getMidiInDeviceList();
        listInputDevices.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return v.size(); }
            public Object getElementAt(int i) { return v.elementAt(i); }
        });
    }
    
    private void refreshAudioDevicesList() {
        Collection<String> v = FrinikaConfig.getAvailableAudioDevices();
        String[] ss = new String[v.size()];
        int i = 0;
        for (String s : v) {
        	ss[i++] = s;
        }
        comboboxOutputDevice.setModel(new DefaultComboBoxModel(ss));
    }
    
 
   
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tabbedPane = new javax.swing.JTabbedPane();
        audioPanel = new javax.swing.JPanel();
        labelOutputDevice = new javax.swing.JLabel();
        comboboxOutputDevice = new javax.swing.JComboBox();
        buttonStopOutputDevice = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        comboboxSampleRate = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        spinnerBufferSize = new javax.swing.JSpinner();
        
        // PJL
        jLabel3a = new javax.swing.JLabel();
        spinnerTicksPerQuarter = new javax.swing.JSpinner();
        
        jLabel3b = new javax.swing.JLabel();
        spinnerSequencerPriority = new javax.swing.JSpinner();
        //
        
        jLabel4 = new javax.swing.JLabel();
        labelChannels = new javax.swing.JLabel();
        comboboxChannels = new javax.swing.JComboBox();
        labelUnderrunTolerance = new javax.swing.JLabel();
        spinnerUnderrunTolerance = new javax.swing.JSpinner();
        labelBits = new javax.swing.JLabel();
        comboboxBits = new javax.swing.JComboBox();
        labelPriority = new javax.swing.JLabel();
        spinnerPriority = new javax.swing.JSpinner();
        jSeparator2 = new javax.swing.JSeparator();
        audioPropertiesPanel = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        spinnerOutputLatency = new javax.swing.JSpinner();
        buttonMeasureLatency = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        checkboxUseDirectMonitoring = new javax.swing.JCheckBox();
        checkboxUseMultiplexedJavasoundServer = new javax.swing.JCheckBox();
        checkboxAutoconnectJack = new javax.swing.JCheckBox();
        checkboxBigEndian = new javax.swing.JCheckBox();
        midiPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listInputDevices = new javax.swing.JList();
        jPanel6 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        buttonAddInputDevice = new javax.swing.JButton();
        buttonRemoveInputDevice = new javax.swing.JButton();
        userInterfacePanel = new javax.swing.JPanel();
        labelRedrawRate = new javax.swing.JLabel();
        comboboxRedrawRate = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        spinnerMouseDragSpeedSpinners = new javax.swing.JSpinner();
        jSeparator4 = new javax.swing.JSeparator();
        jLabel11 = new javax.swing.JLabel();
        textfieldFontTextLane = new javax.swing.JTextField();
        buttonPickFontTextLane = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        checkboxOpenMaximizedWindow = new javax.swing.JCheckBox();
        directoriesPanel = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        textfieldGroovePatternsDirectory = new javax.swing.JTextField();
        buttonPickGroovePatternsDirectory = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        textfieldScriptsDirectory = new javax.swing.JTextField();
        buttonPickScriptsDirectory = new javax.swing.JButton();
        
        // PJL
        jLabelAudioDirectory = new javax.swing.JLabel();
        textfieldAudioDirectory = new javax.swing.JTextField();
        buttonPickAudioDirectory = new javax.swing.JButton();
        
        
        jLabelSoundFontDirectory = new javax.swing.JLabel();
        textfieldSoundFontDirectory = new javax.swing.JTextField();
        buttonPickSoundFontDirectory = new javax.swing.JButton();

           jLabelPatchNameDirectory = new javax.swing.JLabel();
        textfieldPatchNameDirectory = new javax.swing.JTextField();
        buttonPickPatchNameDirectory = new javax.swing.JButton();
        
        
        jLabelDefaultSoundFont = new javax.swing.JLabel();
        textfieldDefaultSoundFont = new javax.swing.JTextField();
        buttonPickDefaultSoundFont = new javax.swing.JButton();
        //----
        
        
        setLayout(new java.awt.BorderLayout());

        audioPanel.setLayout(new java.awt.GridBagLayout());

        labelOutputDevice.setText("Output Device:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(labelOutputDevice, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(comboboxOutputDevice, gridBagConstraints);

        buttonStopOutputDevice.setText("Stop");
        buttonStopOutputDevice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStopOutputDeviceActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(buttonStopOutputDevice, gridBagConstraints);

        jLabel2.setText("Sample Rate:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(jLabel2, gridBagConstraints);

        comboboxSampleRate.setEditable(true);
        comboboxSampleRate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "48000", "44100", "22050" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(comboboxSampleRate, gridBagConstraints);

        jLabel3.setText("Buffer Size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(jLabel3, gridBagConstraints);

        spinnerBufferSize.setModel(new javax.swing.SpinnerNumberModel(512, 0, 9999, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(spinnerBufferSize, gridBagConstraints);
        
     
        
        jLabel4.setText("msec");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(jLabel4, gridBagConstraints);

        labelChannels.setText("Channels:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(labelChannels, gridBagConstraints);

        comboboxChannels.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2 Stereo", "1 Mono" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(comboboxChannels, gridBagConstraints);

        labelUnderrunTolerance.setText("Underrun Tolerance:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(labelUnderrunTolerance, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(spinnerUnderrunTolerance, gridBagConstraints);

        labelBits.setText("Bits:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(labelBits, gridBagConstraints);

        comboboxBits.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "16", "24" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(comboboxBits, gridBagConstraints);

        labelPriority.setText("Priority:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(labelPriority, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(spinnerPriority, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(jSeparator2, gridBagConstraints);

        audioPropertiesPanel.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(audioPropertiesPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(jSeparator1, gridBagConstraints);

        jLabel7.setText("Output Latency (samples):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(jLabel7, gridBagConstraints);

        spinnerOutputLatency.setModel(new javax.swing.SpinnerNumberModel(0, 0, 9999, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(spinnerOutputLatency, gridBagConstraints);

        buttonMeasureLatency.setText("Measure latency...");
        buttonMeasureLatency.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMeasureLatencyActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(buttonMeasureLatency, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(jSeparator5, gridBagConstraints);

        checkboxUseDirectMonitoring.setText("Use Direct Monitoring");
        checkboxUseDirectMonitoring.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkboxUseDirectMonitoring.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(checkboxUseDirectMonitoring, gridBagConstraints);

        checkboxUseMultiplexedJavasoundServer.setText("Use Multiplexed Javasound Server (Requires Restart)");
        checkboxUseMultiplexedJavasoundServer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkboxUseMultiplexedJavasoundServer.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(checkboxUseMultiplexedJavasoundServer, gridBagConstraints);

        checkboxAutoconnectJack.setText("Autoconnect Jack (Requires Restart)");
        checkboxAutoconnectJack.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkboxAutoconnectJack.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(checkboxAutoconnectJack, gridBagConstraints);

        checkboxBigEndian.setText("Big Endian");
        checkboxBigEndian.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkboxBigEndian.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        audioPanel.add(checkboxBigEndian, gridBagConstraints);

        tabbedPane.addTab("Audio", audioPanel);

        midiPanel.setLayout(new java.awt.GridBagLayout());

        jLabel8.setText("Input Devices:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        midiPanel.add(jLabel8, gridBagConstraints);

        jScrollPane1.setViewportView(listInputDevices);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        midiPanel.add(jScrollPane1, gridBagConstraints);

        jPanel6.setLayout(null);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 0.5;
        midiPanel.add(jPanel6, gridBagConstraints);

        jPanel5.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        midiPanel.add(jPanel5, gridBagConstraints);

        buttonAddInputDevice.setText("Add");
        buttonAddInputDevice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddInputDeviceActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        midiPanel.add(buttonAddInputDevice, gridBagConstraints);

        buttonRemoveInputDevice.setText("Remove");
        buttonRemoveInputDevice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRemoveInputDeviceActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        midiPanel.add(buttonRemoveInputDevice, gridBagConstraints);
        
        // PJL
        jLabel3a.setText("Default Ticks Per Beat:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        midiPanel.add(jLabel3a, gridBagConstraints);

        spinnerTicksPerQuarter.setModel(new javax.swing.SpinnerNumberModel(FrinikaConfig.TICKS_PER_QUARTER, 0, 9999, 1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        midiPanel.add(spinnerTicksPerQuarter, gridBagConstraints);
        
        //  
        jLabel3b.setText("Seqeuncer Priority:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx=0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        midiPanel.add(jLabel3b, gridBagConstraints);
        gridBagConstraints.gridx=1;
        spinnerSequencerPriority.setModel(new javax.swing.SpinnerNumberModel(0, 0, 60, 1));
        midiPanel.add(spinnerSequencerPriority, gridBagConstraints);
        // ---
        
        tabbedPane.addTab("MIDI", midiPanel);

        userInterfacePanel.setLayout(new java.awt.GridBagLayout());

        labelRedrawRate.setText("Redraw Rate (jumps):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(labelRedrawRate, gridBagConstraints);

        comboboxRedrawRate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Disable when playing", "1" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(comboboxRedrawRate, gridBagConstraints);

        jLabel10.setText("Mouse drag speed on number-spinners:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(jLabel10, gridBagConstraints);

        spinnerMouseDragSpeedSpinners.setModel(new javax.swing.SpinnerNumberModel(2.0, 0.1, 10.0, 0.1));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(spinnerMouseDragSpeedSpinners, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(jSeparator4, gridBagConstraints);

        jLabel11.setText("Font in Text lanes:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(jLabel11, gridBagConstraints);

        textfieldFontTextLane.setText("Arial,8,plain");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        userInterfacePanel.add(textfieldFontTextLane, gridBagConstraints);

        buttonPickFontTextLane.setText("Pick Font...");
        buttonPickFontTextLane.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPickFontTextLaneActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(buttonPickFontTextLane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(jSeparator3, gridBagConstraints);

        checkboxOpenMaximizedWindow.setText("Open maximized window");
        checkboxOpenMaximizedWindow.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkboxOpenMaximizedWindow.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        userInterfacePanel.add(checkboxOpenMaximizedWindow, gridBagConstraints);

        tabbedPane.addTab("User Interface", userInterfacePanel);

        
        // Directories panel ......................
        directoriesPanel.setLayout(new java.awt.GridBagLayout());

        jLabel14.setText("Groove-Patterns Storage Directory:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(jLabel14, gridBagConstraints);

        textfieldGroovePatternsDirectory.setText("~/frinika/groove-patterns/");
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx=1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        directoriesPanel.add(textfieldGroovePatternsDirectory, gridBagConstraints);

        buttonPickGroovePatternsDirectory.setText("Pick Directory...");
        buttonPickGroovePatternsDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPickGroovePatternsDirectoryActionPerformed(evt);
            }
        });

        gridBagConstraints.gridx=2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(buttonPickGroovePatternsDirectory, gridBagConstraints);

        //-----------------------------   SCRIPTS
        jLabel15.setText("JavaScript Storage Directory:");
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=1;
        
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(jLabel15, gridBagConstraints);

        textfieldScriptsDirectory.setText("~/frinika/scripts/");

        gridBagConstraints.gridx=1;
//gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        directoriesPanel.add(textfieldScriptsDirectory, gridBagConstraints);

        buttonPickScriptsDirectory.setText("Pick Directory...");
        buttonPickScriptsDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPickScriptsDirectoryActionPerformed(evt);
            }
        });
        
        gridBagConstraints.gridx=2;

       // gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(buttonPickScriptsDirectory, gridBagConstraints);
        
        
        // PJL  AUDIO -----------------------------------------------------------------------------
        jLabelAudioDirectory.setText("Audio Storage Directory:");
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=2;
        
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(jLabelAudioDirectory, gridBagConstraints);

        textfieldAudioDirectory.setText("~/frinika/audio/");
        // gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx=1;

        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        directoriesPanel.add(textfieldAudioDirectory, gridBagConstraints);

        buttonPickAudioDirectory.setText("Pick Directory...");
        buttonPickAudioDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPickAudioDirectoryActionPerformed(evt);
            }
        });
        
        gridBagConstraints.gridx=2;

        //gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(buttonPickAudioDirectory, gridBagConstraints);

        //---------------------- SOUND FONT
        
        jLabelSoundFontDirectory.setText("Soundfont Storage Directory:");
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy=3;

        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(jLabelSoundFontDirectory, gridBagConstraints);

        textfieldSoundFontDirectory.setText("~/frinika/soundfont/");
        // gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx=1;

        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        directoriesPanel.add(textfieldSoundFontDirectory, gridBagConstraints);


        buttonPickSoundFontDirectory.setText("Pick Directory...");
        buttonPickSoundFontDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPickSoundFontDirectoryActionPerformed(evt);
            }
        });

         gridBagConstraints.gridx=2;

        //gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(buttonPickSoundFontDirectory, gridBagConstraints);


        // -- PATCH NAME -------------------------------------------------------------------------------------------------------
        jLabelPatchNameDirectory.setText("PatchNames Directory:");
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy++;

        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(jLabelPatchNameDirectory, gridBagConstraints);

        textfieldPatchNameDirectory.setText("~/frinika/patchname/");
        // gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx=1;

        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        directoriesPanel.add(textfieldPatchNameDirectory, gridBagConstraints);


        buttonPickPatchNameDirectory.setText("Pick Directory...");
        buttonPickPatchNameDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPickDefaultPatchNameActionDirectoryPerformed(evt);
            }
        });

        gridBagConstraints.gridx=2;

        //gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(buttonPickPatchNameDirectory, gridBagConstraints);


        // --- DEFAULT SOUND FONT DIR --------------------------------------------------------------

        jLabelDefaultSoundFont.setText("Default Soundfont:");
        gridBagConstraints.gridx=0;
        gridBagConstraints.gridy++;
        
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(jLabelDefaultSoundFont, gridBagConstraints);

        textfieldDefaultSoundFont.setText("~/frinika/soundfont/8MBGMSFX.SF2");
        // gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx=1;

        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        directoriesPanel.add(textfieldDefaultSoundFont, gridBagConstraints);

        
        
       
        buttonPickDefaultSoundFont.setText("Pick default Soundfont...");
        buttonPickDefaultSoundFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPickDefaultSoundFontActionPerformed(evt);
            }
        });
        
        gridBagConstraints.gridx=2;

        //gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        directoriesPanel.add(buttonPickDefaultSoundFont, gridBagConstraints);

        //------------------------------------------------------------------------------------------------

        
        tabbedPane.addTab("Directories", directoriesPanel);

        add(tabbedPane, java.awt.BorderLayout.CENTER);

    }// </editor-fold>                        

    private void buttonStopOutputDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStopOutputDeviceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonStopOutputDeviceActionPerformed

    private void buttonMeasureLatencyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMeasureLatencyActionPerformed
        FrinikaAudioSystem.latencyMeasureSet();
    }//GEN-LAST:event_buttonMeasureLatencyActionPerformed

    private void buttonAddInputDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddInputDeviceActionPerformed
        // add a new midi in device
        Collection<String> v = FrinikaConfig.getAvailableMidiInDevices();
        String[] a = new String[v.size()];
        int i = 0;
        for (String s : v) {
        	a[i++] = s;
        }
        String inDev = (String) JOptionPane.showInputDialog(null, "Select midi input device", "Input", JOptionPane.INFORMATION_MESSAGE, null, a, a[0]);
        if (inDev == null) {
        	return;
        }
        Vector<String> vv = FrinikaConfig.getMidiInDeviceList();
        vv.add(inDev);
        FrinikaConfig.setMidiInDeviceList(vv);
        refreshMidiInDevicesList();
    }//GEN-LAST:event_buttonAddInputDeviceActionPerformed

    private void buttonRemoveInputDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRemoveInputDeviceActionPerformed
        // remove currently selected midi input device
        String device = listInputDevices.getSelectedValue().toString();
        if (device != null) {
            Vector v = FrinikaConfig.getMidiInDeviceList();
            v.remove(device);
            FrinikaConfig.setMidiInDeviceList(v);
            refreshMidiInDevicesList();
        }
    }//GEN-LAST:event_buttonRemoveInputDeviceActionPerformed

    
    private void buttonPickAudioDirectoryActionPerformed(java.awt.event.ActionEvent evt) {                                                           
        FrinikaConfig.pickDirectory(frame, textfieldAudioDirectory);
    }
    private void buttonPickSoundFontDirectoryActionPerformed(java.awt.event.ActionEvent evt) {
        FrinikaConfig.pickDirectory(frame, textfieldSoundFontDirectory);
    }

    private void buttonPickDefaultSoundFontActionPerformed(java.awt.event.ActionEvent evt) {
        FrinikaConfig.pickFont(frame, textfieldDefaultSoundFont);
    }

     private void buttonPickDefaultPatchNameActionDirectoryPerformed(java.awt.event.ActionEvent evt) {
        FrinikaConfig.pickDirectory(frame, textfieldPatchNameDirectory);
    }


    private void buttonPickScriptsDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPickScriptsDirectoryActionPerformed
        FrinikaConfig.pickDirectory(frame, textfieldScriptsDirectory);
    }//GEN-LAST:event_buttonPickScriptsDirectoryActionPerformed

    private void buttonPickGroovePatternsDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPickGroovePatternsDirectoryActionPerformed
        FrinikaConfig.pickDirectory(frame, textfieldGroovePatternsDirectory);
    }//GEN-LAST:event_buttonPickGroovePatternsDirectoryActionPerformed

    private void buttonPickFontTextLaneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPickFontTextLaneActionPerformed
        FrinikaConfig.pickFont(frame, textfieldFontTextLane);
    }//GEN-LAST:event_buttonPickFontTextLaneActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel audioPanel;
    private javax.swing.JPanel audioPropertiesPanel;
    private javax.swing.JButton buttonAddInputDevice;
    private javax.swing.JButton buttonMeasureLatency;
    private javax.swing.JButton buttonPickFontTextLane;
    javax.swing.JButton buttonPickGroovePatternsDirectory;
    javax.swing.JButton buttonPickScriptsDirectory;
    private javax.swing.JButton buttonRemoveInputDevice;
    private javax.swing.JButton buttonStopOutputDevice;
    javax.swing.JCheckBox checkboxAutoconnectJack;
    javax.swing.JCheckBox checkboxBigEndian;
    javax.swing.JCheckBox checkboxOpenMaximizedWindow;
    javax.swing.JCheckBox checkboxUseDirectMonitoring;
    javax.swing.JCheckBox checkboxUseMultiplexedJavasoundServer;
    javax.swing.JComboBox comboboxBits;
    javax.swing.JComboBox comboboxChannels;
    javax.swing.JComboBox comboboxOutputDevice;
    javax.swing.JComboBox comboboxRedrawRate;
    javax.swing.JComboBox comboboxSampleRate;
    private javax.swing.JPanel directoriesPanel;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel3a;   // PJL
    private javax.swing.JLabel jLabel3b;   // PJL
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JLabel labelBits;
    private javax.swing.JLabel labelChannels;
    private javax.swing.JLabel labelOutputDevice;
    private javax.swing.JLabel labelPriority;
    private javax.swing.JLabel labelRedrawRate;
    private javax.swing.JLabel labelUnderrunTolerance;
    private javax.swing.JList listInputDevices;
    private javax.swing.JPanel midiPanel;
    javax.swing.JSpinner spinnerBufferSize;
    javax.swing.JSpinner spinnerTicksPerQuarter;   // PJL
    javax.swing.JSpinner spinnerSequencerPriority;   // PJL
    javax.swing.JSpinner spinnerMouseDragSpeedSpinners;
    javax.swing.JSpinner spinnerOutputLatency;
    javax.swing.JSpinner spinnerPriority;
    javax.swing.JSpinner spinnerUnderrunTolerance;
    javax.swing.JTabbedPane tabbedPane;
    javax.swing.JTextField textfieldFontTextLane;
    javax.swing.JTextField textfieldGroovePatternsDirectory;
    javax.swing.JTextField textfieldScriptsDirectory;
    private javax.swing.JPanel userInterfacePanel;
    // End of variables declaration//GEN-END:variables
    
}
