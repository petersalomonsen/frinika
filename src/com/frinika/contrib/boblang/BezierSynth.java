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

import com.frinika.synth.Oscillator;

/**
  Bezier Wave 3.2

  <p>An object of this class is a synthesizer for a single note played on
  the keyboard.

  @author Bob Lang
  @version 7 April 2003
*/
public class BezierSynth {
  // Constants imported from other classes
  public static final int
    MAX_AMPLITUDE = BezierParamsV3_5.MAX_AMPLITUDE,
    MAX_PITCH     = BezierParamsV3_5.MAX_PITCH,
    HIGH_PITCH    = BezierParamsV3_5.HIGH_PITCH,
    LOW_PITCH     = BezierParamsV3_5.LOW_PITCH,
    AMP_STEADY    = BezierParamsV3_5.AMP_STEADY,
    AMP_RISE_FALL = BezierParamsV3_5.AMP_RISE_FALL,
    AMP_RISING    = BezierParamsV3_5.AMP_RISING,
    AMP_FALLING   = BezierParamsV3_5.AMP_FALLING;

  // The X and Y centre points are measured in absolute "pixel" positions.
  private int
    x2centre,                   // X centre position of upper control point
    y2centre,                   // Y centre position of upper control point
    x3centre,                   // X centre position of lower control point
    y3centre;                   // Y centre position of lower control point

  // Amplitudes are measured in absolute "pixel" positions
  private int
    x2amplitude,                // Max X amplitude of upper control point
    y2amplitude,                // Max Y amplitude of upper control point
    x3amplitude,                // Max X amplitude of lower control point
    y3amplitude;                // Max Y amplitude of lower control point

  // Amplitude envelopes
  private int
    x2envType,                  // Upper X amplitude envelope type
    y2envType,                  // Upper Y amplitude envelope type
    x3envType,                  // Lower X amplitude envelope type
    y3envType;                  // Lower Y amplitude envelope type

  // Amplitude envelope times in millisecs
  private int
    x2envTime,                  // Upper X envelope time
    y2envTime,                  // Upper Y envelope time
    x3envTime,                  // Lower X envelope time
    y3envTime;                  // Lower Y envelope time

  // Angular velocities are measured in absolute degrees/second
  private double
    x2AngVel,                   // SHM X frequency of upper control point
    y2AngVel,                   // SHM Y frequency of upper control point
    x3AngVel,                   // SHM X frequency of lower control point
    y3AngVel;                   // SHM Y frequency of lower control point

  // Initial phase values, stored in degrees
  private double
    x2Phase,                    // X upper frequency phase offset
    y2Phase,                    // Y upper frequency phase offset
    x3Phase,                    // X lower frequency phase offset
    y3Phase;                    // Y lower frequency phase offset

  // Other useful values
  private double
    frequency;                  // Frequency of generated wave in Hz
  private int
    sampleRate,                 // Sample rate
    wavelength;                 // Number of X points for whole wave

  /**
    Constructor to create a new Bezier wave of the specified frequency.
  */
  public BezierSynth (int inMidiPitch, BezierParams inParams) {
    // Working storage
    double f;

    // Save the sample rate
    sampleRate = inParams.getSampleRate();
    
    // Calculate the fundamental frequency and the wavelength of the note
    frequency = Oscillator.getFrequency (inMidiPitch);

    // Wavelength is limited to an integer number of samples
    wavelength = inParams.wavelengthFromFrequency (frequency);

    // Get actual frequency from integer wave length (not nominal)
    frequency = inParams.frequencyFromWavelength (wavelength);

    // **** Transfer each parameter, making any necessary scale changes ****
    // Position and amplitude of the control points
    x2centre = inParams.getUpperCentreX()*wavelength/100;
    y2centre = inParams.getUpperCentreY()*MAX_AMPLITUDE/100;
    x3centre = inParams.getLowerCentreX()*wavelength/100;
    y3centre = inParams.getLowerCentreY()*MAX_AMPLITUDE/100;
    x2amplitude = inParams.getUpperAmplX()*wavelength/100;
    y2amplitude = inParams.getUpperAmplY()*MAX_AMPLITUDE/100;
    x3amplitude = inParams.getLowerAmplX()*wavelength/100;
    y3amplitude = inParams.getLowerAmplY()*MAX_AMPLITUDE/100;
    x2envType = inParams.getUpperEnvX();
    y2envType = inParams.getUpperEnvY();
    x3envType = inParams.getLowerEnvX();
    y3envType = inParams.getLowerEnvY();
    x2envTime = inParams.getUpperXEnvTime();
    y2envTime = inParams.getUpperYEnvTime();
    x3envTime = inParams.getLowerXEnvTime();
    y3envTime = inParams.getLowerYEnvTime();

    // Calculate the angular velocities of each control point
    f = frequency*inParams.getUpperRelFreqX() + inParams.getUpperAbsFreqX();
    x2AngVel = 360.0*f;
    f = frequency*inParams.getUpperRelFreqY() + inParams.getUpperAbsFreqY();
    y2AngVel = 360.0*f;
    f = frequency*inParams.getLowerRelFreqX() + inParams.getLowerAbsFreqX();
    x3AngVel = 360.0*f;
    f = frequency*inParams.getLowerRelFreqY() + inParams.getLowerAbsFreqY();
    y3AngVel = 360.0*f;

    // Save the initial phases (in degrees)
    x2Phase = inParams.getUpperPhaseX();
    y2Phase = inParams.getUpperPhaseY();
    x3Phase = inParams.getLowerPhaseX();
    y3Phase = inParams.getLowerPhaseY();

    // printParams ();
  } // BezierSynth ()

  /**
    Diagnostic print of the Bezier parameters
  */
  public void printParams () {
    System.out.println ("Freq= " + frequency + " wavelength=" + wavelength);
    System.out.println ("upper centre = " + x2centre + "," + y2centre);
    System.out.println ("lower centre = " + x3centre + "," + y3centre);
    System.out.println ("upper ampl: x= " + x2amplitude + " y= " + y2amplitude);
    System.out.println ("lower ampl: x= " + x3amplitude + " y= " + y3amplitude);
    System.out.println ("upper avel: x= " + x2AngVel + " y=" + y2AngVel);
    System.out.println ("lower avel: x= " + x3AngVel + " y=" + y3AngVel);
  } // printParams ()

  /**
    Calculate the value of the sound sample with the given index number
  */
  public final int getSample (int sampleNumber) {
    // Four control points to define the Bezier curve
    double x1, x2, x3, x4, y1, y2, y3, y4;

    // Control terms for the Bezier curve and the "t" parameter
    double ax, bx, cx, ay, by, cy, t;

    // Actual time from the start of the waveform
    double time = (double) sampleNumber/(double) sampleRate;

    // Calculate the current amplitudes of the control points
    double x2amp = getAmplitude (x2amplitude, x2envType, x2envTime, time);
    double y2amp = getAmplitude (y2amplitude, y2envType, y2envTime, time);
    double x3amp = getAmplitude (x3amplitude, x3envType, x3envTime, time);
    double y3amp = getAmplitude (y3amplitude, y3envType, y3envTime, time);

    // Calculate the x and y positions of the control points
    // First control point is the start of the wave
    x1 = 0.0;
    y1 = 0.0;

    // Fourth control point is the end of the wave
    x4 = wavelength;
    y4 = 0.0;

    // Second (or upper) control point
    x2 = x2centre
       + x2amp*WaveSupport.localCosine (x2AngVel*time + x2Phase);
    y2 = y2centre + y2amp*WaveSupport.localSine (y2AngVel*time + y2Phase);

    // Third (or lower) control point
    x3 = x3centre
       + x3amp*WaveSupport.localCosine (x3AngVel*time + x3Phase);
    y3 = y3centre + y3amp*WaveSupport.localSine (y3AngVel*time + y3Phase);

    /*
      System.out.println ("Sample=" + sampleNumber + " Time=" + time);
      System.out.print ("  Upper point = " + x2 + "," + y2);
      System.out.println ("  Lower point = " + x3 + "," + y3);
    */

    // Calculate the control terms used in the cubic equations
    cx = 3.0*(x2 - x1);
    cy = 3.0*(y2 - y1);
    bx = 3.0*(x3 - x2 - x2 + x1);
    by = 3.0*(y3 - y2 - y2 + y1);
    ax = x4 - 3.0*(x3 - x2) - x1;
    ay = y4 - 3.0*(y3 - y2) - y1;

    // Find the value of the "t" parameter corresponding to this sample number
    t = findT (ax, bx, cx, x1, sampleNumber);

    // Calculate and return the sample value
    double sample = ay*t*t*t + by*t*t + cy*t + y1;

    // System.out.println (sample);
    return (int) sample;
  } // getSample ()

  /**
    <P>Method to calculate the "t" parameter from the sample number.
    This method uses Newton's iterative technique to find the value
    of "t" which corresponds to the desired Bezier x value.

    <P>The method uses Newton's algorithm and in cases where the wave
    curves back on itself, this algorithm may either loop indefinitely
    of find a value of t outside its valid range of 0..1.  In such cases,
    the method returns an arbitrary value of zero.
  */
  private double findT (double a,
                        double b,
                        double c,
                        double x1,
                        int sampleNumber)
  {
    // Local variables
    int maxLoops = 10;
    double t, d, x;
    int loopCount = 0;

    // Calculate index within the current wave cycle
    int index = sampleNumber%wavelength;

    // First approximation to the t parameter
    t = (double) index/wavelength;

    // Newton's iteration method for successive approximation
    do {
      x = a*t*t*t + b*t*t + c*t + x1 - index;
      d = 3*a*t*t + 2*b*t + c;
      t = t - x/d;
      loopCount++;
    } while (Math.abs (x) > 0.0001 && loopCount < maxLoops);

    // Fudge a zero return value if no true value found
    if (loopCount >= maxLoops || t < 0.0 || t > 1.0) {
      t = 0.0;
    } // if

    // Return the final approximated value of t
    return t;
  } // findT ()

  /**
    Find the amplitude of the control point from the time and type
  */
  private double getAmplitude (double inAmplitude,
                               int inEnvType,
                               int inEnvTimeMillis,
                               double time)
  {
    // Create local storage
    double factor = 1.0;
    double timeValue = (double) inEnvTimeMillis/1000.0;

    // Select appropriate processing for each envelope type
    switch (inEnvType) {
  case AMP_STEADY:
      // Steady amplitude - factor is unity
      factor = 1.0;
      break;

  case AMP_RISE_FALL:
      // Rise and fall envelope
      // Test for rising part..
      if (time < timeValue) {
        factor = time/timeValue;
      }

      // Test for falling part
      else if (time < 2.0*timeValue) {
        factor = 1.0 - (time - timeValue)/timeValue;
      }

      // Envelope finished
      else {
        factor = 0.0;
      } // else
      break;

  case AMP_RISING:
      // Rising envelope
      // Test for rising part
      if (time < timeValue) {
        factor = time/timeValue;
      }

      // Else constant full amplitude
      else {
        factor = 1.0;
      } // else
      break;

  case AMP_FALLING:
      // Falling amplitude
      // Test for falling part
      if (time < timeValue) {
        factor = 1.0 - time/timeValue;
      }

      // Else constant zero amplitude
      else {
        factor = 0.0;
      } // else
      break;
    } // case

    // Calculate the final amplitude
    return inAmplitude*factor;
  } // getAmplitude ()
} // BezierSynth
