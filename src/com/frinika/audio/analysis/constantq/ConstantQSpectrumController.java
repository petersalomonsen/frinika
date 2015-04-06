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
package com.frinika.audio.analysis.constantq;

import com.frinika.audio.io.LimitedAudioReader;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JPanel;


import com.frinika.util.tweaks.gui.TweakerPanel;
import com.frinika.util.tweaks.Tweakable;
import com.frinika.util.tweaks.TweakableDouble;
import com.frinika.util.tweaks.TweakableInt;
import com.frinika.audio.analysis.Mapper;
import com.frinika.audio.analysis.SpectrumController;

public class ConstantQSpectrumController implements SpectrumController {

    Vector<Tweakable> tweaks = new Vector<Tweakable>();
    TweakableDouble minFreqT = new TweakableDouble(tweaks, 1.0, 1000.0, 55.0,
            1.0, "min Freq");
    TweakableDouble maxFreqT = new TweakableDouble(tweaks, 2.0, 20000.0,
            6000.0, 5.0, "max Freq");
    TweakableInt binsPerOctave = new TweakableInt(tweaks, 1, 64, 48,
            "bins/Octave");
    TweakableDouble threshold = new TweakableDouble(tweaks, 0.0, 0.1, .01, .05,
            "threshold");
    TweakableDouble dt = new TweakableDouble(tweaks, 0.001, 0.02, .01, .001,
            "dt");
    TweakableDouble spread = new TweakableDouble(tweaks, 0.1, 50.0, 1.0, .001,
            "spread");

    class FreqMapper implements Mapper {

        double minFreq = minFreqT.doubleValue();
        double maxFreq = maxFreqT.doubleValue();

        public final float eval(float val) {
            double x = Math.log(val / minFreq);
            double mx = Math.log(maxFreq / minFreq);
            return (float) (x / mx);
        }

        void update() {
            minFreq = minFreqT.doubleValue();
            maxFreq = maxFreqT.doubleValue();
        }
    }
    FreqMapper freqMapper;
    Observer reco;

    public ConstantQSpectrumController(final ConstantQSpectrogramDataBuilder spectroData, final LimitedAudioReader reader) {


        freqMapper = new FreqMapper();


        reco = new Observer() {

            public void update(Observable o, Object arg) {
                freqMapper.update();
                spectroData.setParameters(reader, minFreqT.doubleValue(),
                        maxFreqT.doubleValue(), binsPerOctave.intValue(),
                        threshold.doubleValue(), spread.doubleValue(), dt.doubleValue());
            }
        };

        binsPerOctave.addObserver(reco);
        maxFreqT.addObserver(reco);
        minFreqT.addObserver(reco);
        threshold.addObserver(reco);
        dt.addObserver(reco);
        spread.addObserver(reco);
    }

    public Mapper getFrequencyMapper() {
        // TODO Auto-generated method stub
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
