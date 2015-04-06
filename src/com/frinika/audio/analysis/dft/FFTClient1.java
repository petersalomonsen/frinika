/*
 * Created on 2 Jan 2008
 *
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

package com.frinika.audio.analysis.dft;


public class FFTClient1 implements		FFTSpectrumClient {

	private float[][] magnArray;
	private float[][] magnArrayX;
	
	private double logMagn[];

	int chunkPtr;

	private int sizeInChunks;
	
	
	FFTSpectrogramDataBuilder builder;
	
	public FFTClient1(FFTSpectrogramDataBuilder builder) {
		this.builder=builder;
	}

	public void setSize(int sizeInChunks, int nBin, float freq[], double dt) {


		magnArray = new float[sizeInChunks][nBin];
		magnArrayX = new float[sizeInChunks][nBin];
		logMagn=new double[nBin*2];
		this.sizeInChunks=sizeInChunks;

		
		chunkPtr=0;
	}

	public float[][] getMagnitude() {
		return magnArray;
	}

	public float[][] getSMagnitude() {
		return magnArrayX;
	}

	public void process(double fftOut[], int nBin) {

//		double maxV=0.0;
		if (chunkPtr >= sizeInChunks) {
			try {
				throw new Exception(" ptr tpp big ");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
		}
		for (int i = 0; i < nBin; i++) {
			double real = fftOut[2 * i];
			double imag = fftOut[2 * i + 1];

			magnArray[chunkPtr][i] = (float) Math.sqrt(real * real + imag
					* imag);
			logMagn[i]=Math.log(magnArray[chunkPtr][i]);
			logMagn[i+nBin]=0.0;
		//	maxV = Math.max(maxV, magnArray[chunkPtr][i]);

		}
		
		//
		builder.getFFT().calcReal(logMagn, -1);

		double cutoff=400;  // filter changes greater than 400 Hz// filter 
		double Fs=builder.getSampleRate();
		int i1=(int) (Fs/cutoff);
		for (int i = i1; i < nBin; i++) {
			logMagn[2*i]=logMagn[2*i+1]=0.0f; // (float) Math.sqrt(logMagn[2*i]*logMagn[2*i]+ logMagn[2*i+1]*logMagn[2*i+1]);	
		}
		
		
		builder.getFFT().calcReal(logMagn, 1);

		
		for (int i = 0; i < nBin; i++) {
			magnArrayX[chunkPtr][i]=(float) Math.exp( logMagn[i]/nBin); // (float) Math.sqrt(logMagn[2*i]*logMagn[2*i]+ logMagn[2*i+1]*logMagn[2*i+1]);	
		}

		
//		
	//	System.out.println(maxV + " @ " +chunkPtr);
		chunkPtr++;

	}

	public float[] getMagnitudeAt(long chunkPtr) {
		if (magnArray == null)
			return null;

		// int pix = (int) (framePtr / chunksize);
		if (chunkPtr >= magnArray.length || chunkPtr < 0)
			return null;
		return magnArray[(int) chunkPtr];
	}

	public float[] getPhaseAt(long chunkPtr) {
		return null;

	}

	public float[] getPhaseFreqAt(long chunkPtr) {
		return null;

	}

	public int getSizeInChunks() {
		return sizeInChunks;

	}

	public int getChunkRenderedCount() {
		return chunkPtr;
	}
	
	public boolean validAt(long chunkPtr2) {
		return chunkPtr2>=0  && chunkPtr2 < this.chunkPtr;
	}

	public float[] getSmagnitudeAt(long chunkPtr) {
		if (magnArrayX == null)
			return null;

		// int pix = (int) (framePtr / chunksize);
		if (chunkPtr >= magnArrayX.length || chunkPtr < 0)
			return null;
		return magnArrayX[(int) chunkPtr];
	}

}
