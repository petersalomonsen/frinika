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
  Stores the details of a note that presently being played by
  the synthesizer

  @author Bob Lang
  @version 22 Mar 2003
*/
public class BezierPlayingNote {
  // Various working values
  private int
    underSampling,              // Undersampling rate
    midiPitch,                  // The pitch of the note
    index,                      // Index position [stepped by undersampling rate]
    releaseIndex;               // Number of samples since release

  // Various flags
  private boolean
    isReleased;                 // True if released

  // Links to other objects and classes
  private BezierSynth
    bezierSynth;                // Which generates sound sample values
  private BezierEnvelopeShaper
    shaper;                     // Envelope shaper

  /**
    Constructor for this class.
  */
  public BezierPlayingNote (BezierSynth inBezierSynth,
                            BezierEnvelopeShaper inShaper,
                            int inMidiPitch,
                            int inUnderSampling)
  {
    // Save the sound buffer data
    bezierSynth = inBezierSynth;
    shaper = inShaper;
    midiPitch = inMidiPitch;
    underSampling = inUnderSampling;

    // Start playback from the start
    index = 0;

    // Note hasn't been released yet
    isReleased = false;
    releaseIndex = 0;
  } // BezierPlayingNote ()

  /**
    Set the release status for this note, recording the index
    number where the release happened.
  */
  public void setRelease () {
    isReleased = true;
    releaseIndex = index;
  } // setRelease ()

  /**
    Get the release status for this loop
  */
  public boolean isReleased () {
    return isReleased;
  } // isReleased ()

  /**
    Has the note finished yet?
  */
  public boolean isFinished () {
    if (!isReleased) {
      return false;
    }
    else {
      return shaper.isNoteFinished (index, releaseIndex);
    }
  } // isFinished ()

  /**
    Get the pitch of this note
  */
  public int getPitch () {
    return midiPitch;
  } // getPitch ()

  /**
    Get the next buffer of data from the sound buffer.  This method
    performs the undersampling necessary to convert from the internal
    sample rate (say 196000) to the output rate (say 48000).  This
    is achieved by incrementing the index by the undersampling rate
    each time.
  */
  public void getBuffer (short [] buffer, int count) {
    // Special action if note is released or not
    if (!isReleased) {
      for (int i=0; i<count; i++) {
        // Get the sample and the envelope factor
        int sample = bezierSynth.getSample (index);
        double factor = shaper.getFactor (index);

        // Perform envelope shaping
        sample = (int) (sample * factor);
        buffer [i] = (short) sample;

        // Increment the index number for the whole note        
        index += underSampling;
      } // for
    } // if
    else {
      // Released note - different version of shaping factor
      for (int i=0; i<count; i++) {
        // Get the sample and the envelope factor
        int sample = bezierSynth.getSample (index);
        double factor = shaper.getRelFactor (index, releaseIndex);

        // Perform envelope shaping
        sample = (int) (sample * factor);
        buffer [i] = (short) sample;

        // Increment the index number for the whole note        
        index += underSampling;
      } // for
    } // else
  } // getBuffer ()
  
  /**
  Get the next buffer of data from the sound buffer.  This method
  performs the undersampling necessary to convert from the internal
  sample rate (say 196000) to the output rate (say 48000).  This
  is achieved by incrementing the index by the undersampling rate
  each time.
  
  Adjustments for Frinika
  32 bit floating point version
  startBuffer and endBufferPos - for realtime parameter modifications
  Add (mix) new samples to the buffer instead of replace.
  @author Bob Lang
  @author Peter Johan Salomonsen
  */
  public void getBuffer (float [] buffer, int startBufferPos, int endBufferPos) {
  // Special action if note is released or not
  if (!isReleased) {
    for (int i=startBufferPos; i<endBufferPos;) {
      // Get the sample and the envelope factor
      double sample = bezierSynth.getSample (index);
      double factor = shaper.getFactor (index);

      // Perform envelope shaping
      sample = (sample * factor) / 32768f; // Assume 16 bit and convert to range -1.0 to 1.0
      buffer [i++] += sample; 
      buffer [i++] -= sample;  // Simple stereo effect

      // Increment the index number for the whole note        
      index += underSampling;
    } // for
  } // if
  else {
    // Released note - different version of shaping factor
    for (int i=startBufferPos; i<endBufferPos; ) {
      // Get the sample and the envelope factor
      double sample = bezierSynth.getSample (index);
      double factor = shaper.getRelFactor (index, releaseIndex);

      // Perform envelope shaping
      sample = (sample * factor) / 32768f; // Assume 16 bit and convert to range -1.0 to 1.0
      buffer [i++] += sample; 
      buffer [i++] -= sample; // Simple stereo effect


      // Increment the index number for the whole note        
      index += underSampling;
    } // for
  } // else
} // getBuffer ()
} // BezierPlayingNote
