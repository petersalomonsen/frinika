/**
 * Copyright (c) 2005 - Bob Lang (http://www.cems.uwe.ac.uk/~lrlang/)
 *
 * http://www.frinika.com
 *
 * This file is part of Frinika.
 *
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.contrib.boblang;
// Source file generated by UWE GUI BUILDER Version 1.6 beta  Free-Ware

// File Pass 1...(Class file creation)
//import uwejava.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.midi.*;
import javax.swing.*;

public class BezierSetup extends JDialog implements ActionListener, WindowListener, ItemListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
// **** Global data accepted from the gui *******************************
    public int dgMidiInputNumber,
            dgSampleRate,
            dgPolyphony,
            dgBufferLength;

    private void getDataFromGui() {
        String s;
        dgMidiInputNumber = midiInput.getSelectedIndex();
        s = sampleRate.getSelectedItem().toString();
        dgSampleRate = Convert.stringToInt(s, 48000);
        s = polyphonyBox.getSelectedItem().toString();
        dgPolyphony = Convert.stringToInt(s, 8);
        s = bufferSizeBox.getSelectedItem().toString();
        dgBufferLength = Convert.stringToInt(s, 128);
    } // getDataFromGui ()

    // **********************************************************************
    // Listener objects
    ActionListener actionListener = this;
    WindowListener windowListener = this;
    ItemListener itemListener = this;
    JComboBox midiInput = new JComboBox(midiInputChoices);
    static String[] midiInputChoices = {
        "Undefined MIDI Input",};
    JComboBox sampleRate = new JComboBox(sampleRateChoices);
    static String[] sampleRateChoices = {
        "48000",
        "44100",
        "24000",
        "22050",};
    JComboBox polyphonyBox = new JComboBox(polyphonyBoxChoices);
    static String[] polyphonyBoxChoices = {
        "2",
        "4",
        "6",
        "8",
        "12",
        "16",
        "20",
        "24",};
    JComboBox bufferSizeBox = new JComboBox(bufferSizeBoxChoices);
    static String[] bufferSizeBoxChoices = {
        "1024",
        "2048",
        "4096",
        "6144",
        "8192",
        "10240",
        "16384",};

    // Components for Panel combinedPanel
    JPanel combinedPanel = new JPanel();
    JLabel label1 = new JLabel("Midi Input", JLabel.CENTER);
    JLabel label2 = new JLabel("Sample Rate", JLabel.CENTER);
    JLabel label3 = new JLabel("Write Buffer Length", JLabel.CENTER);
    JLabel label4 = new JLabel("Max Polyphony", JLabel.CENTER);

    // Components for Panel controlPanel
    JPanel controlPanel = new JPanel();
    JButton accept = new JButton("Accept");
    JButton quit = new JButton("Quit");
    JLabel label5 = new JLabel("Bezier Synthesizer Setup Options", JLabel.CENTER);
// File Pass 2...

    // Public constructor
    public BezierSetup(Frame parent, String title, boolean modal) {
        // Call parent constructor to give title to frame
        super(parent, title, modal);

        // Default write buffer size
        bufferSizeBox.setSelectedIndex(3);

        // **** Create the midi input combo box here ******************************
        try {
            MidiDevice.Info[] info = MidiSystem.getMidiDeviceInfo();
            String[] deviceList = new String[info.length];
            for (int i = 0; i < info.length; i++) {
                deviceList[i] = "  " + i + ". " + info[i];
            } // for

            // Create the combo box
            midiInput = new JComboBox(deviceList);
        } // try
        catch (Exception e) {
            String m = "BezierSetup reports exception\n";
            m += e.toString();
            JOptionPane.showMessageDialog(null,
                    m,
                    null,
                    JOptionPane.ERROR_MESSAGE);
            System.out.println("BezierSetup reports exception");
            System.out.println("Problem accessing midi input devices");
            System.out.println(e);
            System.exit(0);
        }

        // ************************************************************************
        // Make this frame its own window listener
        addWindowListener(windowListener);
        midiInput.addItemListener(itemListener);
        sampleRate.addItemListener(itemListener);
        polyphonyBox.addItemListener(itemListener);
        bufferSizeBox.addItemListener(itemListener);

        // Panel components: combinedPanel
        GridBagLayout combinedPanelGridBag = new GridBagLayout();
        GridBagConstraints combinedPanelConstr = new GridBagConstraints();
        combinedPanel.setLayout(combinedPanelGridBag);
        combinedPanelConstr.anchor = GridBagConstraints.CENTER;
        combinedPanelConstr.weightx = 1.0;
        combinedPanelConstr.weighty = 1.0;
        combinedPanelConstr.fill = GridBagConstraints.BOTH;
        combinedPanelConstr.gridx = 0;
        combinedPanelConstr.gridy = 0;
        combinedPanelConstr.gridwidth = 1;
        combinedPanelConstr.gridheight = 1;
        combinedPanelGridBag.setConstraints(label1, combinedPanelConstr);
        combinedPanel.add(label1);
        combinedPanelConstr.gridx = 1;
        combinedPanelConstr.gridy = 0;
        combinedPanelConstr.gridwidth = 2;
        combinedPanelConstr.gridheight = 1;
        combinedPanelGridBag.setConstraints(midiInput, combinedPanelConstr);
        combinedPanel.add(midiInput);
        combinedPanelConstr.gridx = 0;
        combinedPanelConstr.gridy = 1;
        combinedPanelConstr.gridwidth = 1;
        combinedPanelConstr.gridheight = 1;
        combinedPanelGridBag.setConstraints(label2, combinedPanelConstr);
        combinedPanel.add(label2);
        combinedPanelConstr.gridx = 1;
        combinedPanelConstr.gridy = 1;
        combinedPanelConstr.gridwidth = 1;
        combinedPanelConstr.gridheight = 1;
        combinedPanelGridBag.setConstraints(sampleRate, combinedPanelConstr);
        combinedPanel.add(sampleRate);
        combinedPanelConstr.gridx = 0;
        combinedPanelConstr.gridy = 2;
        combinedPanelConstr.gridwidth = 1;
        combinedPanelConstr.gridheight = 1;
        combinedPanelGridBag.setConstraints(label3, combinedPanelConstr);
        combinedPanel.add(label3);
        combinedPanelConstr.gridx = 1;
        combinedPanelConstr.gridy = 2;
        combinedPanelConstr.gridwidth = 1;
        combinedPanelConstr.gridheight = 1;
        combinedPanelGridBag.setConstraints(bufferSizeBox, combinedPanelConstr);
        combinedPanel.add(bufferSizeBox);
        combinedPanelConstr.gridx = 0;
        combinedPanelConstr.gridy = 3;
        combinedPanelConstr.gridwidth = 1;
        combinedPanelConstr.gridheight = 1;
        combinedPanelGridBag.setConstraints(label4, combinedPanelConstr);
        combinedPanel.add(label4);
        combinedPanelConstr.gridx = 1;
        combinedPanelConstr.gridy = 3;
        combinedPanelConstr.gridwidth = 1;
        combinedPanelConstr.gridheight = 1;
        combinedPanelGridBag.setConstraints(polyphonyBox, combinedPanelConstr);
        combinedPanel.add(polyphonyBox);

        // Panel components: controlPanel
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        controlPanel.add(accept);
        accept.addActionListener(actionListener);
        controlPanel.add(quit);
        quit.addActionListener(actionListener);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add("North", label5);
        this.getContentPane().add("Center", combinedPanel);
        this.getContentPane().add("South", controlPanel);

        // Set dialogue size and show it
        setSize(500, 250);
        setVisible(true);
    } // Dialogue constructor BezierSetup ()

//File Pass 3...(Window and Action Listeners)
    // Window listener interface methods
    @Override
    public void windowActivated(WindowEvent e) {
        //$ System.out.println ("Window activated");
    } // windowActivated ()

    @Override
    public void windowClosed(WindowEvent e) {
        //$ System.out.println ("Window closed");
    } // windowClosed ()

    @Override
    public void windowClosing(WindowEvent e) {
        //$ System.out.println ("Window closing");
        setVisible(false);
    } // windowClosing ()

    @Override
    public void windowDeactivated(WindowEvent e) {
        //$ System.out.println ("Window deactivated");
    } // windowDeactivated ()

    @Override
    public void windowDeiconified(WindowEvent e) {
        //$ System.out.println ("Window deiconified");
    } // windowDeiconified ()

    @Override
    public void windowIconified(WindowEvent e) {
        //$ System.out.println ("Window iconified");
    } // windowIconifed ()

    @Override
    public void windowOpened(WindowEvent e) {
        //$ System.out.println ("Window opened");
    } // windowOpened ()

    // Action Listener interface method
    @Override
    public void actionPerformed(ActionEvent event) {
        Object target = event.getSource();
        if (target == accept) {
            //$ System.out.println ("AE1 JButton accept selected");
            getDataFromGui();
            setVisible(false);
        } else if (target == quit) {
            //$ System.out.println ("AE2 JButton quit selected");
            System.exit(0);
        } else {
        } // if
    } // actionPerformed ()
//File Pass 4...(Document Listener)
//File Pass 5...(Item Listener)

    // Item Listener interface method
    @Override
    public void itemStateChanged(ItemEvent event) {
        Object target = event.getSource();
        if (target == midiInput) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                //$ System.out.print ("IS1 Item midiInput changed to: ");
                //$ System.out.print (midiInput.getSelectedIndex () + "  ");
                //$ System.out.println (midiInput.getSelectedItem ());
            }
        } else if (target == sampleRate) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                //$ System.out.print ("IS2 Item sampleRate changed to: ");
                //$ System.out.print (sampleRate.getSelectedIndex () + "  ");
                //$ System.out.println (sampleRate.getSelectedItem ());
            }
        } else if (target == polyphonyBox) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                //$ System.out.print ("IS3 Item polyphonyBox changed to: ");
                //$ System.out.print (polyphonyBox.getSelectedIndex () + "  ");
                //$ System.out.println (polyphonyBox.getSelectedItem ());
            }
        } else if (target == bufferSizeBox) {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                //$ System.out.print ("IS4 Item bufferSizeBox changed to: ");
                //$ System.out.print (bufferSizeBox.getSelectedIndex () + "  ");
                //$ System.out.println (bufferSizeBox.getSelectedItem ());
            }
        } else {
        } // if
    } // itemStateChanged ()
//File Pass 6...(List Selection Listener)
//File Pass 7...(Adjustment Listener)
//File Pass 8...(Change Listener)

    // Main method
    public static void main(String[] args) {
        // Create a dummy parent
        Frame f = new Frame("DummyParent");
        f.setSize(200, 100);
        f.setVisible(true);
        // Create the test dialogue and make it visible
        BezierSetup d = new BezierSetup(f, "BezierSetup", true);
        d.setVisible(true);
        // Exit when dialogue returns
        System.exit(0);
    } // main () method
} // Dialog BezierSetup
