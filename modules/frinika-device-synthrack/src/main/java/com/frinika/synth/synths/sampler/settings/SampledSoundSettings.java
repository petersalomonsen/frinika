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
package com.frinika.synth.synths.sampler.settings;

/**
 * @author Peter Johan Salomonsen
 */
public interface SampledSoundSettings {

    short getAttack();

    void setAttack(short attack);

    int getFineTune();

    void setFineTune(int fineTune);

    short[] getLeftSamples();

    void setLeftSamples(short[] leftSamples);

    int getLoopEnd();

    void setLoopEnd(int loopEnd);

    int getLoopStart();

    void setLoopStart(int loopStart);

    int getPitchCorrection();

    void setPitchCorrection(int pitchCorrection);

    short getRelease();

    void setRelease(short release);

    short[] getRightSamples();

    void setRightSamples(short[] rightSamples);

    int getRootKey();

    void setRootKey(int rootKey);

    int getSampleMode();

    void setSampleMode(int sampleMode);

    String getSampleName();

    void setSampleName(String sampleName);

    int getSampleRate();

    void setSampleRate(int sampleRate);

    int getScaleTune();

    void setScaleTune(int scaleTune);

    @Override
    String toString();

    void setExclusiveClass(int exclusiveClass);

    int getExclusiveClass();
}
