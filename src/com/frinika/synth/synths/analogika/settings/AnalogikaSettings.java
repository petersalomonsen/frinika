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
    public float[] getWaveform();
    public void setWaveform(float[] waveform);

    public int getLayers();
    public void setLayers(int layers);
    
    public float getFreqSpread();
    public void setFreqSpread(float freqSpread);

    public int getVolAttack();
    public void setVolAttack(int volAttack);
    
    public int getVolDecay();
    public void setVolDecay(int volDecay);
    
    public int getVolRelease();
    public void setVolRelease(int volRelease);
    
    public int getVolSustain();
    public void setVolSustain(int volSustain);

    public int getLoPassAttack();
    public void setLoPassAttack(int loPassAttack);
    
    public int getLoPassDecay();
    public void setLoPassDecay(int loPassDecay);
    
    public int getLoPassMax();
    public void setLoPassMax(int loPassMax);
    
    public int getLoPassRelease();
    public void setLoPassRelease(int loPassRelease) ;
    
    public int getLoPassSustain() ;
    public void setLoPassSustain(int loPassSustain);
    
}
