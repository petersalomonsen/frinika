/*
 * Created on Dec 3, 2005
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
package com.frinika.contrib.boblang;

/**
 * When adding new versions of BezierParams create a new class instead of
 * modifing the current The new class should implement this interface. The old
 * versions should be modified so that new parameters return a default value.
 *
 * @author Peter Johan Salomonsen
 */
public interface BezierParams {

    /**
     * @return Returns the envAttackTime.
     */
    int getEnvAttackTime();

    /**
     * @param envAttackTime The envAttackTime to set.
     */
    void setEnvAttackTime(int envAttackTime);

    /**
     * @return Returns the envDecayTime.
     */
    int getEnvDecayTime();

    /**
     * @param envDecayTime The envDecayTime to set.
     */
    void setEnvDecayTime(int envDecayTime);

    /**
     * @return Returns the envReleaseTime.
     */
    int getEnvReleaseTime();

    /**
     * @param envReleaseTime The envReleaseTime to set.
     */
    void setEnvReleaseTime(int envReleaseTime);

    /**
     * @return Returns the envSusLevel.
     */
    int getEnvSusLevel();

    /**
     * @param envSusLevel The envSusLevel to set.
     */
    void setEnvSusLevel(int envSusLevel);

    /**
     * @return Returns the lowerAbsFreqX.
     */
    double getLowerAbsFreqX();

    /**
     * @param lowerAbsFreqX The lowerAbsFreqX to set.
     */
    void setLowerAbsFreqX(double lowerAbsFreqX);

    /**
     * @return Returns the lowerAbsFreqY.
     */
    double getLowerAbsFreqY();

    /**
     * @param lowerAbsFreqY The lowerAbsFreqY to set.
     */
    void setLowerAbsFreqY(double lowerAbsFreqY);

    /**
     * @return Returns the lowerAmplX.
     */
    int getLowerAmplX();

    /**
     * @param lowerAmplX The lowerAmplX to set.
     */
    void setLowerAmplX(int lowerAmplX);

    /**
     * @return Returns the lowerAmplY.
     */
    int getLowerAmplY();

    /**
     * @param lowerAmplY The lowerAmplY to set.
     */
    void setLowerAmplY(int lowerAmplY);

    /**
     * @return Returns the lowerCentreX.
     */
    int getLowerCentreX();

    /**
     * @param lowerCentreX The lowerCentreX to set.
     */
    void setLowerCentreX(int lowerCentreX);

    /**
     * @return Returns the lowerCentreY.
     */
    int getLowerCentreY();

    /**
     * @param lowerCentreY The lowerCentreY to set.
     */
    void setLowerCentreY(int lowerCentreY);

    /**
     * @return Returns the lowerEnvX.
     */
    int getLowerEnvX();

    /**
     * @param lowerEnvX The lowerEnvX to set.
     */
    void setLowerEnvX(int lowerEnvX);

    /**
     * @return Returns the lowerEnvY.
     */
    int getLowerEnvY();

    /**
     * @param lowerEnvY The lowerEnvY to set.
     */
    void setLowerEnvY(int lowerEnvY);

    /**
     * @return Returns the lowerPhaseX.
     */
    int getLowerPhaseX();

    /**
     * @param lowerPhaseX The lowerPhaseX to set.
     */
    void setLowerPhaseX(int lowerPhaseX);

    /**
     * @return Returns the lowerPhaseY.
     */
    int getLowerPhaseY();

    /**
     * @param lowerPhaseY The lowerPhaseY to set.
     */
    void setLowerPhaseY(int lowerPhaseY);

    /**
     * @return Returns the lowerRelFreqX.
     */
    double getLowerRelFreqX();

    /**
     * @param lowerRelFreqX The lowerRelFreqX to set.
     */
    void setLowerRelFreqX(double lowerRelFreqX);

    /**
     * @return Returns the lowerRelFreqY.
     */
    double getLowerRelFreqY();

    /**
     * @param lowerRelFreqY The lowerRelFreqY to set.
     */
    void setLowerRelFreqY(double lowerRelFreqY);

    /**
     * @return Returns the lowerXEnvTime.
     */
    int getLowerXEnvTime();

    /**
     * @param lowerXEnvTime The lowerXEnvTime to set.
     */
    void setLowerXEnvTime(int lowerXEnvTime);

    /**
     * @return Returns the lowerYEnvTime.
     */
    int getLowerYEnvTime();

    /**
     * @param lowerYEnvTime The lowerYEnvTime to set.
     */
    void setLowerYEnvTime(int lowerYEnvTime);

    /**
     * @return Returns the upperAbsFreqX.
     */
    double getUpperAbsFreqX();

    /**
     * @param upperAbsFreqX The upperAbsFreqX to set.
     */
    void setUpperAbsFreqX(double upperAbsFreqX);

    /**
     * @return Returns the upperAbsFreqY.
     */
    double getUpperAbsFreqY();

    /**
     * @param upperAbsFreqY The upperAbsFreqY to set.
     */
    void setUpperAbsFreqY(double upperAbsFreqY);

    /**
     * @return Returns the upperAmplX.
     */
    int getUpperAmplX();

    /**
     * @param upperAmplX The upperAmplX to set.
     */
    void setUpperAmplX(int upperAmplX);

    /**
     * @return Returns the upperAmplY.
     */
    int getUpperAmplY();

    /**
     * @param upperAmplY The upperAmplY to set.
     */
    void setUpperAmplY(int upperAmplY);

    /**
     * @return Returns the upperCentreX.
     */
    int getUpperCentreX();

    /**
     * @param upperCentreX The upperCentreX to set.
     */
    void setUpperCentreX(int upperCentreX);

    /**
     * @return Returns the upperCentreY.
     */
    int getUpperCentreY();

    /**
     * @param upperCentreY The upperCentreY to set.
     */
    void setUpperCentreY(int upperCentreY);

    /**
     * @return Returns the upperEnvX.
     */
    int getUpperEnvX();

    /**
     * @param upperEnvX The upperEnvX to set.
     */
    void setUpperEnvX(int upperEnvX);

    /**
     * @return Returns the upperEnvY.
     */
    int getUpperEnvY();

    /**
     * @param upperEnvY The upperEnvY to set.
     */
    void setUpperEnvY(int upperEnvY);

    /**
     * @return Returns the upperPhaseX.
     */
    int getUpperPhaseX();

    /**
     * @param upperPhaseX The upperPhaseX to set.
     */
    void setUpperPhaseX(int upperPhaseX);

    /**
     * @return Returns the upperPhaseY.
     */
    int getUpperPhaseY();

    /**
     * @param upperPhaseY The upperPhaseY to set.
     */
    void setUpperPhaseY(int upperPhaseY);

    /**
     * @return Returns the upperRelFreqX.
     */
    double getUpperRelFreqX();

    /**
     * @param upperRelFreqX The upperRelFreqX to set.
     */
    void setUpperRelFreqX(double upperRelFreqX);

    /**
     * @return Returns the upperRelFreqY.
     */
    double getUpperRelFreqY();

    /**
     * @param upperRelFreqY The upperRelFreqY to set.
     */
    void setUpperRelFreqY(double upperRelFreqY);

    /**
     * @return Returns the upperXEnvTime.
     */
    int getUpperXEnvTime();

    /**
     * @param upperXEnvTime The upperXEnvTime to set.
     */
    void setUpperXEnvTime(int upperXEnvTime);

    /**
     * @return Returns the upperYEnvTime.
     */
    int getUpperYEnvTime();

    /**
     * @param upperYEnvTime The upperYEnvTime to set.
     */
    void setUpperYEnvTime(int upperYEnvTime);

    int getSampleRate();

    void setSampleRate(int sampeRate);

    int wavelengthFromFrequency(double frequency);

    double frequencyFromWavelength(int wavelength);

    int getOutputRate();

    void setOutputRate(int outputRate);

    int getBufferLength();

}
