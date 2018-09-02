/*
 * Created on Feb 27, 2005
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
package com.petersalomonsen.mystudio.mysynth.synths;

import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;
import com.frinika.synth.synths.sampler.settings.SamplerSettings;
import com.petersalomonsen.mystudio.mysynth.SampledSound;
import java.io.Serializable;

/**
 * @author Peter Johan Salomonsen
 *
 */
class SFSynthSettings implements SamplerSettings, Serializable {

    private static final long serialVersionUID = 1L;
    public String soundFontName;
    public int instrumentIndex;
    public String instrumentName;
    public SampledSound[][] sampledSounds;

    @Override
    public String getSoundFontName() {
        return soundFontName;
    }

    @Override
    public void setSoundFontName(String soundFontName) {
        this.soundFontName = soundFontName;

    }

    @Override
    public int getInstrumentIndex() {
        return instrumentIndex;
    }

    @Override
    public void setInstrumentIndex(int instrumentIndex) {
        this.instrumentIndex = instrumentIndex;
    }

    @Override
    public String getInstrumentName() {
        return instrumentName;
    }

    @Override
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    @Override
    public SampledSoundSettings[][] getSampledSounds() {
        return (SampledSoundSettings[][]) sampledSounds;
    }

    @Override
    public void setSampledSounds(SampledSoundSettings[][] sampledSounds) {
        this.sampledSounds = (SampledSound[][]) sampledSounds;
    }

    @Override
    public int getLayers() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setLayers(int layers) {
        // TODO Auto-generated method stub
    }

    @Override
    public float getFreqSpread() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFreqSpread(float freqSpread) {
        // TODO Auto-generated method stub
    }
}
