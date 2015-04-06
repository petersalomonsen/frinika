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

import com.frinika.global.FrinikaConfig;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.Timer;

import com.frinika.util.tweaks.gui.TweakerPanel;
import com.frinika.util.tweaks.Tweakable;
import com.frinika.util.tweaks.TweakableDouble;

import com.frinika.audio.analysis.CycliclyBufferedAudio;
import com.frinika.audio.analysis.Mapper;
import com.frinika.audio.analysis.dft.CyclicBufferFFTSpectrogramDataBuilder;

import uk.org.toot.audio.core.AudioProcess;


/**
 * 
 * TOp level panel for the audioanalysis GUI
 * 
 * @author pjl
 * 
 */

public class CyclicBufferFFTAnalysisPanel extends JPanel {

	Vector<Tweakable> tweaks = new Vector<Tweakable>();

	TweakableDouble mindB = new TweakableDouble(tweaks, -400.0, 100.0, -40.0,
			5.0, "minDb");

	TweakableDouble maxdB = new TweakableDouble(tweaks, -400.0, 100.0, -50.0,
			5.0, "maxDb");

	JToggleButton linearBut;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// AudioAnalysisTimePanel timePanel;

	// SingleImagePanel spectroSlicePanel;

	AudioProcess reader;

	private ValMapper valMapper;

	// private Mapper freqMapper;

	CyclicSpectrogramImage image;

	CyclicBufferFFTSpectrogramDataBuilder spectroData;

	// SpectrumController spectroController;
	JPanel spectroPanel;

	// ' private KeyboardFocusManager kbd;

	FFTOption fftOpts[] = { new FFTOption(512, 256), new FFTOption(512, 128),
			new FFTOption(1024, 512), new FFTOption(1024, 256),
			new FFTOption(1024, 128), new FFTOption(2048, 1024),
			new FFTOption(2048, 512), new FFTOption(2048, 256) };

	private float maxFreq;

	public CyclicBufferFFTAnalysisPanel(final CycliclyBufferedAudio bp) {

		setLayout(new BorderLayout());

        valMapper = new ValMapper();
		maxdB.addObserver(valMapper);
		mindB.addObserver(valMapper);


		spectroData = new CyclicBufferFFTSpectrogramDataBuilder(bp.out, 1000,FrinikaConfig.sampleRate);
		// spectroController=new
		// FFTSpectrumController((FFTSpectrogramControlable) spectroData);
		spectroData.setParameters(fftOpts[0].chunkSize, fftOpts[0].fftSize,bp.getSampleRate());
		
		final JComboBox combo = new JComboBox(fftOpts);

		combo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				final FFTOption opt = (FFTOption) combo.getSelectedItem();
				System.err.println(" xxxxxxxxxxxxxx ");
				new Thread(new Runnable() {
					public void run() {
						spectroData.setParameters(opt.chunkSize, opt.fftSize,bp.getSampleRate());
                        revalidate();
					}
				}).start();

			}

		});

    	image = new CyclicSpectrogramImage(valMapper,800);

       // JLabel p=new JLabel(" HELKP ME WORK ");
        JScrollPane sp=new JScrollPane(image);

		spectroData.addSizeObserver(image);

		add(sp, BorderLayout.CENTER);

		JPanel buts = new JPanel();
		buts.setLayout(new BoxLayout(buts, BoxLayout.Y_AXIS));

		// Log linear switch
		linearBut = new JToggleButton("linear");
		linearBut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (!linearBut.isSelected()) {
					linearBut.setText("Linear");
				} else {
					linearBut.setText("Log10");
				}
				valMapper.update(null, null);
			}
		});
		buts.add(linearBut);

		// Switch to static synth
		final JToggleButton synthBut = new JToggleButton("Synth Mode");
		synthBut.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				// timePanel.setSynthMode(synthBut.isSelected());
			}
		});
		buts.add(synthBut);

		TweakerPanel tpanel = new TweakerPanel(2, 4);

		for (Tweakable t : tweaks) {
			tpanel.addSpinTweaker(t);
		}

		JPanel control = new JPanel();

		control.add(buts);
		control.add(tpanel);
		control.add(combo); // spectroController.getTweakPanel());

		add(control, BorderLayout.SOUTH);

		valMapper.update(null, null);

		// spectroController.update();
		revalidate();
		repaint();

		final JLabel label=new JLabel("            ");;
		control.add(label);
		new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				label.setText(String.valueOf(bp.getLag()));
			}

		}).start();
	}

	// public void dispose() {
	// timePanel.dispose();
	// }

	final class ValMapper implements Observer, Mapper {

		double maxdb;

		double mindb;

		double max;

		double min;

		boolean linear;

		private Thread thread;

		public final float eval(float val) {
			if (linear) {
				float vv = (float) ((val - min) / (max - min));
				return vv;
			} else {
				double dB = 20 * Math.log10(val + 1e-15);
				float vv = (float) ((dB - mindb) / (maxdb - mindb));
				return vv;
			}
		}

		public void update(Observable o, Object arg) {

			linear = linearBut.isSelected();

			maxdb = maxdB.doubleValue();
			max = Math.pow(10, maxdb / 20.0);

			mindb = mindB.doubleValue();
			min = Math.pow(10, mindb / 20.0);

			repaint();
			// if (thread != null)
			// thread.interrupt();

			// thread = new Thread(new Runnable() {
			// public void run() {
			// spectroSlicePanel.repaint();
			// timePanel.spectroImage.update(null, null);
			// thread = null;
			// }
			// });
			// thread.start();
		}
	}

}

class FFTOption {
	int fftSize;

	int chunkSize;

	public FFTOption(int i, int j) {
		fftSize = i;
		chunkSize = j;
	}

	public String toString() {
		return fftSize + "/" + chunkSize;
	}
}