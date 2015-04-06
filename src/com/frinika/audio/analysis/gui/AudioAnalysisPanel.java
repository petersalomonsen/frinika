/*
 * Created on Mar 20, 2007
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

package com.frinika.audio.analysis.gui;


import com.frinika.audio.io.AudioReaderFactory;

import com.frinika.audio.DynamicMixer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

import com.frinika.util.tweaks.gui.TweakerPanel;
import com.frinika.util.tweaks.Tweakable;
import com.frinika.util.tweaks.TweakableDouble;
import com.frinika.audio.analysis.Mapper;
import com.frinika.audio.analysis.SpectrumController;
import com.frinika.audio.analysis.SpectrumDataBuilder;
import com.frinika.audio.analysis.constantq.ConstantQSpectrogramDataBuilder;
import com.frinika.audio.analysis.constantq.ConstantQSpectrumController;

//import com.frinika.sequencer.model.AudioPart;
import com.frinika.audio.io.LimitedAudioReader;


/**
 * 
 * TOp level panel for the audioanalysis GUI
 * 
 * @author pjl
 *
 */


public class AudioAnalysisPanel extends JPanel {

	Vector<Tweakable> tweaks = new Vector<Tweakable>();

	TweakableDouble mindB = new TweakableDouble(tweaks, -400.0, 100.0, -70.0,
			1.0, "minDb");

	TweakableDouble maxdB = new TweakableDouble(tweaks, -400.0, 100.0, -30.0,
			1.0, "maxDb");

	JToggleButton linearBut;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	AudioAnalysisTimePanel timePanel;

	SingleImagePanel spectroSlicePanel;

	LimitedAudioReader reader;
	private ValMapper valMapper;
	private Mapper freqMapper;
	
	
	SpectrumDataBuilder spectroData;
	SpectrumController spectroController;
	JPanel spectroPanel;

	private KeyboardFocusManager kbd;
	
	DynamicMixer mixer;
	

	
	public AudioAnalysisPanel(final AudioReaderFactory part,DynamicMixer mixer,JFrame frame,KeyboardFocusManager kbd) {

        this.mixer=mixer;
		this.kbd=kbd;
		setFocusable(true);
		setLayout(new BorderLayout());
    	try {
			reader = part.createAudioReader();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		valMapper = new ValMapper();
		maxdB.addObserver(valMapper);
		mindB.addObserver(valMapper);

		JMenu menu= new JMenu("Spectral");
			
		JMenuItem item=new JMenuItem("FFT");
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				clear();
				spectroData = new FFTSpectrogramDataBuilderWrapper(reader);
				spectroController=  ((FFTSpectrogramDataBuilderWrapper)spectroData).getController();
			//	new FFTSpectrumController((FFTSpectrogramControlable) spectroData,reader);
				setSpectralView(part);
			}
		});
		
		menu.add(item);
		
		item=new JMenuItem("constant Q");
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				clear();
				spectroData = new ConstantQSpectrogramDataBuilder();
				spectroController=new ConstantQSpectrumController((ConstantQSpectrogramDataBuilder)spectroData,reader);
				setSpectralView(part);
			}
		});
		menu.add(item);
		
		frame.getJMenuBar().add(menu);
	
	}
		
		
	
	void clear() {
		if (spectroData != null) spectroData.dispose();
		if (timePanel != null) timePanel.dispose();
		removeAll();
	}		
		
	void setSpectralView(AudioReaderFactory part) {
		

		freqMapper=spectroController.getFrequencyMapper();
		

			
		
		timePanel = new AudioAnalysisTimePanel(part,mixer, valMapper, spectroData,kbd);

		SpectralSliceImage spectralSliceImage = new SpectralSliceImage(
				spectroData, valMapper, freqMapper , timePanel.getSynth());
		
		
		// Make sure this is the last thing notified 
		spectroData.addSizeObserver(spectralSliceImage);
		timePanel.addCursorObserver(spectralSliceImage);

	

		spectroSlicePanel = new SingleImagePanel(spectralSliceImage);		
		
		add(spectroSlicePanel, BorderLayout.NORTH);

		JScrollPane scroll = new JScrollPane(timePanel);
		add(scroll, BorderLayout.CENTER);
		setPreferredSize(new Dimension(1000, 400));

		JPanel buts  = new JPanel();
		buts.setLayout(new BoxLayout(buts, BoxLayout.Y_AXIS));
		
		// Log linear switch 
		linearBut = new JToggleButton("Log10");
        // linearBut.setSelected(valMapper.linear);
		linearBut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (!linearBut.isSelected()) {
					linearBut.setText("Log10");
				} else {
					linearBut.setText("Linear");
				}
				valMapper.update(null, null);
			}
		});
		buts.add(linearBut);
		
		// Switch to static synth
		final JToggleButton synthBut = new JToggleButton("Synth Mode");
		synthBut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				timePanel.setSynthMode(synthBut.isSelected());
			}
		});
		buts.add(synthBut);
		

		
		TweakerPanel tpanel = new TweakerPanel(2, 4);
	
		
		for (Tweakable t : tweaks) {
			tpanel.addSpinTweaker(t);
		}
		


		JPanel control= new JPanel();

		control.add(buts);
		control.add(tpanel);
		control.add(spectroController.getTweakPanel());
		
		
		add(control, BorderLayout.SOUTH);
	

		valMapper.update(null, null);	
		
		
		spectroController.update();
		revalidate();
		repaint();
	}


	
 	
	public void dispose() {
		timePanel.dispose();
	}



	final class ValMapper implements Observer, Mapper {

		double maxdb;

		double mindb;

		double max;

		double min;

		boolean linear=true;

		private Thread thread;

		public final float eval(float val) {
			if (linear) {
				float vv = (float) ((val - min) / (max - min));
				return (float)Math.max(0.0,vv);
			} else {
				double dB = 20 * Math.log10(val + 1e-15);
				float vv = (float) ((dB - mindb) / (maxdb - mindb));
				return (float) Math.max(0.0, vv);
			}
		}

		public void update(Observable o, Object arg) {

			linear = ! linearBut.isSelected();

			maxdb = maxdB.doubleValue();
			max = Math.pow(10, maxdb / 20.0);

			mindb = mindB.doubleValue();
			min = Math.pow(10, mindb / 20.0);

			if (thread != null)
				thread.interrupt();
			thread = new Thread(new Runnable() {
				public void run() {
					spectroSlicePanel.repaint();
					timePanel.spectroImage.update(null, null);
					thread = null;
				}
			});
			thread.start();
		}
	}

	
	
}
