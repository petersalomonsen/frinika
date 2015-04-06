/*
 * Created on 21 Dec 2007
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

package com.frinika.audio.analysis.dft;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JPanel;


import com.frinika.util.tweaks.gui.TweakerPanel;
import com.frinika.util.tweaks.Tweakable;
import com.frinika.util.tweaks.TweakableInt;
import com.frinika.audio.io.LimitedAudioReader;
import com.frinika.audio.analysis.Mapper;
import com.frinika.audio.analysis.SpectrumController;


public class FFTSpectrumController implements SpectrumController {

	Vector<Tweakable> tweaks = new Vector<Tweakable>();

	
	TweakableInt fftsizeT = new TweakableInt(tweaks, 16 , 2048, 512,
			"FFT size");

	TweakableInt chunksizeT = new TweakableInt(tweaks, 8, 2048, 256," chunksize");


	private Mapper freqMapper;
	
	double maxFreq;
	
	Observer reco;
	public FFTSpectrumController(final FFTSpectrogramControlable spectroData,final LimitedAudioReader reader) {
		maxFreq=spectroData.getSampleRate()/2;
		
		freqMapper = new Mapper() {
			
			public final float eval(float val) {
				return (float) (val/maxFreq);
			}
			
		};
		
		
		
		reco = new Observer() {
			public void update(Observable o, Object arg) {
				maxFreq=spectroData.getSampleRate()/2;
				spectroData.setParameters(chunksizeT.intValue(),
						fftsizeT.intValue(),reader);
			}
		};

		fftsizeT.addObserver(reco);
		chunksizeT.addObserver(reco);
	}

	public Mapper getFrequencyMapper() {
		return freqMapper;
	}

	
	public void update() {
		reco.update(null, null);		
	}

	public JPanel getTweakPanel() {
		
		TweakerPanel tpanel = new TweakerPanel(2, 4);
		for (Tweakable t : tweaks) {
			tpanel.addSpinTweaker(t);
		}	
		return tpanel;
	}

}
