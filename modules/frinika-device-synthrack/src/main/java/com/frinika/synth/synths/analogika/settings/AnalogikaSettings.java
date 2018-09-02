/*
 * Created on Mar 3, 2005
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth.synths.analogika.settings;

import com.frinika.synth.soundbank.SynthRackInstrumentIF;

public interface AnalogikaSettings extends SynthRackInstrumentIF {

    float[] getWaveform();

    void setWaveform(float[] waveform);

    int getLayers();

    void setLayers(int layers);

    float getFreqSpread();

    void setFreqSpread(float freqSpread);

    int getVolAttack();

    void setVolAttack(int volAttack);

    int getVolDecay();

    void setVolDecay(int volDecay);

    int getVolRelease();

    void setVolRelease(int volRelease);

    int getVolSustain();

    void setVolSustain(int volSustain);

    int getLoPassAttack();

    void setLoPassAttack(int loPassAttack);

    int getLoPassDecay();

    void setLoPassDecay(int loPassDecay);

    int getLoPassMax();

    void setLoPassMax(int loPassMax);

    int getLoPassRelease();

    void setLoPassRelease(int loPassRelease);

    int getLoPassSustain();

    void setLoPassSustain(int loPassSustain);
}
