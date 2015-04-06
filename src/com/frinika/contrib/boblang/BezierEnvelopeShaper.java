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
  Implements a basic Attack/decay/sustain/release
  envelope shaper.

  @author Bob Lang
  @version 25 Apr 2004
*/
public class BezierEnvelopeShaper {
  // Constants imported from Bezier Params
  private static final int
    MAX_AMPLITUDE = BezierParamsV3_5.MAX_AMPLITUDE,
    MAX_PITCH     = BezierParamsV3_5.MAX_PITCH,
    HIGH_PITCH    = BezierParamsV3_5.HIGH_PITCH,
    LOW_PITCH     = BezierParamsV3_5.LOW_PITCH;

  // Extra time after release has finished
  private static final int
    RELEASE_EXTRA = 48000;			// Needed to give a full buffer of zeroes

  // Envelope shaping attributes
  private int
    attackTime,                 // Time in mS for attack after note start
    decayTime,                  // Time in mS for decay following attack
    releaseTime,                // Time in mS for decay after release
    attackIndex,                // Point of maximum attack
    decayIndex;                 // Point of final decay
  private double
    sustainLevel,               // Sustain level as fraction of max amplitude
    attackLength,               // Number of samples for attack period
    decayLength,                // Number of samples for decay period
    releaseLength;              // Number of samples for release period

  /**
    Constructor which converts envelope data in the Ant Params object
    into their equivalent attributes.  In practice, it calls the public
    method reshapeEnvelope () to allow dynamic envelope changes
  */
  public BezierEnvelopeShaper (BezierParams params) {
    reshapeEnvelope (params);
  } // BezierEnvelopeShaper ()

  /**
    Convert the envelope data in the parameter object into equivalent
    attributes.  This method may be used to dynamically change the
    envelope, perhaps as a note is playing.
  */
  public final void reshapeEnvelope (BezierParams params) {
    // Copy directly equivalent parameters
    attackTime = params.getEnvAttackTime();
    decayTime = params.getEnvDecayTime();
    releaseTime = params.getEnvReleaseTime();
    sustainLevel = (double) params.getEnvSusLevel() / 100.0;

    // Calculate ending position for the attack and sustain phases
    attackIndex = attackTime*params.getSampleRate()/1000;
    decayIndex = (attackTime + decayTime)*params.getSampleRate()/1000;

    // Calculate number of samples for decay and release periods
    attackLength = (double) attackIndex;
    decayLength = (double) (decayIndex - attackIndex);
    releaseLength = (double) releaseTime*params.getSampleRate()/1000;
  } // reshapeEnvelope ()

  /**
    Get the multiplication factor for the current sample based on its
    position in the sound wave.  The position is obtained from index and
    this version of the method assumes that the note has not been
    released.

    <p>If window is non-zero, then the factor is the average value for
    a window of samples starting at index and of the specified width
  */
  public final double getFactor (int index) {
    // Result to be returned
    double dblIndex, factor;

    // Modify the index to take the window into account
    dblIndex = (double) (index);

    // Is this in the attack phase?
    if (index < attackIndex) {
      // Calculate the attack time factor
      factor = dblIndex/attackLength;
      if (factor > 1.0) {
        factor = 1.0;
      }
      //System.out.println ("A" + factor);
    } // if attack phase

    // Is this the decay phase?
    else if (index < decayIndex) {
      // Calculate the decay time factor
      double decayOffset = (double) (decayIndex - index);
      double attackOffset = (double) (index - attackIndex);
      factor = (decayOffset + sustainLevel*attackOffset)/decayLength;
      //System.out.println ("D" + factor);
    } // if decay phase

    // Assume it's in the sustain phase
    else {
      factor = sustainLevel;
      //System.out.println ("S" + factor);
    } // sustain phase

    // Return the final result
    return factor;
   } // getFactor ()

  /**
    Get the multiplication factor for the current sample based on its
    position in the sound wave but assuming it was released at the
    specified time.
  */
  public final double getRelFactor (int index,
                              int releaseIndex)
  {
    double releaseFactor;
    double dblReleaseIndex = (double) (index - releaseIndex);

    // Calculate the further factor assuming a linear release
    releaseFactor = sustainLevel * (1.0 - dblReleaseIndex/releaseLength);
    if (releaseFactor < 0.01) {
      releaseFactor = 0.0;
    } // if
    //System.out.println ("R" + releaseFactor);
    return releaseFactor;
  } // getFactorAfterRelease ()

  /**
    Return true if the note should have finished by now
  */
  public final boolean isNoteFinished (int index, int releaseIndex) {
      return (getRelFactor(index,releaseIndex)<0.01) ? true : false;
      // The 2 is a fudge factor so that all zeroes are written to the buffer
    //return index-releaseIndex > releaseLength;
  } // isNoteFinished ()
} // EnvelopeShaper
