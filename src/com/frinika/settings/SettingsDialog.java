/*
 * Created on 20.5.2007
 *
 * Copyright (c) 2006-2007 Karl Helgason
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

package com.frinika.settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;

public class SettingsDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static void main(String[] args)
	{
		new SettingsDialog().setVisible(true);
	}
	
	private static SettingsDialog instance = null;
	
	public static void showSettings()
	{
		if(instance == null) instance = new SettingsDialog();
		instance.setAlwaysOnTop(true);
		instance.setModal(false);
		instance.setVisible(true);
	}
	
	public static void showSettingsModal()
	{
		if(instance == null) instance = new SettingsDialog();
		instance.setAlwaysOnTop(false);
		instance.setModal(true);
		instance.setVisible(true);	

	}
	
	public SettingsDialog()
	{
		super();
		setTitle("Settings");		
						
		JPanel contentpane = new JPanel();
		contentpane.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		contentpane.setLayout(new BorderLayout());
				
		JPanel audiogroup = new JPanel();
		audiogroup.setBorder(BorderFactory.createTitledBorder("Audio"));
		audiogroup.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));
		
		JPanel groupcontent = new JPanel();
		audiogroup.add(groupcontent);
		groupcontent.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		groupcontent.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();		
		c.anchor = GridBagConstraints.WEST;
		//c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2,2,2,2);
		//c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridheight = 1;
		
		c.gridy = 0;
		c.gridx = 0;
		groupcontent.add(new JLabel("Output Device:"),c);
		String[] devices = {"Java Sound Device"};
		
		c.gridx = 1;
		groupcontent.add(new JComboBox(devices),c);
				
		
		
		
		c.gridy = 1;
		c.gridx = 0;
		groupcontent.add(new JLabel("Sample Rate:"),c);
		
		JComboBox ratebox = new JComboBox(new String[] {"48000", "44100", "22050"});
		ratebox.setEditable(true);
		c.gridx = 1;
		groupcontent.add(ratebox,c);
		
		c.gridy = 2;		
		c.gridx = 0;		
		groupcontent.add(new JLabel("Channels:"),c);
		c.gridx = 1;		
		groupcontent.add(new JComboBox(new String[] {"2 Stereo", "1 Mono"}),c);
		
		c.gridy = 3;		
		c.gridx = 0;		
		groupcontent.add(new JLabel("Bits:"),c);
		c.gridx = 1;		
		groupcontent.add(new JComboBox(new String[] {"16", "24"}),c);
		

		c.gridy = 0;		
		c.gridx = 2;
		c.gridheight = 4;
		c.fill = GridBagConstraints.BOTH;
		JPanel sep = new JPanel();
		sep.setMinimumSize(new Dimension(10,10));
		sep.setPreferredSize(new Dimension(10,10));
		sep.setMaximumSize(new Dimension(10,10));
		groupcontent.add(sep,c);
		c.fill = GridBagConstraints.NONE;
		c.gridheight = 1;
		
		c.gridy = 0;		
		c.gridx = 3;		
		groupcontent.add(new JButton("Stop"),c);
	
		c.gridy = 1;		
		c.gridx = 3;		
		groupcontent.add(new JLabel("Buffer Size:"),c);
		
		c.gridx = 4;		
		groupcontent.add(new JTextField("100"),c);	
		
		c.gridx = 5;		
		groupcontent.add(new JLabel("msec"),c);
		
		
		c.gridy = 2;		
		c.gridx = 3;		
		groupcontent.add(new JLabel("Underrun Tolerance:"),c);
		c.gridx = 4;		
		groupcontent.add(new JSpinner(),c);			
		
		c.gridy = 3;		
		c.gridx = 3;		
		groupcontent.add(new JLabel("Priority:"),c);
		c.gridx = 4;		
		groupcontent.add(new JSpinner(),c);				
		
						

		c.gridy = 4;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(7,2,2,2);
		
		JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);		
		sep.setMinimumSize(new Dimension(5,5));
		sep.setPreferredSize(new Dimension(5,5));
		groupcontent.add(sep2,c);
		
		c.insets = new Insets(2,2,2,2);		
		c.fill = GridBagConstraints.NONE;
		
		c.gridwidth = 1;
		c.gridy = 5;
		c.gridx = 0;
		groupcontent.add(new JLabel("Output Latency (samples)"),c);		
		c.gridx = 1;
		groupcontent.add(new JSpinner(),c);		
		c.gridx = 3;
		groupcontent.add(new JButton("Measure latency..."),c);		
		
		c.gridy = 6;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(7,2,2,2);
		
		sep2 = new JSeparator(JSeparator.HORIZONTAL);		
		sep.setMinimumSize(new Dimension(5,5));
		sep.setPreferredSize(new Dimension(5,5));
		groupcontent.add(sep2,c);
		
		c.insets = new Insets(2,2,2,2);		
		c.fill = GridBagConstraints.NONE;
		
		
		c.gridy = 7;		
		groupcontent.add(new JCheckBox("Use Direct Monitoring"),c);
		c.gridy = 8;		
		groupcontent.add(new JCheckBox("Use Multiplexed Javasound Server (Requires Restart)"),c);
		c.gridy = 9;		
		groupcontent.add(new JCheckBox("Autoconnect Jack (Requires Restart)"),c);
		
		JPanel midigroup = new JPanel();
		midigroup.setBorder(BorderFactory.createTitledBorder("MIDI"));
		midigroup.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));
		
		groupcontent = new JPanel();
		midigroup.add(groupcontent);
		groupcontent.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		groupcontent.setLayout(new GridBagLayout());
		c = new GridBagConstraints();		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2,2,2,2);
		
		c.gridwidth = 1;
		groupcontent.add(new JLabel("Input Devices :"),c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		String[] midiindevs = {"Virtual Keyboard", "MIDI Port 1"};
		JList midiindev_list = new JList(midiindevs);
		JScrollPane midiindev_list_s = new JScrollPane(midiindev_list);
		Dimension p = midiindev_list_s.getPreferredSize();
		p.height = 70;
		p.width = 200;
		midiindev_list_s.setPreferredSize(p);
		groupcontent.add(midiindev_list_s,c);
		
		JPanel midiindev_butonpanel = new JPanel();
		midiindev_butonpanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,0));
		midiindev_butonpanel.add(new JButton("Add"));
		midiindev_butonpanel.add(new JButton("Remove"));
		
		c.gridx = 1;
		groupcontent.add(midiindev_butonpanel,c);
		
		JPanel miscgroup = new JPanel();
		miscgroup.setBorder(BorderFactory.createTitledBorder("User Interface"));
		miscgroup.setLayout(new FlowLayout(FlowLayout.LEFT, 0,0));
		
		groupcontent = new JPanel();
		miscgroup.add(groupcontent);
		groupcontent.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		groupcontent.setLayout(new GridBagLayout());
		c = new GridBagConstraints();		
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2,2,2,2);
		
		c.gridwidth = 1;
		groupcontent.add(new JLabel("Redraw Rate (jumps):"),c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		String[] guivalues = {"Disable when playing", "1"};
		groupcontent.add(new JComboBox(guivalues),c);
		
		
		JPanel grouppane = new JPanel();
		grouppane.setLayout(new BoxLayout(grouppane, BoxLayout.Y_AXIS));
		grouppane.add(audiogroup);
		grouppane.add(midigroup);		
		grouppane.add(miscgroup);
		
		contentpane.add(grouppane);
		
		JPanel buttonpane = new JPanel();
		//buttonpane.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		buttonpane.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
		buttonpane.add(new JButton("OK"));
		buttonpane.add(new JButton("Cancel"));
		buttonpane.add(new JButton("Apply"));
		contentpane.add(buttonpane, BorderLayout.SOUTH);
		
		setContentPane(contentpane);
		
		//setSize(500, 400);
		pack();
		
	    Rectangle windowSize ;
	    Insets windowInsets;		
		
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();	    
	    if(gc == null) 
	        gc = getGraphicsConfiguration();	    
	    
	    if(gc != null) {
	    	windowSize = gc.getBounds();
	    } else {
	    	windowSize = new java.awt.Rectangle(toolkit.getScreenSize());
	    }	   		
		
		Dimension size = getSize();		
		Point parent_loc = getLocation();			
		setLocation(parent_loc.x + windowSize.width/2 - (size.width/2),
				    parent_loc.y + windowSize.height/2 - (size.height/2));
		
	}
	
}
