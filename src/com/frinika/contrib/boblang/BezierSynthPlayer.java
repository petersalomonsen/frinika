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
import javax.sound.sampled.*;
import javax.swing.*;

/**
  A polyphonic note player for the Bezier Synthesizer.
  
  @author Bob Lang
  @version 25 April 2004
*/
public class BezierSynthPlayer extends Thread {
  // Constants imported from Ant Params
  private static final int
    MAX_AMPLITUDE = BezierParamsV3_5.MAX_AMPLITUDE,
    MAX_PITCH     = BezierParamsV3_5.MAX_PITCH,
    HIGH_PITCH    = BezierParamsV3_5.HIGH_PITCH,
    LOW_PITCH     = BezierParamsV3_5.LOW_PITCH;

  // Constants imported from Command List
  private static final int
    NOTE_ON  = CommandList.NOTE_ON,
    NOTE_OFF = CommandList.NOTE_OFF;

  // Other constants
  private static final int
    SAMPLES_PER_CYCLE = 128,				// Number of samples written
    MAX_16BITS = 32767; 				  	// Maximum 16 bit value
  
  // Undersampling rate derived from sample rate and output rate
  private int
    underSampling;

  // Polyphony (number of simultaneous notes)
  private int
    polyphony;  

  // Java write buffer
  private int
    writeBufferLength;							// Length of line write buffer

  // Links to other objects
  private SourceDataLine
    line;                       // Output sound device
  private BezierEnvelopeShaper
    shaper;                     // Envelope shaper
  private BezierParams
    params;                     // Copy of synth parameters
  private CommandList
    commands;                   // Circular buffer of note on/off commands
  private BezierPlayingNote []
    playingNote;

  /**
    Constructor for this class.
  */
  public BezierSynthPlayer (BezierParams inParams,
                            CommandList inCommands,
                            int inMaxPolyphony)
  {
    // Copy the parameters
    params = inParams;
    
    // Calculate the current undersampling rate
    underSampling = params.getSampleRate()/params.getOutputRate();
    
    // Save the amount of polyphony
    polyphony = inMaxPolyphony;
    playingNote = new BezierPlayingNote [polyphony];

    // Buffer length and number of bytes to write
    writeBufferLength = params.getBufferLength();
    
    // Create the command list
    commands = inCommands;

    // Try to access the sound and midi system devices
    try {
      // Format of audio output buffer
      AudioFormat
        format = new AudioFormat ((float) params.getOutputRate(), 16, 1, true, false);

      // Find a suitable SourceDataLine and set it up
      DataLine.Info
        dataLineInfo = new DataLine.Info (SourceDataLine.class, format);
      line = (SourceDataLine) AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]).getLine (dataLineInfo);

      // Open the line and start it running.
      line.open (format, writeBufferLength);
      //line.open (format);
      //System.out.println (line.getBufferSize ());
      line.start ();

      // Turn off reverb
      //Mixer.Info [] mixerInfo = AudioSystem.getMixerInfo ();
      //System.out.println ("Mixer=" + mixerInfo [0]);
      //Mixer mixer = AudioSystem.getMixer (mixerInfo [0]);
      //Control [] ctrlList = mixer.getControls ();
      //EnumControl cont = (EnumControl) ctrlList[0];
      //Object [] valueList = cont.getValues();
      //for (int obj=0; obj<valueList.length; obj++) {
        //System.out.println ("  Obj= " + valueList [obj]);
      //}
      //cont.setValue (valueList [0]);
      //ctrlList = mixer.getControls ();
      //System.out.println (ctrlList [0]);
    }
    catch (Exception e) {
      String m = "BezierSynthPlayer reports exception\n";
      m += e.toString ();

      JOptionPane.showMessageDialog (null,
                                     m,
                                     null,
                                     JOptionPane.ERROR_MESSAGE);

      System.out.println ("BezierSynthPlayer reports exception");
      System.out.println ("Unable to get sound system resource");
      System.out.println (e);
    } // catch
  } // BezierSynthPlayer ()

  /**
    Change the instrument patch, so that it plays new sounds.
  */
  public void changePatch (BezierParams inParams) {
    // Save the parameters
    params = inParams;
      
    // Create a new envelope shaper
    shaper = new BezierEnvelopeShaper (inParams);
  } // changePatch ()

  /**
    Run method for this thread performs all the necessary thread
    actions.
  */
  public void run () {
    // Polyphonic sound buffer and byte equivalent
    short [][] extract = new short [polyphony][SAMPLES_PER_CYCLE];
    byte [] byteData = new byte [2*SAMPLES_PER_CYCLE];
    int playingCount;

    // Main loop to process commands and play notes
    while (true) {
      // Extract any commands waiting
      if (commands.isCommandWaiting (0L)) {
        // Get the command and the note number
        int [] commandData = commands.getCommand ();
        int commandType = commandData [0];
        int commandNote = commandData [1];

        // Process note on and note off commands
        if (commandType == NOTE_ON) {
          // Start a new note playing
          startPlaying (commandNote);
        } // if
        else if (commandType == NOTE_OFF) {
          // Release a note that's currently playing
          releasePlaying (commandNote);
        } // if
      } // if any commands queued

      // Process all the notes currently playing
      playingCount = 0;
      for (int poly = 0; poly < polyphony; poly++) {
        // Ignore a null entry in the table
        if (playingNote [poly] != null) {
          // Non null entry is a note that's currently playing
          BezierPlayingNote note = playingNote [poly];

          // Extract more sound to keep the note playing
          note.getBuffer (extract [playingCount], SAMPLES_PER_CYCLE);
          playingCount++;

          // See if the note has finished
          if (note.isFinished ()) {
            // Discard the note
            playingNote [poly] = null;
          } // if finished
        } // if note is not null
      } // for poly

      // Add the extracts together to give the final buffer
      addExtracts (extract, playingCount, byteData);

      //Write the data to the output buffer
      //System.out.print ("*");
      line.write (byteData, 0, byteData.length);
    } // while
  } // run ()

  /**
    Add the latest extract to the current buffer
  */
  private void addExtracts (short [][] extract, int extractCount, byte [] byteBuffer) {
    if (extractCount > 0) {
      int k=0;
      int len=extract [0].length;
      short value = 0;

      // Add the extract to the current data buffer
      for (int i = 0; i < len; i++) {
        value = extract [0][i];
        for (int j=1; j < extractCount; j++) {
          value += extract [j][i];
        } // for

        byteBuffer [k] = (byte) (value & 255);
        k++;
        byteBuffer [k] = (byte) (value >>> 8);
        k++;

      } // for
    } // extract count valid
    //$ System.out.print (extractCount);
  } // addExtracts ()

  /**
    Start playing a note.  Put an entry in the playingNote array.
  */
  private void startPlaying (int pitch) {
    // Ensure note is in valid range
    if (pitch >= LOW_PITCH && pitch <= HIGH_PITCH) {
      // Information message
      //$ System.out.print ("On " + pitch);

      // Initialisation
      boolean entered = false;

      // Search for a free entry in the polyphony table
      int polyIndex = 0;
      while (polyIndex < polyphony && !entered) {
        // Is this a free entry?
        if (playingNote [polyIndex] == null) {
          // Start the note playing and stop searching
          BezierSynth synth = new BezierSynth (pitch, params);
          playingNote [polyIndex] =
            new BezierPlayingNote (synth, shaper, pitch, underSampling);
          entered = true;
          //$ System.out.println ("P->" + polyIndex);
        } // if

        // Advance to next entry in the table
        polyIndex++;
      } // while

      // If note not actually entered, search for a released note to replace
      polyIndex = 0;
      while (polyIndex < polyphony && !entered) {
        // Is this a released note?
        if (playingNote [polyIndex].isReleased ()) {
          // Overwrite the playing note by the new note
          BezierSynth synth = new BezierSynth (pitch, params);
          playingNote [polyIndex] =
            new BezierPlayingNote (synth, shaper, pitch, underSampling);
          entered = true;
          //$ System.out.println ("P+>" + polyIndex);
        } // if

        // Advance to next entry in the table
        polyIndex++;
      } // while
    } // if note in valid range
  } // startPlaying ()

  /**
    Release a note that's presently playing.
  */
  private void releasePlaying (int pitch) {
    // Ensure note is in valid range
    if (pitch >= LOW_PITCH && pitch <= HIGH_PITCH) {
      // Display information message
      //$ System.out.print ("Off " + pitch);

      // Search the polyphony table for the note to release
      boolean found = false;
      int polyIndex = 0;
      while (polyIndex < polyphony && !found) {
        // Extract the current note
        BezierPlayingNote note = playingNote [polyIndex];

        // Don't process null notes!
        if (note != null) {
          // Is this the note we should release?
          if (note.getPitch () == pitch && !note.isReleased ()) {
            // Release the note and mark entry as found
            note.setRelease ();
            found = true;
            //$ System.out.println ("R->" + polyIndex);
          } // if
        } // if

        // Advance to the next note in the polyphony table
        polyIndex++;
      } // while
    } // if note in valid range
  } // releasePlaying ()

} // BezierSynthPlayer
