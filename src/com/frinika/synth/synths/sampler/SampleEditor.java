/*
 * Created on Dec 25, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
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
package com.frinika.synth.synths.sampler;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.frinika.audio.Decibel;
import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SampleEditor extends JFrame{

	SampledSoundSettings sampledSound;
	/**
	 * @param selectedValue
	 */
	public SampleEditor(SampledSoundSettings sampledSound) {
		this.sampledSound = sampledSound;
		
		this.setTitle(sampledSound.toString());
		setLayout(new BorderLayout());
		add(new SampleGraph(sampledSound.getLeftSamples()),BorderLayout.CENTER);
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu editMenu = new JMenu("Edit");
		JMenuItem renameMenuItem = new JMenuItem("Rename");
		renameMenuItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				SampleEditor.this.sampledSound.setSampleName(JOptionPane.showInputDialog("Enter new name",SampleEditor.this.sampledSound.getSampleName()));
			}});
		editMenu.add(renameMenuItem);
		menuBar.add(editMenu);
		
		JMenu processMenu = new JMenu("Process");
		JMenuItem normalizeMenuItem = new JMenuItem("Normalize");
		normalizeMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				short[] leftSamples = SampleEditor.this.sampledSound.getLeftSamples();
				int maxVal = 0;
				for(int n=0;n<leftSamples.length;n++)
					if(Math.abs(leftSamples[n])>maxVal)
						maxVal = Math.abs(leftSamples[n]);
				System.out.println("Normalizing "+maxVal+" "+(32767.0 / maxVal));
				for(int n=0;n<leftSamples.length;n++)
					leftSamples[n]*=(32767.0 / maxVal);
				repaint();
			}
		});
		processMenu.add(normalizeMenuItem);

		JMenuItem plus3dBMenuItem = new JMenuItem("Plus 3 dB");
		plus3dBMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				short[] leftSamples = SampleEditor.this.sampledSound.getLeftSamples();
				System.out.println("multiplying every sample with: "+Decibel.getAmplitudeRatio(3f));
				for(int n=0;n<leftSamples.length;n++)
				{
					int sample =(int)(leftSamples[n]*Decibel.getAmplitudeRatio(3f));
					if(sample>32767)
						sample = 32767;
					if(sample<-32768)
						sample = -32768;
					leftSamples[n] = (short)sample;
				}
				repaint();
			}
		});
		processMenu.add(plus3dBMenuItem);

		JMenuItem minus3dBMenuItem = new JMenuItem("Minus 3 dB");
		minus3dBMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				short[] leftSamples = SampleEditor.this.sampledSound.getLeftSamples();
				for(int n=0;n<leftSamples.length;n++)
					leftSamples[n]*=Decibel.getAmplitudeRatio(-3f);
				repaint();
			}
		});
		processMenu.add(minus3dBMenuItem);

		menuBar.add(processMenu);
		add(menuBar,BorderLayout.NORTH);
		setSize(500,500);
		setVisible(true);
		
	}	
}
