/*
 * Created on Mar 20, 2007
 *
 * Copyright (c) 2006-2007 P.J.Leonard
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

package com.frinika.audio.analysis.dft;

import rasmus.interpreter.sampled.util.FFT;

/**
 * Creates a spectrogram from a DoubleDataSource
 * 
 * Observers are notified when data changes (during build)
 * 
 * SizeObserver are notify when the number of frequency bins is changed.
 * 
 * @author pjl
 * 
 */
public class FFTSpectrogramDataBuilder implements ChunkReaderProcess {

	

	
	
	float freqArray[];
//double freq[];

	



	private int nBin;

	float Fs; 



	// int chunksize;

	int fftsize;
	
	FFT fft; 

	double hanning[];

	/**
	 * 
	 * @param minF
	 * @param nOctave
	 * @param binsPerOctave
	 */
	public FFTSpectrogramDataBuilder() {
		//	this.reader=reader;
	}
	
//
//	public FFTSpectrogramDataBuilder() {
//		// TODO Auto-generated constructor stub
//	}

	public void setParameters(int fftsize,float Fs) {

        this.Fs=Fs;
		this.fftsize = fftsize;
	
		fft = new FFT(fftsize);
		nBin=fftsize/2;
		hanning=fft.wHanning();
		freqArray = new float[nBin];

	//	freq = new double[nBin];
		
		for (int i = 0; i < nBin; i++) {
			freqArray[i] = (float) (i*Fs/nBin);
		}

		
	}




	public int getBinCount() {
		return nBin;
	}

	

	
	
	public double [] process(double fftInOut[]) {
		
		for (int i=0;i<fftsize;i++) fftInOut[i]*=hanning[i];
		fft.calcReal(fftInOut,-1);
		return fftInOut;
		
		
	}
	
	
	public float[] getFreqArray() {
		return freqArray;
	}



//	public long getLengthInFrames() {
//		return reader.getLengthInFrames();
//	}



	public float getSampleRate() {
		
		return Fs;
	}

	public FFT getFFT() {
		return fft;
	}

  

}
