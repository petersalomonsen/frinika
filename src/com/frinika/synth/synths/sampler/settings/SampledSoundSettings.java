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
 *
 */
public interface SampledSoundSettings {
        public short getAttack();
        public void setAttack(short attack);
        public int getFineTune();
        public void setFineTune(int fineTune);
        public short[] getLeftSamples();
        public void setLeftSamples(short[] leftSamples);
        public int getLoopEnd();
        public void setLoopEnd(int loopEnd);
        public int getLoopStart();
        public void setLoopStart(int loopStart);
        public int getPitchCorrection();
        public void setPitchCorrection(int pitchCorrection);
        public short getRelease();
        public void setRelease(short release);
        public short[] getRightSamples();
        public void setRightSamples(short[] rightSamples);
        public int getRootKey();
        public void setRootKey(int rootKey);
        public int getSampleMode();
        public void setSampleMode(int sampleMode);
        public String getSampleName();
        public void setSampleName(String sampleName);
        public int getSampleRate();
        public void setSampleRate(int sampleRate);
        public int getScaleTune();
        public void setScaleTune(int scaleTune);
        public String toString();
        public void setExclusiveClass(int exclusiveClass);
        public int getExclusiveClass();       
}
