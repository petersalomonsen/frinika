/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    int fftsize;
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
            for (int i = 0; i < fftsize; i++) {
                fftOut[i] = input[i] * hanning[i];
//				fftOut[2 * i] = input[i] * hanning[i];
//				fftOut[2 * i + 1] = 0.0;
            }
        }

        fft.calcReal(fftOut, -1);
   
    }

    public void resize(int fftsize) {
        this.fftsize = fftsize;
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
        return fftsize;
    }
}
