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
  Class to perform Fast Fourier transformations - based on fourierd.c
  by Don Cross <dcross@intersrv.com>

  @author Bob Lang (Java conversion)
  @author Don Cross (Original C/C++ implementation)
*/
public class Fourier {
  // Max sample size and number of bits for indexing
  private static final int
    MAX_BITS        = 14,
    MAX_SAMPLE_SIZE = 16384;

  // Dynamic look up table of reversed indexing bits
  private static int []
    reversedBits;

  // Current/previous number of bits used for indexing
  private static int
    currentNumberOfBits = 0;

  /**
    Number of bits needed for any particular sample size.
    The sample size must be a power of two and in the valid
    range of samples 2..MAX_SAMPLE_SIZE
  */
  private static int numberOfBitsNeeded (int sampleSize) {
    if (sampleSize < 2 || sampleSize > MAX_SAMPLE_SIZE)
      return 0;
    else {
      if (sampleSize == 2)
        return 1;
      if (sampleSize == 4)
        return 2;
      if (sampleSize == 8)
        return 3;
      if (sampleSize == 16)
        return 4;
      if (sampleSize == 32)
        return 5;
      if (sampleSize == 64)
        return 6;
      if (sampleSize == 128)
        return 7;
      if (sampleSize == 256)
        return 8;
      if (sampleSize == 512)
        return 9;
      if (sampleSize == 1024)
        return 10;
      if (sampleSize == 2048)
        return 11;
      if (sampleSize == 4096)
        return 12;
      if (sampleSize == 8192)
        return 13;
      if (sampleSize == 16384)
        return 14;
      else
        return 0;
    } // else
  } // numberOfBitsNeeded ()

  /**
    Calculate the next power of two from the sample size,
    assuming that the sample size isn't already a power of two
  */
  public static int nextPowerOfTwo (int sampleSize) {
    if (sampleSize < 2 || sampleSize > MAX_SAMPLE_SIZE)
      return 0;
    else {
      if (sampleSize <= 2)
        return 2;
      if (sampleSize <= 4)
        return 4;
      if (sampleSize <= 8)
        return 8;
      if (sampleSize <= 16)
        return 16;
      if (sampleSize <= 32)
        return 32;
      if (sampleSize <= 64)
        return 64;
      if (sampleSize <= 128)
        return 128;
      if (sampleSize <= 256)
        return 256;
      if (sampleSize <= 512)
        return 512;
      if (sampleSize <= 1024)
        return 1024;
      if (sampleSize <= 2048)
        return 2048;
      if (sampleSize <= 4096)
        return 4096;
      if (sampleSize <= 8196)
        return 8196;
      if (sampleSize <= 16384)
        return 16384;
      else
        return 0;
    } // else
  } // nextPowerOfTwo ()

  /**
    Return true if the number of samples is a power of two and
    within the valid range of samples.
  */
  private static boolean isPowerOfTwo (int sampleSize) {
    return (numberOfBitsNeeded (sampleSize) != 0);
  } // isPowerOfTwo ()

  /**
    Calculate the power of two from the number of bits
  */
  private static int powerOfTwo (int power) {
    int result = 1;
    for (int i = 1; i <= power; i++) {
      result <<= 1;
    } // for
    return result;
  } // powerOfTwo ()

  /**
    Reverse the bit pattern of the index within the specified
    number of bits.  It uses an array for rapid calculation
  */
  private static int reverseBits (int index, int numberOfBits) {
    // See if the fast lookup table needs to be allocated
    if (numberOfBits != currentNumberOfBits) {
      setupReverseBitsTable (numberOfBits);
    }

    // Lookup the reverse bit pattern from the table
    return reversedBits [index];
  } // reverseBits ()

  /**
    Setup the lookup table of reversed indexing bits
  */
  private static void setupReverseBitsTable (int numberOfBits) {
    // Allocate the table
    int tableSize = powerOfTwo (numberOfBits);
    currentNumberOfBits = numberOfBits;
    reversedBits = new int [tableSize];

    //$ System.out.print ("Allocated table size = " + tableSize);
    //$ System.out.println (" bits " + numberOfBits);
    // Loop to calculate and store each reversed index
    for (int index = 0; index < tableSize; index++) {
      int ind = index;
      int rev = 0;

      //$ System.out.print ("Index = " + index);
      for (int b = 0; b < numberOfBits; b++) {
        rev <<= 1;
        rev = rev + ind%2;
        ind >>= 1;
      } // while

      //$ System.out.println ("  Reversed = " + rev);
      // Save the new value in the array
      reversedBits [index] = rev;
    } // for
  } // setupReverseBitsTable ()

  /**
    Base FFT method which performs an FFT on a power of two samples.
    Blatantly copied from FOURIERD.C by Don Cross
  */
  public static void fftBase (int numSamples,
                              boolean inverseTransform,
                              double [] realIn,
                              double [] imagIn,
                              double [] realOut,
                              double [] imagOut)
  {
    double ar [] = new double [3];
    double ai [] = new double [3];
    int numBits;                // Number of bits needed to store indices
    int i, j, k, n;
    int blockSize, blockEnd;
    double angleNumerator = 2.0*Math.PI;
    double tr, ti;              // temp real, temp imaginary

    // Confirm we have a power of two samples
    if (!isPowerOfTwo (numSamples)) {
      System.out.println ("Error in fft():  numSamples is not power of two");
      return;
    } // if
    if (inverseTransform)
      angleNumerator = -angleNumerator;

    // Find number of bits needed
    numBits = numberOfBitsNeeded (numSamples);

    /*
      **   Do simultaneous data copy and bit-reversal ordering into outputs...
    */
    for (i = 0; i < numSamples; i++) {
      j = reverseBits (i, numBits);
      realOut [j] = realIn [i];
      imagOut [j] = imagIn [i];
    } // for

    /*
      **   Do the FFT itself...
    */
    blockEnd = 1;
    for (blockSize = 2; blockSize <= numSamples; blockSize <<= 1) {
      double deltaAngle = angleNumerator/(double) blockSize;
      double sm2 = Math.sin (-2*deltaAngle);
      double sm1 = Math.sin (-deltaAngle);
      double cm2 = Math.cos (-2*deltaAngle);
      double cm1 = Math.cos (-deltaAngle);
      double w = 2*cm1;

      // double ar[3], ai[3];
      double temp;
      for (i = 0; i < numSamples; i += blockSize) {
        ar [2] = cm2;
        ar [1] = cm1;
        ai [2] = sm2;
        ai [1] = sm1;
        for (j = i, n = 0; n < blockEnd; j++, n++) {
          ar [0] = w*ar [1] - ar [2];
          ar [2] = ar [1];
          ar [1] = ar [0];
          ai [0] = w*ai [1] - ai [2];
          ai [2] = ai [1];
          ai [1] = ai [0];
          k = j + blockEnd;
          tr = ar [0]*realOut [k] - ai [0]*imagOut [k];
          ti = ar [0]*imagOut [k] + ai [0]*realOut [k];
          realOut [k] = realOut [j] - tr;
          imagOut [k] = imagOut [j] - ti;
          realOut [j] += tr;
          imagOut [j] += ti;
        } // for
      } // for
      blockEnd = blockSize;
    } // for

    /*
      **   Need to normalize if inverse transform...
    */
    if (inverseTransform) {
      double denom = (double) numSamples;
      for (i = 0; i < numSamples; i++) {
        realOut [i] /= denom;
        imagOut [i] /= denom;
      } // for
    } // if
  } // fftBase ()

  /**
    Perform an FFT on a sequence of samples and produce a list
    of frequency values as output.  The method assumes that only the
    real data should be supplied, and the output produced is the magnitude
    of the frequency components
  */
  public static void frequencyFft (int numberOfSamples,
                                   double [] dataIn,
                                   double [] frequencies)
  {
    // Create the real and imaginary output tables
    double [] realOut = new double [numberOfSamples];
    double [] imagOut = new double [numberOfSamples];

    // Create a list of zeroes for the imaginary input
    double [] imagIn = padToPowerOfTwo (numberOfSamples, null);

    // Perform the FFT
    fftBase (numberOfSamples, false, dataIn, imagIn, realOut, imagOut);

    // Calculate the frequency components
    for (int bin = 0; bin < numberOfSamples; bin++) {
      double
        fsq = realOut [bin]*realOut [bin] + imagOut [bin]*imagOut [bin];
      frequencies [bin] = Math.sqrt (fsq);
    } // for
  } // frequencyFft ()

  /**
    Integer version of frequencyFft ().
  */
  public static void frequencyFft (int numberOfSamples,
                                   int [] dataIn,
                                   double [] frequencies)
  {
    double [] dDataIn = new double [numberOfSamples];
    for (int i = 0; i < numberOfSamples; i++) {
      dDataIn [i] = (double) dataIn [i];
    } // for
    frequencyFft (numberOfSamples, dDataIn, frequencies);
  } // frequencyFft (int [])

  /**
    Calculate the frequency from a specified index number of the
    output real/imag arrays.  After an FFT with N samples, the
    frequency spectrum from DC to half the sampling frequency
    is divided into n/2 frequency bins.
  */
  public static double frequencyFromBin (int binNumber,
                                         int numberOfSamples,
                                         double samplingFrequency)
  {
    double frequency;
    double n = (double) numberOfSamples;
    double b = (double) binNumber;
    if (binNumber > numberOfSamples/2) {
      // Negative frequency range
      frequency = samplingFrequency*(b - n)/n;
    }
    else {
      // Positive frequency range
      frequency = samplingFrequency*b/n;
    }
    return frequency;
  } // frequencyFromBin ()

  /**
    Produce an array which is a power of two long and is suitably
    padded with zeroes.  If the input array is null then a
    suitable zero padded array of the correct size is returned
  */
  public static double [] padToPowerOfTwo (int numberOfSamples,
                                           double [] inData)
  {
    // Output array (not yet allocated)
    double [] outData;

    // Check if the in data array is okay to use
    if (isPowerOfTwo (numberOfSamples) && inData != null) {
      return inData;
    }
    else {
      // Allocate a larger array
      int desiredSamples = nextPowerOfTwo (numberOfSamples);
      outData = new double [desiredSamples];

      // Is there any input data to copy?
      if (inData == null) {
        // Set all the values to zero
        for (int i = 0; i < desiredSamples; i++) {
          outData [i] = 0.0;
        } // for
      } // if
      else {
        // Copy across input data
        for (int i = 0; i < numberOfSamples; i++) {
          outData [i] = inData [i];
        } // for

        // Pad the array with zeroes
        for (int i = numberOfSamples + 1; i < desiredSamples; i++) {
          outData [i] = 0.0;
        } // for
      } // else

      // Return the allocated array
      return outData;
    } // else
  } // padToPowerOfTwo ()
} // Fourier
