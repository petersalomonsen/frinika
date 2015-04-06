/** 
 * Copyright (c) 2005 - Bob Lang (http://www.cems.uwe.ac.uk/~lrlang/)
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
  Bezier Parameters 3.5

 * When adding new versions of BezierParams create a new class instead of modifing the current
 * The new class should implement this interface. The old versions should be modified so that new
 * parameters return a default value. 

  @author Bob Lang
  @author Peter Salomonsen
  @version 21 May 2004
*/
public class BezierParamsV3_5 implements BezierParams,java.io.Serializable {
    private static final long serialVersionUID = 1L;

// Useful constants
  public static final int
    MAX_AMPLITUDE = 8000,       // Maximum amplitude of wave form
    MAX_PITCH     = 128,        // Maximum midi pitch number
    HIGH_PITCH    = 96,         // Midi number of highest working pitch
    LOW_PITCH     = 33;         // Midi number of lowest working pitch

  // Amplitude envelope types
  public static final int
    AMP_STEADY    = 0,          // Steady amplitude
    AMP_RISE_FALL = 1,          // Amplitude rises and then falls
    AMP_RISING    = 2,          // Amplitude rises to maximum
    AMP_FALLING   = 3;          // Amplitude falls from maximum to zero

  // Internal and output sampling rates.  These *must* be linked by
  // a whole integer multiplier
  private int
    sampleRate,                 // Internal sampling rate
    outputRate;                 // Output sampling rate

  // Java output write buffer size.  A larger buffer reduces the possibility
  // of glitches, but increases the latency
  private int
    bufferLength;

  // X Centre positions are stored as a percentage of the wave width
  // Y Centre positions are stored as a percentage of the max amplitude
  private int
    upperCentreX,               // X centre position of upper control point
    upperCentreY,               // Y centre position of upper control point
    lowerCentreX,               // X centre position of lower control point
    lowerCentreY;               // Y centre position of lower control point

  // Amplitudes are stored in the same units as the centre positions
  private int
    upperAmplX,                 // Max X amplitude of upper control point
    upperAmplY,                 // Max Y amplitude of upper control point
    lowerAmplX,                 // Max X amplitude of lower control point
    lowerAmplY;                 // Max Y amplitude of lower control point

  // Amplitude envelopes - default is steady
  private int
    upperEnvX,                  // Upper X amplitude envelope type
    upperEnvY,                  // Upper Y amplitude envelope type
    lowerEnvX,                  // Lower X amplitude envelope type
    lowerEnvY;                  // Lower Y amplitude envelope type

  // Amplitude envelope times in millisecs
  private int
    upperXEnvTime,              // Upper point X amplitude envelope
    upperYEnvTime,              // Upper point Y amplitude envelope
    lowerXEnvTime,              // Lower point X amplitude envelope
    lowerYEnvTime;              // Lower point Y amplitude envelope

  // Absolute frequency component in Hz
  private double
    upperAbsFreqX,              // SHM X frequency of upper control point
    upperAbsFreqY,              // SHM Y frequency of upper control point
    lowerAbsFreqX,              // SHM X frequency of lower control point
    lowerAbsFreqY;              // SHM Y frequency of lower control point

  // Relative frequency component as fraction of fundamental
  private double
    upperRelFreqX,              // SHM X frequency of upper control point
    upperRelFreqY,              // SHM Y frequency of upper control point
    lowerRelFreqX,              // SHM X frequency of lower control point
    lowerRelFreqY;              // SHM Y frequency of lower control point

  // Starting phase for frequency components - all in degrees
  private int
    upperPhaseX,                // Phase variation of upper control point
    upperPhaseY,                // Phase variation of upper control point
    lowerPhaseX,                // Phase variation of lower control point
    lowerPhaseY;                // Phase variation of lower control point

  // Envelope shaper parameters - most parameters in millisecs
  private int
    envAttackTime,              // Attack time in mSecs
    envDecayTime,               // Decay time in mSecs
    envSusLevel,                // Sustain level as % of max amplitude
    envReleaseTime;             // Release time in mSecs


  
  /**
    Set the output sampling rate and calculate the internal working rate
  */
  public void setOutputRate (int inOutputRate, int overSamplingRate) {
    // Set the sampling and output rates
    outputRate = inOutputRate;
    sampleRate = inOutputRate * overSamplingRate;
  }

  /**
    Obtain the wavelength in samples from the frequency
  */
  public int wavelengthFromFrequency (double frequency) {
    return (int) (sampleRate/frequency);
  } // wavelengthFromFrequency ()

  /**
    Obtain the corrected frequency, from the integer wavelength
  */
  public double frequencyFromWavelength (int wavelength) {
    return (double) sampleRate/(double) wavelength;
  } // frequencyFromWavelength ()

  // Bezier parameter modifiers

     /**
     * @return Returns the envAttackTime.
     */
    public int getEnvAttackTime() {
        return envAttackTime;
    }
    
    /**
     * @param envAttackTime The envAttackTime to set.
     */
    public void setEnvAttackTime(int envAttackTime) {
        this.envAttackTime = envAttackTime;
    }
    
    /**
     * @return Returns the envDecayTime.
     */
    public int getEnvDecayTime() {
        return envDecayTime;
    }
    
    /**
     * @param envDecayTime The envDecayTime to set.
     */
    public void setEnvDecayTime(int envDecayTime) {
        this.envDecayTime = envDecayTime;
    }
    
    /**
     * @return Returns the envReleaseTime.
     */
    public int getEnvReleaseTime() {
        return envReleaseTime;
    }
    
    /**
     * @param envReleaseTime The envReleaseTime to set.
     */
    public void setEnvReleaseTime(int envReleaseTime) {
        this.envReleaseTime = envReleaseTime;
    }
    
    /**
     * @return Returns the envSusLevel.
     */
    public int getEnvSusLevel() {
        return envSusLevel;
    }
    
    /**
     * @param envSusLevel The envSusLevel to set.
     */
    public void setEnvSusLevel(int envSusLevel) {
        this.envSusLevel = envSusLevel;
    }
    
    /**
     * @return Returns the lowerAbsFreqX.
     */
    public double getLowerAbsFreqX() {
        return lowerAbsFreqX;
    }
    
    /**
     * @param lowerAbsFreqX The lowerAbsFreqX to set.
     */
    public void setLowerAbsFreqX(double lowerAbsFreqX) {
        this.lowerAbsFreqX = lowerAbsFreqX;
    }
    
    /**
     * @return Returns the lowerAbsFreqY.
     */
    public double getLowerAbsFreqY() {
        return lowerAbsFreqY;
    }
    
    /**
     * @param lowerAbsFreqY The lowerAbsFreqY to set.
     */
    public void setLowerAbsFreqY(double lowerAbsFreqY) {
        this.lowerAbsFreqY = lowerAbsFreqY;
    }
    
    /**
     * @return Returns the lowerAmplX.
     */
    public int getLowerAmplX() {
        return lowerAmplX;
    }
    
    /**
     * @param lowerAmplX The lowerAmplX to set.
     */
    public void setLowerAmplX(int lowerAmplX) {
        this.lowerAmplX = lowerAmplX;
    }
    
    /**
     * @return Returns the lowerAmplY.
     */
    public int getLowerAmplY() {
        return lowerAmplY;
    }
    
    /**
     * @param lowerAmplY The lowerAmplY to set.
     */
    public void setLowerAmplY(int lowerAmplY) {
        this.lowerAmplY = lowerAmplY;
    }
    
    /**
     * @return Returns the lowerCentreX.
     */
    public int getLowerCentreX() {
        return lowerCentreX;
    }
    
    /**
     * @param lowerCentreX The lowerCentreX to set.
     */
    public void setLowerCentreX(int lowerCentreX) {
        this.lowerCentreX = lowerCentreX;
    }
    
    /**
     * @return Returns the lowerCentreY.
     */
    public int getLowerCentreY() {
        return lowerCentreY;
    }
    
    /**
     * @param lowerCentreY The lowerCentreY to set.
     */
    public void setLowerCentreY(int lowerCentreY) {
        this.lowerCentreY = lowerCentreY;
    }
    
    /**
     * @return Returns the lowerEnvX.
     */
    public int getLowerEnvX() {
        return lowerEnvX;
    }
    
    /**
     * @param lowerEnvX The lowerEnvX to set.
     */
    public void setLowerEnvX(int lowerEnvX) {
        this.lowerEnvX = lowerEnvX;
    }
    
    /**
     * @return Returns the lowerEnvY.
     */
    public int getLowerEnvY() {
        return lowerEnvY;
    }
    
    /**
     * @param lowerEnvY The lowerEnvY to set.
     */
    public void setLowerEnvY(int lowerEnvY) {
        this.lowerEnvY = lowerEnvY;
    }
    
    /**
     * @return Returns the lowerPhaseX.
     */
    public int getLowerPhaseX() {
        return lowerPhaseX;
    }
    
    /**
     * @param lowerPhaseX The lowerPhaseX to set.
     */
    public void setLowerPhaseX(int lowerPhaseX) {
        this.lowerPhaseX = lowerPhaseX;
    }
    
    /**
     * @return Returns the lowerPhaseY.
     */
    public int getLowerPhaseY() {
        return lowerPhaseY;
    }
    
    /**
     * @param lowerPhaseY The lowerPhaseY to set.
     */
    public void setLowerPhaseY(int lowerPhaseY) {
        this.lowerPhaseY = lowerPhaseY;
    }
    
    /**
     * @return Returns the lowerRelFreqX.
     */
    public double getLowerRelFreqX() {
        return lowerRelFreqX;
    }
    
    /**
     * @param lowerRelFreqX The lowerRelFreqX to set.
     */
    public void setLowerRelFreqX(double lowerRelFreqX) {
        this.lowerRelFreqX = lowerRelFreqX;
    }
    
    /**
     * @return Returns the lowerRelFreqY.
     */
    public double getLowerRelFreqY() {
        return lowerRelFreqY;
    }
    
    /**
     * @param lowerRelFreqY The lowerRelFreqY to set.
     */
    public void setLowerRelFreqY(double lowerRelFreqY) {
        this.lowerRelFreqY = lowerRelFreqY;
    }
    
    /**
     * @return Returns the lowerXEnvTime.
     */
    public int getLowerXEnvTime() {
        return lowerXEnvTime;
    }
    
    /**
     * @param lowerXEnvTime The lowerXEnvTime to set.
     */
    public void setLowerXEnvTime(int lowerXEnvTime) {
        this.lowerXEnvTime = lowerXEnvTime;
    }
    
    /**
     * @return Returns the lowerYEnvTime.
     */
    public int getLowerYEnvTime() {
        return lowerYEnvTime;
    }
    
    /**
     * @param lowerYEnvTime The lowerYEnvTime to set.
     */
    public void setLowerYEnvTime(int lowerYEnvTime) {
        this.lowerYEnvTime = lowerYEnvTime;
    }
    
    /**
     * @return Returns the upperAbsFreqX.
     */
    public double getUpperAbsFreqX() {
        return upperAbsFreqX;
    }
    
    /**
     * @param upperAbsFreqX The upperAbsFreqX to set.
     */
    public void setUpperAbsFreqX(double upperAbsFreqX) {
        this.upperAbsFreqX = upperAbsFreqX;
    }
    
    /**
     * @return Returns the upperAbsFreqY.
     */
    public double getUpperAbsFreqY() {
        return upperAbsFreqY;
    }
    
    /**
     * @param upperAbsFreqY The upperAbsFreqY to set.
     */
    public void setUpperAbsFreqY(double upperAbsFreqY) {
        this.upperAbsFreqY = upperAbsFreqY;
    }
    
    /**
     * @return Returns the upperAmplX.
     */
    public int getUpperAmplX() {
        return upperAmplX;
    }
    
    /**
     * @param upperAmplX The upperAmplX to set.
     */
    public void setUpperAmplX(int upperAmplX) {
        this.upperAmplX = upperAmplX;
    }
    
    /**
     * @return Returns the upperAmplY.
     */
    public int getUpperAmplY() {
        return upperAmplY;
    }
    
    /**
     * @param upperAmplY The upperAmplY to set.
     */
    public void setUpperAmplY(int upperAmplY) {
        this.upperAmplY = upperAmplY;
    }
    
    /**
     * @return Returns the upperCentreX.
     */
    public int getUpperCentreX() {
        return upperCentreX;
    }
    
    /**
     * @param upperCentreX The upperCentreX to set.
     */
    public void setUpperCentreX(int upperCentreX) {
        this.upperCentreX = upperCentreX;
    }
    
    /**
     * @return Returns the upperCentreY.
     */
    public int getUpperCentreY() {
        return upperCentreY;
    }
    
    /**
     * @param upperCentreY The upperCentreY to set.
     */
    public void setUpperCentreY(int upperCentreY) {
        this.upperCentreY = upperCentreY;
    }
    
    /**
     * @return Returns the upperEnvX.
     */
    public int getUpperEnvX() {
        return upperEnvX;
    }
    
    /**
     * @param upperEnvX The upperEnvX to set.
     */
    public void setUpperEnvX(int upperEnvX) {
        this.upperEnvX = upperEnvX;
    }
    
    /**
     * @return Returns the upperEnvY.
     */
    public int getUpperEnvY() {
        return upperEnvY;
    }
    
    /**
     * @param upperEnvY The upperEnvY to set.
     */
    public void setUpperEnvY(int upperEnvY) {
        this.upperEnvY = upperEnvY;
    }
    
    /**
     * @return Returns the upperPhaseX.
     */
    public int getUpperPhaseX() {
        return upperPhaseX;
    }
    
    /**
     * @param upperPhaseX The upperPhaseX to set.
     */
    public void setUpperPhaseX(int upperPhaseX) {
        this.upperPhaseX = upperPhaseX;
    }
    
    /**
     * @return Returns the upperPhaseY.
     */
    public int getUpperPhaseY() {
        return upperPhaseY;
    }
    
    /**
     * @param upperPhaseY The upperPhaseY to set.
     */
    public void setUpperPhaseY(int upperPhaseY) {
        this.upperPhaseY = upperPhaseY;
    }
    
    /**
     * @return Returns the upperRelFreqX.
     */
    public double getUpperRelFreqX() {
        return upperRelFreqX;
    }
    
    /**
     * @param upperRelFreqX The upperRelFreqX to set.
     */
    public void setUpperRelFreqX(double upperRelFreqX) {
        this.upperRelFreqX = upperRelFreqX;
    }
    
    /**
     * @return Returns the upperRelFreqY.
     */
    public double getUpperRelFreqY() {
        return upperRelFreqY;
    }
    
    /**
     * @param upperRelFreqY The upperRelFreqY to set.
     */
    public void setUpperRelFreqY(double upperRelFreqY) {
        this.upperRelFreqY = upperRelFreqY;
    }
    
    /**
     * @return Returns the upperXEnvTime.
     */
    public int getUpperXEnvTime() {
        return upperXEnvTime;
    }
    
    /**
     * @param upperXEnvTime The upperXEnvTime to set.
     */
    public void setUpperXEnvTime(int upperXEnvTime) {
        this.upperXEnvTime = upperXEnvTime;
    }
    
    /**
     * @return Returns the upperYEnvTime.
     */
    public int getUpperYEnvTime() {
        return upperYEnvTime;
    }
    
    /**
     * @param upperYEnvTime The upperYEnvTime to set.
     */
    public void setUpperYEnvTime(int upperYEnvTime) {
        this.upperYEnvTime = upperYEnvTime;
    }

    public int getSampleRate() {
        
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
        
    }

    public int getOutputRate() {
        return outputRate;
    }

    /**
     * @return Returns the bufferLength.
     */
    public int getBufferLength() {
        return bufferLength;
    }

    /**
     * @param bufferLength The bufferLength to set.
     */
    public void setBufferLength(int bufferLength) {
        this.bufferLength = bufferLength;
    }

    /**
     * @param outputRate The outputRate to set.
     */
    public void setOutputRate(int outputRate) {
        this.outputRate = outputRate;
    }
    
} // BezierParams
