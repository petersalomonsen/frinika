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
 * When adding new versions of BezierParams create a new class instead of modifing the current
 * The new class should implement this interface. The old versions should be modified so that new
 * parameters return a default value. 
 * 
 * @author Peter Johan Salomonsen
 */
public interface BezierParams {
    /**
     * @return Returns the envAttackTime.
     */
    public int getEnvAttackTime() ;
    
    /**
     * @param envAttackTime The envAttackTime to set.
     */
    public void setEnvAttackTime(int envAttackTime) ;
    
    /**
     * @return Returns the envDecayTime.
     */
    public int getEnvDecayTime() ;
    
    /**
     * @param envDecayTime The envDecayTime to set.
     */
    public void setEnvDecayTime(int envDecayTime);
    
    /**
     * @return Returns the envReleaseTime.
     */
    public int getEnvReleaseTime() ;
    
    /**
     * @param envReleaseTime The envReleaseTime to set.
     */
    public void setEnvReleaseTime(int envReleaseTime) ;
    
    /**
     * @return Returns the envSusLevel.
     */
    public int getEnvSusLevel() ;
    
    /**
     * @param envSusLevel The envSusLevel to set.
     */
    public void setEnvSusLevel(int envSusLevel) ;
    
    /**
     * @return Returns the lowerAbsFreqX.
     */
    public double getLowerAbsFreqX() ;
    
    /**
     * @param lowerAbsFreqX The lowerAbsFreqX to set.
     */
    public void setLowerAbsFreqX(double lowerAbsFreqX) ;
    
    /**
     * @return Returns the lowerAbsFreqY.
     */
    public double getLowerAbsFreqY() ;
    
    /**
     * @param lowerAbsFreqY The lowerAbsFreqY to set.
     */
    public void setLowerAbsFreqY(double lowerAbsFreqY) ;
    
    /**
     * @return Returns the lowerAmplX.
     */
    public int getLowerAmplX() ;
    
    /**
     * @param lowerAmplX The lowerAmplX to set.
     */
    public void setLowerAmplX(int lowerAmplX) ;
    
    /**
     * @return Returns the lowerAmplY.
     */
    public int getLowerAmplY() ;
    
    /**
     * @param lowerAmplY The lowerAmplY to set.
     */
    public void setLowerAmplY(int lowerAmplY) ;
    
    /**
     * @return Returns the lowerCentreX.
     */
    public int getLowerCentreX() ;
    
    /**
     * @param lowerCentreX The lowerCentreX to set.
     */
    public void setLowerCentreX(int lowerCentreX) ;
    
    /**
     * @return Returns the lowerCentreY.
     */
    public int getLowerCentreY() ;
    
    /**
     * @param lowerCentreY The lowerCentreY to set.
     */
    public void setLowerCentreY(int lowerCentreY) ;
    
    /**
     * @return Returns the lowerEnvX.
     */
    public int getLowerEnvX() ;
    
    /**
     * @param lowerEnvX The lowerEnvX to set.
     */
    public void setLowerEnvX(int lowerEnvX) ;
    
    /**
     * @return Returns the lowerEnvY.
     */
    public int getLowerEnvY() ;
    
    /**
     * @param lowerEnvY The lowerEnvY to set.
     */
    public void setLowerEnvY(int lowerEnvY) ;
    
    /**
     * @return Returns the lowerPhaseX.
     */
    public int getLowerPhaseX() ;
    
    /**
     * @param lowerPhaseX The lowerPhaseX to set.
     */
    public void setLowerPhaseX(int lowerPhaseX) ;
    
    /**
     * @return Returns the lowerPhaseY.
     */
    public int getLowerPhaseY() ;
    
    /**
     * @param lowerPhaseY The lowerPhaseY to set.
     */
    public void setLowerPhaseY(int lowerPhaseY) ;
    
    /**
     * @return Returns the lowerRelFreqX.
     */
    public double getLowerRelFreqX() ;
    
    /**
     * @param lowerRelFreqX The lowerRelFreqX to set.
     */
    public void setLowerRelFreqX(double lowerRelFreqX) ;
    
    /**
     * @return Returns the lowerRelFreqY.
     */
    public double getLowerRelFreqY() ;
    
    /**
     * @param lowerRelFreqY The lowerRelFreqY to set.
     */
    public void setLowerRelFreqY(double lowerRelFreqY) ;
    
    /**
     * @return Returns the lowerXEnvTime.
     */
    public int getLowerXEnvTime() ;
    
    /**
     * @param lowerXEnvTime The lowerXEnvTime to set.
     */
    public void setLowerXEnvTime(int lowerXEnvTime) ;
    
    /**
     * @return Returns the lowerYEnvTime.
     */
    public int getLowerYEnvTime() ;
    
    /**
     * @param lowerYEnvTime The lowerYEnvTime to set.
     */
    public void setLowerYEnvTime(int lowerYEnvTime) ;
    
    /**
     * @return Returns the upperAbsFreqX.
     */
    public double getUpperAbsFreqX() ;
    
    /**
     * @param upperAbsFreqX The upperAbsFreqX to set.
     */
    public void setUpperAbsFreqX(double upperAbsFreqX) ;
    
    /**
     * @return Returns the upperAbsFreqY.
     */
    public double getUpperAbsFreqY() ;
    
    /**
     * @param upperAbsFreqY The upperAbsFreqY to set.
     */
    public void setUpperAbsFreqY(double upperAbsFreqY) ;
    
    /**
     * @return Returns the upperAmplX.
     */
    public int getUpperAmplX() ;
    
    /**
     * @param upperAmplX The upperAmplX to set.
     */
    public void setUpperAmplX(int upperAmplX) ;
    
    /**
     * @return Returns the upperAmplY.
     */
    public int getUpperAmplY() ;
    
    /**
     * @param upperAmplY The upperAmplY to set.
     */
    public void setUpperAmplY(int upperAmplY) ;
    
    /**
     * @return Returns the upperCentreX.
     */
    public int getUpperCentreX() ;
    
    /**
     * @param upperCentreX The upperCentreX to set.
     */
    public void setUpperCentreX(int upperCentreX) ;
    
    /**
     * @return Returns the upperCentreY.
     */
    public int getUpperCentreY() ;
    
    /**
     * @param upperCentreY The upperCentreY to set.
     */
    public void setUpperCentreY(int upperCentreY) ;
    
    /**
     * @return Returns the upperEnvX.
     */
    public int getUpperEnvX() ;
    
    /**
     * @param upperEnvX The upperEnvX to set.
     */
    public void setUpperEnvX(int upperEnvX) ;
    
    /**
     * @return Returns the upperEnvY.
     */
    public int getUpperEnvY() ;
    
    /**
     * @param upperEnvY The upperEnvY to set.
     */
    public void setUpperEnvY(int upperEnvY) ;
    
    /**
     * @return Returns the upperPhaseX.
     */
    public int getUpperPhaseX() ;
    
    /**
     * @param upperPhaseX The upperPhaseX to set.
     */
    public void setUpperPhaseX(int upperPhaseX) ;
    
    /**
     * @return Returns the upperPhaseY.
     */
    public int getUpperPhaseY() ;
    
    /**
     * @param upperPhaseY The upperPhaseY to set.
     */
    public void setUpperPhaseY(int upperPhaseY) ;
    
    /**
     * @return Returns the upperRelFreqX.
     */
    public double getUpperRelFreqX() ;
    
    /**
     * @param upperRelFreqX The upperRelFreqX to set.
     */
    public void setUpperRelFreqX(double upperRelFreqX) ;
    
    /**
     * @return Returns the upperRelFreqY.
     */
    public double getUpperRelFreqY() ;
    
    /**
     * @param upperRelFreqY The upperRelFreqY to set.
     */
    public void setUpperRelFreqY(double upperRelFreqY) ;
    
    /**
     * @return Returns the upperXEnvTime.
     */
    public int getUpperXEnvTime() ;
    
    /**
     * @param upperXEnvTime The upperXEnvTime to set.
     */
    public void setUpperXEnvTime(int upperXEnvTime) ;
    
    /**
     * @return Returns the upperYEnvTime.
     */
    public int getUpperYEnvTime() ;
    
    /**
     * @param upperYEnvTime The upperYEnvTime to set.
     */
    public void setUpperYEnvTime(int upperYEnvTime) ;

    public int getSampleRate();
    public void setSampleRate(int sampeRate);
    
    public int wavelengthFromFrequency (double frequency) ;
    public double frequencyFromWavelength (int wavelength);

    public int getOutputRate();
    public void setOutputRate(int outputRate);
    
    public int getBufferLength();
    
}
