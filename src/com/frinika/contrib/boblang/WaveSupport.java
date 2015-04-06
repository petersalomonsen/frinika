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
//import uwejava.*;
import java.awt.Color;

/**
  Support methods for creating PCM wave forms
*/
public class WaveSupport {
  // Propagation options for extending a waveform
  public static final int
    PROP_COPY   = 1,            // Copy the wave form
    PROP_GLIDE  = 2,            // Glide reflect
    PROP_ROTATE = 3;            // Rotate

  // Maximum amplitude in a rescaled waveform
  public static final double
    MAX_AMPLITUDE = 32000.0;

  // Maximum graph handle number
  public static final int
    MAX_GRAPH_HANDLE = 8;

  // Set of static graphs - ready for reuse
  private static Graph []
    graphList = new Graph [MAX_GRAPH_HANDLE];

  /**
    Rescale the wave form so it uses the dynamic range specified
    by the given amplitude.
    @param waveform - array containing the waveform to be rescaled
    @param amplitude - amplitude of the rescaled waveform
  */
  public static int [] rescale (int [] waveform, double amplitude) {
    // Initialisation
    int
      numberOfSamples = waveform.length,
      largestValue    = 0,
      smallestValue   = 0,
      greatestValue   = 0;

    // Create the empty buffer of scaled waveform
    int [] scaledWave = new int [numberOfSamples];

    // Scan the sample to find the greatest absolute value
    for (int i = 0; i < numberOfSamples; i++) {
      int sample = waveform [i];
      if (sample > largestValue) {
        largestValue = sample;
      } // if
      if (sample < smallestValue) {
        smallestValue = sample;
      } // if

      // Find the greatest absolute value
      greatestValue = largestValue;
      if (smallestValue < 0) {
        if (-smallestValue > largestValue) {
          greatestValue = -smallestValue;
        } // if
      } // if
    } // for

    // Calculate the required scaling factor
    double scaleFactor = amplitude/greatestValue;

    // Rescale the data
    for (int i = 0; i < numberOfSamples; i++) {
      scaledWave [i] = (int) (waveform [i]*scaleFactor + 0.5);
    } // for

    // Return the final result
    return scaledWave;
  } // rescale (int [] waveform, double amplitude)

  /**
    Rescale the wave form so it uses (most of) the full dynamic range.
    @param waveform - array containing the waveform to be rescaled
  */
  public static int [] rescale (int [] waveform) {
    return rescale (waveform, MAX_AMPLITUDE);
  } // rescale (int [])

  /**
    Method to propagate a waveform to fill a complete array.
    It uses the possible propagation options.

    @param halfWave - an array containing a half wave
    @param desiredSize - the number of samples in the output array
    @param option - the propagation option
  */
  public static int [] propagate (int [] halfWave,
                                  int desiredSize,
                                  int option)
  {
    // Create empty output array
    int [] outArray = new int [desiredSize];

    // Initialisation
    int inLength = halfWave.length;
    int outPointer = 0;
    int inPointer = 0;

    // Copy the first block of data
    System.arraycopy (halfWave, inPointer, outArray, outPointer, inLength);
    outPointer += inLength;

    // Copy the second half of the waveform according to the copy option
    switch (option) {
  case PROP_COPY:
      // Just copy the first block again
      System.arraycopy (halfWave,
                        inPointer,
                        outArray,
                        outPointer,
                        inLength);
      outPointer += inLength;
      break;

  case PROP_GLIDE:
      // Glide reflection of first block
      for (int i = 0; i < inLength; i++) {
        outArray [inLength + i] = -halfWave [i];
      } // for
      outPointer += inLength;
      break;

  case PROP_ROTATE:
      // Invert address and negate the first block
      for (int i = 0; i < inLength; i++) {
        outArray [inLength + i] = -halfWave [inLength - i - 1];
      } // for
      outPointer += inLength;
      break;
    } // switch

    // Continue repetitions of full wave.  Note that the initial input
    // was only a half wave, hence the multiplications by 2
    while (outPointer <= desiredSize - inLength*2) {
      System.arraycopy (outArray,
                        inPointer,
                        outArray,
                        outPointer,
                        inLength*2);
      outPointer += inLength*2;
    } // while

    // Fill in the remaining elements with a partial repetition
    int j = 0;
    for (int i = outPointer; i < desiredSize; i++) {
      outArray [i] = outArray [j];
      j++;
    } // for

    // Return the final array
    return outArray;
  } // propagate ()

  /**
    Method to apply a rectangular filter to a half wave and
    return the resulting filtered output.  The method needs
    to know how the half wave will be propagated, hence the
    option parameter
  */
  public static int [] filter (int [] halfWave,
                               int filterWidth,
                               int option)
  {
    // Ensure minimum filter width
    if (filterWidth < 1) {
      filterWidth = 1;
    } // if

    // Calculate the lengths of the different arrays
    int inLength = halfWave.length;
    int workLength = inLength*2*3;
    int resultLength = inLength*2;

    // Put three full waves in the working array
    int [] work = propagate (halfWave, workLength, option);

    // Create the output array
    int [] result = new int [resultLength];

    // Calculate the sum for the first result
    int sum = 0;
    int firstIndex = inLength*2 - filterWidth/2;
    int lastIndex = firstIndex + filterWidth - 1;
    for (int i = firstIndex; i <= lastIndex; i++) {
      sum += work [i];
    } // for i

    // Save the sum in the result array
    result [0] = sum/filterWidth;

    // Calculate the sum across a full wave
    for (int i = 1; i < resultLength; i++) {
      // Remove the first index from the sum
      sum = sum - work [firstIndex];
      firstIndex++;

      // Increment and add the new last index to the sum
      lastIndex++;
      sum = sum + work [lastIndex];

      // Store the average in the result array
      result [i] = sum/filterWidth;
    } // for
    return result;
  } // filter ()

  /**
    Method to apply an increasing filter across a waveform.

    @param halfWave - an array containing a half wave
    @param filterWidth - the initial filterWidth
    @param filterIncr - number of samples at which filter increments
    @param desiredSize - the number of samples in the output array
    @param option - the propagation option
  */
  public static int [] increasingFilter (int [] halfWave,
                                         int filterWidth,
                                         int filterIncr,
                                         int desiredSize,
                                         int option)
  {
    // Calculate the lengths of the different arrays
    int inLength = halfWave.length;
    int fullWaveLength = inLength*2;
    int workLength = fullWaveLength*3;
    int resultLength = desiredSize;

    // Put three full waves in the working array
    int [] work = propagate (halfWave, workLength, option);
    //$ plotGraph (0, "Working array", "", "x", "y", work, workLength);

    // Create the output array
    int [] result = new int [resultLength];

    // Calculate the sum for the first result
    int sum = 0;
    int firstIndex = fullWaveLength - filterWidth/2;
    int lastIndex = firstIndex + filterWidth - 1;
    for (int i = firstIndex; i <= lastIndex; i++) {
      sum += work [i];
    } // for i

    //$ System.out.println (firstIndex + " " + lastIndex + " " + sum);
    // Save the sum in the result array
    result [0] = sum/filterWidth;

    // Calculate the sum across a full wave
    int incrCount = 0;
    int workFilterWidth = filterWidth;

    // Calculate the sum
    for (int i = 1; i < resultLength; i++) {
      // Is it necessary to increase the filter width?
      incrCount++;
      if (incrCount >= filterIncr) {
        //$ System.out.println ("Incrementing filter");
        incrCount = 0;
        workFilterWidth++;
      } // if
      else {
        // Remove the first index from the sum
        //$ System.out.println ("Subtracting " + firstIndex + "(" + work [firstIndex] + ")");
        sum = sum - work [firstIndex];
        firstIndex++;
      } // else

      // Increment and add the new last index to the sum
      lastIndex++;
      sum = sum + work [lastIndex];

      //$ System.out.println ("Adding " + lastIndex + "(" + work [lastIndex] + ")");
      // Store the average in the result array
      //$ System.out.println ("Storing result in " + i + "(" + sum + ")[" + workFilterWidth + "]");
      result [i] = sum/workFilterWidth;

      // Ensure that indexes wrap round correctly
      if (firstIndex >= fullWaveLength) {
        //$ System.out.print ("first " + firstIndex + "(" + work [firstIndex] + ") => ");
        firstIndex = firstIndex - fullWaveLength;

        //$ System.out.println (firstIndex + "(" + work [firstIndex] + ")");
      } // if
      if (lastIndex > fullWaveLength) {
        //$ System.out.print ("last " + lastIndex + "(" + work [lastIndex] + ") => ");
        lastIndex = lastIndex - fullWaveLength;

        //$ System.out.println (lastIndex + "(" + work [lastIndex] + ")");
      } // if
    } // for
    return result;
  } // increasingFilter ()


  /**
    Method to change the wavelength waveform using linear interpolation.
    The input wave is assumed to be a single cycle.
  */
  public static int [] changeWaveLength (int [] inWave,
                                         int newWaveLength)
  {
    // Working storage
    int base, step, baseIndex;
    double scaledIndex, offset;
    
    // Calculate the scaling factor
    int inLength = inWave.length;

    /*
    plotGraph (6, "Basic and pitch shifted grains",
          	      "Input Grain", " ", " ",
    		      inWave, 0, inLength);
    */
    
    // Create the output array
    int [] outWave = new int [newWaveLength];

    // Fill up every point in the output array
    for (int i=0; i<newWaveLength; i++) {
      // Scaling operation
      scaledIndex =
        (double) i * (double) inLength / (double) newWaveLength;
      baseIndex = (int) scaledIndex;
      offset = scaledIndex - baseIndex;

      // Calculate the output
      base = inWave [baseIndex];
      step = inWave [(baseIndex+1)%inLength] - base;
      outWave [i] = (int) (base + offset * step);

      /*
        System.out.print ("i=" + i);
        System.out.print (" scaledIndex=" + scaledIndex);
        System.out.print (" baseIndex=" + baseIndex);
        System.out.print (" offset=" + offset);
        System.out.print (" base=" + base);
        System.out.print (" step=" + step);
        System.out.println ();
      */
    } // for
    
    //$ addGraph (6, Graph.BLUE, outWave, outWave.length);

    // Return the final result
    return outWave;
  } // changeWaveLength ()


  /**
    Method to plot an array as a graph.

    @param handle     - graph window to create or re-use
    @param inTitle    - The overall graph title
    @param inXTitle   - X axis title
    @param inYTitle   - Y axis title
    @param inArray    - array to be plotted
    @param inElements - number of elements to plot
  */
  public synchronized static void plotGraph (int handle,
                                             String inTitle,
                                             String inSubTitle,
                                             String inXTitle,
                                             String inYTitle,
                                             int [] inArray,
                                             int inElements)
  {
    // Calculate the number of elements to plot
    int plotCount = inElements;
    if (plotCount > inArray.length) {
      plotCount = inArray.length;
    } // if

    // Ensure that we have a valid handle
    if (handle < MAX_GRAPH_HANDLE) {
      // Are we re-using the graph again?
      if (graphList [handle] != null) {
        // Clear out existing graph for re-use
        graphList [handle].clearGraph (inTitle, inXTitle, inYTitle);
      }
      else {
        // Create a new graph
        graphList [handle] = new Graph (inTitle, inXTitle, inYTitle);
      } // else

      //$ temp test
      graphList [handle].setSubTitle (inSubTitle);

      // Set the plot colour to red
      graphList [handle].setColour (Graph.RED);

      // Plot all the points onto the graph
      for (int i = 0; i < plotCount; i++) {
        graphList [handle].add (i, inArray [i]);
      } // for

      // Show the graph - don't exit when closed
      graphList [handle].showGraph (false);
    } // valid handle number
  } // plotGraph ()

  /**
    Method to plot a section of an array as a graph.

    @param handle     - graph window to create or re-use
    @param inTitle    - The overall graph title
    @param inXTitle   - X axis title
    @param inYTitle   - Y axis title
    @param inArray    - array to be plotted
    @param inFirst    - first element to plot
    @param inLast     - last element to plot
  */
  public synchronized static void plotGraph (int handle,
                                             String inTitle,
                                             String inSubTitle,
                                             String inXTitle,
                                             String inYTitle,
                                             int [] inArray,
                                             int inFirst,
                                             int inLast)
  {
    // Check that parameters are valid
    if (inLast > inArray.length) {
      inLast = inArray.length - 1;
    } // if
    if (inFirst > inLast) {
      inFirst = inLast - 24;
    } // if
    if (inFirst < 0) {
      inFirst = 0;
    } // if

    // Ensure that we have a valid handle
    if (handle < MAX_GRAPH_HANDLE) {
      // Are we re-using the graph again?
      if (graphList [handle] != null) {
        // Clear out existing graph for re-use
        graphList [handle].clearGraph (inTitle, inXTitle, inYTitle);
      }
      else {
        // Create a new graph
        graphList [handle] = new Graph (inTitle, inXTitle, inYTitle);
      } // else

      //$ temp test
      graphList [handle].setSubTitle (inSubTitle);

      // Set the plot colour to red
      graphList [handle].setColour (Graph.RED);

      // Plot all the points onto the graph
      for (int i = inFirst; i < inLast; i++) {
        graphList [handle].add (i-inFirst, inArray [i]);
      } // for

      // Show the graph - don't exit when closed
      graphList [handle].showGraph (false);
    } // valid handle number
  } // plotGraph ()

  /**
    Method to add a further plot to an existing graph.

    @param handle     - graph window to create or re-use
    @param inArray    - array to be plotted
    @param inElements - number of elements to plot
  */
  public synchronized static void addGraph (int handle,
                                            Color inColour,
                                            int [] inArray,
                                            int inElements)
  {
    // Calculate the number of elements to plot
    int plotCount = inElements;
    if (plotCount > inArray.length) {
      plotCount = inArray.length;
    } // if

    // Ensure that we have a valid handle
    if (handle < MAX_GRAPH_HANDLE) {
      // Are we re-using the graph again?
      if (graphList [handle] != null) {
        // Clear out existing graph for re-use
        graphList [handle].nextGraph ();
      }
      else {
        // Create a new graph
        graphList [handle] = new Graph ("Untitled Graph", "x", "y");
      } // else

      // Set the plot colour
      graphList [handle].setColour (inColour);

      // Plot all the points onto the graph
      for (int i = 0; i < plotCount; i++) {
        graphList [handle].add (i, inArray [i]);
      } // for

      // Show the graph - don't exit when closed
      graphList [handle].showGraph (false);
    } // valid handle number
  } // addGraph ()

  /**
    Method to add an array subset as a further plot to an
    existing graph.

    @param handle     - graph window to create or re-use
    @param inArray    - array to be plotted
    @param inElements - number of elements to plot
  */
  public synchronized static void addGraph (int handle,
                                            Color inColour,
                                            int [] inArray,
                                            int inFirst,
                                            int inLast)
  {
    // Check that parameters are valid
    if (inLast > inArray.length) {
      inLast = inArray.length - 1;
    } // if
    if (inFirst > inLast) {
      inFirst = inLast - 24;
    } // if
    if (inFirst < 0) {
      inFirst = 0;
    } // if

    // Ensure that we have a valid handle
    if (handle < MAX_GRAPH_HANDLE) {
      // Are we re-using the graph again?
      if (graphList [handle] != null) {
        // Clear out existing graph for re-use
        graphList [handle].nextGraph ();
      }
      else {
        // Create a new graph
        graphList [handle] = new Graph ("Untitled Graph", "x", "y");
      } // else

      // Set the plot colour to green
      graphList [handle].setColour (inColour);

      // Plot all the points onto the graph
      for (int i = inFirst; i < inLast; i++) {
        graphList [handle].add (i-inFirst, inArray [i]);
      } // for

      // Show the graph - don't exit when closed
      graphList [handle].showGraph (false);
    } // valid handle number
  } // addGraph ()


  /**
    Method to plot the data in as a FFT.  The x co-ordinates are
    frequency bins which are converted to the appropriate frequencies
    before being plotted

    @param handle         - graph window to create or re-use
    @param inArray        - output from FFT
    @param fourierSamples - number of samples in FFT
    @param sampleRate     - sample rate in samples/sec
    @param inElements     - number of elements to plot
  */
  public synchronized static void plotFourier (int handle,
                                               double [] inArray,
                                               int fourierSamples,
                                               int sampleRate,
                                               int inElements)
  {
    String
      graphTitle = "Fourier Transform",
      subTitle   = " ",
      xTitle     = "Hz",
      yTitle     = "Power";

    // Working variables
    double freq, logOfHeight;

    // Calculate the number of elements to plot
    int plotCount = inElements;
    if (plotCount > inArray.length) {
      plotCount = inArray.length;
    } // if

    // Ensure that we have a valid handle
    if (handle < MAX_GRAPH_HANDLE) {
      // Are we re-using the graph again?
      if (graphList [handle] != null) {
        // Clear out existing graph for re-use
        graphList [handle].clearGraph (graphTitle, xTitle, yTitle);
      }
      else {
        // Create a new graph
        graphList [handle] = new Graph (graphTitle, xTitle, yTitle);
      } // else
      graphList [handle].setSubTitle (subTitle);

      // Set the plot colour to blue
      graphList [handle].setColour (Graph.BLUE);

      // Plot all the points onto the graph
      for (int i = 0; i < plotCount; i++) {
        freq = Fourier.frequencyFromBin (i, fourierSamples, sampleRate);
        graphList [handle].add (freq, inArray [i]);
      } // for

      // Show the graph - don't exit when closed
      graphList [handle].showGraph (false);
    } // valid handle number
  } // plotFourier ()

  /**
    Static local sine and cosine tables
  */
  private static double [] sineTable = new double [360];
  private static double [] cosineTable = new double [360];

  /**
    Local version of sine method (with parameter in degrees)
  */
  public final static double localSine (double degrees) {
    int idegrees = (int) degrees + 360;
    idegrees = idegrees % 360;
    return sineTable [idegrees];
  } // localSine ()

  /**
    Local version of cosine method (with parameter in degrees)
  */
  public final static double localCosine (double degrees) {
    int idegrees = (int) degrees + 360;
    idegrees = idegrees % 360;
    return cosineTable [idegrees];
  } // localCosine ()

  /**
    Static initialiser for the local sine and cosine lookup tables
  */
  static {
    for (int i=0; i<360; i++) {
      sineTable [i] = Math.sin (2.0*Math.PI*i/360.0);
      cosineTable [i] = Math.sin (2.0*Math.PI*i/360.0);
    } // for
  } // static
  
} // WaveSupport
