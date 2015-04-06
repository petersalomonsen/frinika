/*
 * Created on Nov 1, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (mystudio@petersalomonsen.com)
 * 
 * http://www.petersalomonsen.com/mystudio
 * 
 * This file is part of MyStudio.
 * 
 * MyStudio is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * MyStudio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyStudio; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.petersalomonsen.mystudio.mysynth;

import java.io.Serializable;

import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SampledSound implements SampledSoundSettings,Serializable {
	private static final long serialVersionUID = 1L;
	
	public short[] leftSamples;
	public short[] rightSamples;
	
	public int loopStart;
	public int loopEnd;
	public int sampleMode;
	
	public static final int SAMPLEMODE_NO_LOOP = 0;
	public static final int SAMPLEMODE_LOOP_CONTINOUSLY = 1;
	public static final int SAMPLEMODE_LOOP_UNTIL_RELEASE = 3;
	
	public short attack;	// timecents
	public short release; 	// timecents
	
	public int rootKey;
	public int pitchCorrection;
	public int fineTune;
	public int sampleRate;
	
	public String sampleName;
	
	public String toString()
	{
		return(sampleName);
	}
	
    /**
     * @return Returns the attack.
     */
    public short getAttack() {
        return attack;
    }
    /**
     * @param attack The attack to set.
     */
    public void setAttack(short attack) {
        this.attack = attack;
    }
    /**
     * @return Returns the fineTune.
     */
    public int getFineTune() {
        return fineTune;
    }
    /**
     * @param fineTune The fineTune to set.
     */
    public void setFineTune(int fineTune) {
        this.fineTune = fineTune;
    }
    /**
     * @return Returns the leftSamples.
     */
    public short[] getLeftSamples() {
        return leftSamples;
    }
    /**
     * @param leftSamples The leftSamples to set.
     */
    public void setLeftSamples(short[] leftSamples) {
        this.leftSamples = leftSamples;
    }
    /**
     * @return Returns the loopEnd.
     */
    public int getLoopEnd() {
        return loopEnd;
    }
    /**
     * @param loopEnd The loopEnd to set.
     */
    public void setLoopEnd(int loopEnd) {
        this.loopEnd = loopEnd;
    }
    /**
     * @return Returns the loopStart.
     */
    public int getLoopStart() {
        return loopStart;
    }
    /**
     * @param loopStart The loopStart to set.
     */
    public void setLoopStart(int loopStart) {
        this.loopStart = loopStart;
    }
    /**
     * @return Returns the pitchCorrection.
     */
    public int getPitchCorrection() {
        return pitchCorrection;
    }
    /**
     * @param pitchCorrection The pitchCorrection to set.
     */
    public void setPitchCorrection(int pitchCorrection) {
        this.pitchCorrection = pitchCorrection;
    }
    /**
     * @return Returns the release.
     */
    public short getRelease() {
        return release;
    }
    /**
     * @param release The release to set.
     */
    public void setRelease(short release) {
        this.release = release;
    }
    /**
     * @return Returns the rightSamples.
     */
    public short[] getRightSamples() {
        return rightSamples;
    }
    /**
     * @param rightSamples The rightSamples to set.
     */
    public void setRightSamples(short[] rightSamples) {
        this.rightSamples = rightSamples;
    }
    /**
     * @return Returns the rootKey.
     */
    public int getRootKey() {
        return rootKey;
    }
    /**
     * @param rootKey The rootKey to set.
     */
    public void setRootKey(int rootKey) {
        this.rootKey = rootKey;
    }
    /**
     * @return Returns the sampleMode.
     */
    public int getSampleMode() {
        return sampleMode;
    }
    /**
     * @param sampleMode The sampleMode to set.
     */
    public void setSampleMode(int sampleMode) {
        this.sampleMode = sampleMode;
    }
    /**
     * @return Returns the sampleName.
     */
    public String getSampleName() {
        return sampleName;
    }
    /**
     * @param sampleName The sampleName to set.
     */
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }
    /**
     * @return Returns the sampleRate.
     */
    public int getSampleRate() {
        return sampleRate;
    }
    /**
     * @param sampleRate The sampleRate to set.
     */
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getScaleTune() {
        return 100;
    }

    public void setScaleTune(int scaleTune) {
    }

    public void setExclusiveClass(int exclusiveClass) {
    }

    public int getExclusiveClass() {
        return 0;
    }
}
