/*
 * Copyright (c) 2004-2007 Paul John Leonard
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
package com.frinika.audio.analysis;

//import rasmus.fft.FFT;
import rasmus.interpreter.sampled.util.FFT;

/**
 *
 * @author pjl
 */
public class FFTWorker {

    private FFT fft;
    private double[] hanning;
    int fftSize;
    private int nBin;
    //  private double[] fftOut;
    float freqArray[];
    double freq[];
    private double Fs;
    private boolean doHanning;

    public FFTWorker(double Fs, boolean doHanning) {
        this.doHanning = doHanning;
//        this.Fs=Fs;
    }

    public float[] getFreqArray() {
        return freqArray;
    }

    public int getSizeInBins() {
        return nBin;
    }

    public void process(double[] input, double fftOut[]) {

        if (doHanning) {
            for (int i = 0; i < fftSize; i++) {
                fftOut[i] = input[i] * hanning[i];
//				fftOut[2 * i] = input[i] * hanning[i];
//				fftOut[2 * i + 1] = 0.0;
            }
        }

        fft.calcReal(fftOut, -1);
    }

    public void resize(int fftsize) {
        this.fftSize = fftsize;
        fft = new FFT(fftsize);
        hanning = fft.wHanning();

        nBin = fftsize / 2;

        //     fftOut = new double[fftsize * 2];
        freqArray = new float[nBin];
        freq = new double[nBin];

        for (int i = 0; i < nBin; i++) {
            freq[i] = (i * Fs / nBin);
            freqArray[i] = (float) freq[i];

            // System.out.println(" fftsize/chunkSIze = " + fftsize + "/"
            // + chunksize);
        }
    }

    public int getFFTSize() {
        return fftSize;
    }
}
