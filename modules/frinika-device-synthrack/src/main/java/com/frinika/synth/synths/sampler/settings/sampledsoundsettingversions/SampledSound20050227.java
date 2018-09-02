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
package com.frinika.synth.synths.sampler.settings.sampledsoundsettingversions;

import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;
import java.io.Serializable;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SampledSound20050227 implements SampledSoundSettings, Serializable {

    private static final long serialVersionUID = 1L;

    private short[] leftSamples;
    private short[] rightSamples;

    private int loopStart;
    private int loopEnd;
    private int sampleMode;

    private short attack;    // timecents
    private short release;   // timecents

    private int rootKey;
    private int pitchCorrection;
    private int fineTune;
    private int sampleRate;

    private String sampleName;

    /**
     * @return Returns the attack.
     */
    @Override
    public short getAttack() {
        return attack;
    }

    /**
     * @param attack The attack to set.
     */
    @Override
    public void setAttack(short attack) {
        this.attack = attack;
    }

    /**
     * @return Returns the fineTune.
     */
    @Override
    public int getFineTune() {
        return fineTune;
    }

    /**
     * @param fineTune The fineTune to set.
     */
    @Override
    public void setFineTune(int fineTune) {
        this.fineTune = fineTune;
    }

    /**
     * @return Returns the leftSamples.
     */
    @Override
    public short[] getLeftSamples() {
        return leftSamples;
    }

    /**
     * @param leftSamples The leftSamples to set.
     */
    @Override
    public void setLeftSamples(short[] leftSamples) {
        this.leftSamples = leftSamples;
    }

    /**
     * @return Returns the loopEnd.
     */
    @Override
    public int getLoopEnd() {
        return loopEnd;
    }

    /**
     * @param loopEnd The loopEnd to set.
     */
    @Override
    public void setLoopEnd(int loopEnd) {
        this.loopEnd = loopEnd;
    }

    /**
     * @return Returns the loopStart.
     */
    @Override
    public int getLoopStart() {
        return loopStart;
    }

    /**
     * @param loopStart The loopStart to set.
     */
    @Override
    public void setLoopStart(int loopStart) {
        this.loopStart = loopStart;
    }

    /**
     * @return Returns the pitchCorrection.
     */
    @Override
    public int getPitchCorrection() {
        return pitchCorrection;
    }

    /**
     * @param pitchCorrection The pitchCorrection to set.
     */
    @Override
    public void setPitchCorrection(int pitchCorrection) {
        this.pitchCorrection = pitchCorrection;
    }

    /**
     * @return Returns the release.
     */
    @Override
    public short getRelease() {
        return release;
    }

    /**
     * @param release The release to set.
     */
    @Override
    public void setRelease(short release) {
        this.release = release;
    }

    /**
     * @return Returns the rightSamples.
     */
    @Override
    public short[] getRightSamples() {
        return rightSamples;
    }

    /**
     * @param rightSamples The rightSamples to set.
     */
    @Override
    public void setRightSamples(short[] rightSamples) {
        this.rightSamples = rightSamples;
    }

    /**
     * @return Returns the rootKey.
     */
    @Override
    public int getRootKey() {
        return rootKey;
    }

    /**
     * @param rootKey The rootKey to set.
     */
    @Override
    public void setRootKey(int rootKey) {
        this.rootKey = rootKey;
    }

    /**
     * @return Returns the sampleMode.
     */
    @Override
    public int getSampleMode() {
        return sampleMode;
    }

    /**
     * @param sampleMode The sampleMode to set.
     */
    @Override
    public void setSampleMode(int sampleMode) {
        this.sampleMode = sampleMode;
    }

    /**
     * @return Returns the sampleName.
     */
    @Override
    public String getSampleName() {
        return sampleName;
    }

    /**
     * @param sampleName The sampleName to set.
     */
    @Override
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    /**
     * @return Returns the sampleRate.
     */
    @Override
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * @param sampleRate The sampleRate to set.
     */
    @Override
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public String toString() {
        return (sampleName);
    }

    @Override
    public int getScaleTune() {
        return 100;
    }

    @Override
    public void setScaleTune(int scaleTune) {
    }

    @Override
    public void setExclusiveClass(int exclusiveClass) {
    }

    @Override
    public int getExclusiveClass() {
        return 0;
    }
}
