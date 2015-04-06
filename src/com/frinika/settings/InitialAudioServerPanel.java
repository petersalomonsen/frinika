/*
 * Created on Jun 18, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import uk.org.toot.audio.server.ExtendedAudioServer;

import com.frinika.project.FrinikaAudioSystem;
import com.frinika.global.FrinikaConfig;
import com.frinika.toot.javasoundmultiplexed.MultiplexedJavaSoundAudioServer;

public class InitialAudioServerPanel extends JPanel {

	private ExtendedAudioServer audioServer;

	JButton next;
	JPanel buttonpane = new JPanel();
	Color bgCol=Color.white;
	InitialAudioServerPanel() {

		setLayout(new BorderLayout());

		setBackground(bgCol);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final AudioServerSelectPanel multiIO = new AudioServerSelectPanel();
		add(multiIO);
		if (bgCol !=null) multiIO.setBackground(bgCol);

		// multiIO.setOpaque(false);

		// buttonpane.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
		buttonpane.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		if (bgCol !=null) buttonpane.setBackground(bgCol);
		
		// buttonpane.add(new JButton("OK"));
		// buttonpane.add(new JButton("Cancel"));

		buttonpane.add(next = new JButton("Next >"));
		add(buttonpane, BorderLayout.SOUTH);

		next.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				multiIO.done();
				remove(multiIO);
				next.removeActionListener(this);
				
				audioServer = (ExtendedAudioServer) FrinikaAudioSystem
						.getAudioServerInit();

				if (audioServer instanceof MultiplexedJavaSoundAudioServer) {
					setStateMultiPlexedAudioServer();
				} else {
					setStateIO();
				}
			}

		});
	}

	private void setStateIO() {
		final AudioServerIOPanel ioPanel;
		add(ioPanel = new AudioServerIOPanel(audioServer), 0);
		ioPanel.setBackground(bgCol);
		validate();
		repaint();

		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ioPanel.done();
				remove(ioPanel);
				setStateServerConfigured();
				next.removeActionListener(this);
			}
		});
	}

	void setStateServerConfigured() {
		FrinikaAudioSystem.intitIO();
		final JLabel label=new JLabel("Server configured.");
		label.setBackground(bgCol);
		add(label,0);
		next.setText("OK");
		validate();
		repaint();
	
		next.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Container top=getTopLevelAncestor();
				if (getTopLevelAncestor() instanceof SetupDialog ) {
					top.setVisible(false);
				}
			}
			
		});

	}
		
	private void setStateMultiPlexedAudioServer() {
		final MultiPlexAudioServerIOPanel ioPanel;
		add(ioPanel = new MultiPlexAudioServerIOPanel((MultiplexedJavaSoundAudioServer) audioServer), 0);
		//next.setText("Start Audio");
		ioPanel.setBackground(bgCol);
		validate();
		repaint();

		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				next.removeActionListener(this);
				ioPanel.done();
				remove(ioPanel);
				setStateIO();
			}
		});
	}
}

class AudioServerSelectPanel extends JPanel {

	JComboBox cb;
	AudioServerSelectPanel() {
		setLayout(new GridBagLayout());
//		setBackground(Color.WHITE);
		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2, 2, 2, 2);
		// c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridheight = 1;

		c.gridy = 0;
		c.gridx = 0;

		final boolean multiplexIO = FrinikaConfig.MULTIPLEXED_AUDIO;
		//.getPropertyBoolean("multiplexed_audio");

		String opt[] = { "Default Server", "Multiplexed Server" };

		cb = new JComboBox(opt);
		add(new JLabel("Audio Server"));
		c.gridx++;
		add(cb, c);
		
		if (multiplexIO)
			cb.setSelectedIndex(1);
		else
			cb.setSelectedIndex(0);

	}
	
	void done() {
		//FrinikaConfig.setProperty("multiplexed_audio", String
		//		.valueOf(cb.getSelectedIndex() == 1));
		FrinikaConfig.setMultiplexedAudio(cb.getSelectedIndex() == 1);
		FrinikaConfig.store();
	}
	
}

class MultiPlexAudioServerIOPanel extends JPanel {

	final JComboBox devOut;
	final JComboBox devIn;
	final String inConfigStr;
	final String outConfigStr;
	final MultiplexedJavaSoundAudioServer audioServer;
	
	MultiPlexAudioServerIOPanel(MultiplexedJavaSoundAudioServer s) {
		audioServer=((MultiplexedJavaSoundAudioServer) s);
		setLayout(new GridBagLayout());

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2, 2, 2, 2);
		// c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridheight = 1;

		c.gridy = 0;
		c.gridx = 0;

		add(new JLabel("Output Device:"), c);

		devOut = new JComboBox();
		c.gridx++;
		add(devOut, c);

		outConfigStr = s.getConfigKey()
		+ ".outputDevice";
		
		String outStr = FrinikaConfig.getProperty(outConfigStr);

		System.out.println("FrinikaCongigure:" + outConfigStr + "=" + outStr);
		
		List<String> list = audioServer.getOutDeviceList();

		
		for (String str : list) {
			devOut.addItem(str);
			if (str == outStr) {
				devOut.setSelectedItem(str);
			}
		}

		list = ((MultiplexedJavaSoundAudioServer) s).getInDeviceList();
		list.add(0, "NONE");

		c.gridy++;
		c.gridx = 0;
		add(new JLabel("Input Device:"), c);

		devIn = new JComboBox();
		c.gridx++;
		add(devIn, c);

		inConfigStr = s.getConfigKey()
		+ ".inputDevice";
		
		String inStr=FrinikaConfig.getProperty(inConfigStr);
		list = ((MultiplexedJavaSoundAudioServer) s).getInDeviceList();

		list.add("NONE");
		for (String str : list) {
			devIn.addItem(str);
			if (str.equals(inStr))
				devIn.setSelectedItem(str);
		}

		if (devIn.getSelectedItem() == null) devIn.setSelectedIndex(0);
		if (devOut.getSelectedItem() == null) devOut.setSelectedIndex(0);

	}

	void done() {

		audioServer.setOutDevice(devOut.getSelectedItem()
				.toString());
		
		audioServer.setInDevice(devIn.getSelectedItem()
				.toString());

		FrinikaConfig.setProperty(outConfigStr, devOut.getSelectedItem()
				.toString());	

		FrinikaConfig.setProperty(inConfigStr, devIn.getSelectedItem()
				.toString());

		FrinikaConfig.store();
		
	}
}

class AudioServerIOPanel extends JPanel {

	final String outConfigStr;
	final JComboBox devOut;
	
	AudioServerIOPanel(ExtendedAudioServer audioServer) {

		setLayout(new GridBagLayout());

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		// c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(2, 2, 2, 2);
		// c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridheight = 1;

		c.gridy = 0;
		c.gridx = 0;


		add(new JLabel("Output:"), c);

		devOut = new JComboBox();
		c.gridx++;
		add(devOut, c);

		outConfigStr=audioServer.getConfigKey()+ ".output";
		String outStr = FrinikaConfig.getProperty(outConfigStr);
		System.out.println("FrinikaConfigure: " + outConfigStr + "=" + outStr);
		List<String> list = audioServer.getAvailableOutputNames();

		for (String str : list) {
			devOut.addItem(str);
			if (str.equals(outStr))
				devOut.setSelectedItem(str);
		}
		if (devOut.getSelectedItem() == null ) devOut.setSelectedIndex(0);
		
	}
	
	void done() {
		FrinikaConfig.setProperty(outConfigStr, devOut.getSelectedItem()
				.toString());		
		FrinikaConfig.store();
	}
}
