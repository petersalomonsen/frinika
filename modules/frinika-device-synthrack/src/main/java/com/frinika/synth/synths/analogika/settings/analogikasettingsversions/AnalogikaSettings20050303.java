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
package com.frinika.synth.synths.analogika.settings.analogikasettingsversions;

import com.frinika.synth.synths.analogika.settings.AnalogikaSettings;
import java.io.Serializable;

public class AnalogikaSettings20050303 implements AnalogikaSettings, Serializable {

    private static final long serialVersionUID = 1L;

    float[] waveform = new float[(int) (2 * Math.PI * 10000)];

    int layers = 5;
    float freqSpread = 0.003f;

    int volAttack = 0;
    int volDecay = 0;
    int volSustain = 0;
    int volRelease = 0;

    int loPassAttack = 0;
    int loPassDecay = 0;
    int loPassSustain = 0;
    int loPassRelease = 0;
    int loPassMax = 0;

    String instrumentName;

    public AnalogikaSettings20050303() {
        for (float n = 0; n < waveform.length; n++) {
            //waveform[(int)n] = (float)Math.sin((n / (float)waveform.length) * Math.PI * 2.0); 
            waveform[(int) n] = (float) (n / (float) waveform.length) - 0.5f;
        }
    }

    /**
     * @return Returns the freqSpread.
     */
    @Override
    public float getFreqSpread() {
        return freqSpread;
    }

    /**
     * @param freqSpread The freqSpread to set.
     */
    @Override
    public void setFreqSpread(float freqSpread) {
        this.freqSpread = freqSpread;
    }

    /**
     * @return Returns the layers.
     */
    @Override
    public int getLayers() {
        return layers;
    }

    /**
     * @param layers The layers to set.
     */
    @Override
    public void setLayers(int layers) {
        this.layers = layers;
    }

    /**
     * @return Returns the loPassAttack.
     */
    @Override
    public int getLoPassAttack() {
        return loPassAttack;
    }

    /**
     * @param loPassAttack The loPassAttack to set.
     */
    @Override
    public void setLoPassAttack(int loPassAttack) {
        this.loPassAttack = loPassAttack;
    }

    /**
     * @return Returns the loPassDecay.
     */
    @Override
    public int getLoPassDecay() {
        return loPassDecay;
    }

    /**
     * @param loPassDecay The loPassDecay to set.
     */
    @Override
    public void setLoPassDecay(int loPassDecay) {
        this.loPassDecay = loPassDecay;
    }

    /**
     * @return Returns the loPassMax.
     */
    @Override
    public int getLoPassMax() {
        return loPassMax;
    }

    /**
     * @param loPassMax The loPassMax to set.
     */
    @Override
    public void setLoPassMax(int loPassMax) {
        this.loPassMax = loPassMax;
    }

    /**
     * @return Returns the loPassRelease.
     */
    @Override
    public int getLoPassRelease() {
        return loPassRelease;
    }

    /**
     * @param loPassRelease The loPassRelease to set.
     */
    @Override
    public void setLoPassRelease(int loPassRelease) {
        this.loPassRelease = loPassRelease;
    }

    /**
     * @return Returns the loPassSustain.
     */
    @Override
    public int getLoPassSustain() {
        return loPassSustain;
    }

    /**
     * @param loPassSustain The loPassSustain to set.
     */
    @Override
    public void setLoPassSustain(int loPassSustain) {
        this.loPassSustain = loPassSustain;
    }

    /**
     * @return Returns the volAttack.
     */
    @Override
    public int getVolAttack() {
        return volAttack;
    }

    /**
     * @param volAttack The volAttack to set.
     */
    @Override
    public void setVolAttack(int volAttack) {
        this.volAttack = volAttack;
    }

    /**
     * @return Returns the volDecay.
     */
    @Override
    public int getVolDecay() {
        return volDecay;
    }

    /**
     * @param volDecay The volDecay to set.
     */
    @Override
    public void setVolDecay(int volDecay) {
        this.volDecay = volDecay;
    }

    /**
     * @return Returns the volRelease.
     */
    @Override
    public int getVolRelease() {
        return volRelease;
    }

    /**
     * @param volRelease The volRelease to set.
     */
    @Override
    public void setVolRelease(int volRelease) {
        this.volRelease = volRelease;
    }

    /**
     * @return Returns the volSustain.
     */
    @Override
    public int getVolSustain() {
        return volSustain;
    }

    /**
     * @param volSustain The volSustain to set.
     */
    @Override
    public void setVolSustain(int volSustain) {
        this.volSustain = volSustain;
    }

    /**
     * @return Returns the waveform.
     */
    @Override
    public float[] getWaveform() {
        return waveform;
    }

    /**
     * @param waveform The waveform to set.
     */
    @Override
    public void setWaveform(float[] waveform) {
        this.waveform = waveform;
    }

    @Override
    public String getInstrumentName() {
        return instrumentName;
    }

    @Override
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }
}
