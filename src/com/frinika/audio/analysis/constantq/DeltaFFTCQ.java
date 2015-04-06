/* 
 * Copyright (c) 2006, Karl Helgason
 * 
 * 2007/1/8 modified by p.j.leonard
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.frinika.audio.analysis.constantq;


import rasmus.interpreter.sampled.util.FFT;

// Implementation of Constant Q Transform
//
// References:
//
// Judith C. Brown,
// Calculation of a constant Q spectral transform, J. Acoust. Soc. Am., 89(1):
// 425-434, 1991.
// see http://www.wellesley.edu/Physics/brown/pubs/cq1stPaper.pdf
//
// Judith C. Brown and MillerS. Puckette,
// An efficient algorithm for the calculation of a constant Q transform, J.
// Acoust. Soc. Am., Vol. 92, No. 5, November 1992
// see http://www.wellesley.edu/Physics/brown/pubs/effalgV92P2698-P2701.pdf
//
// Benjamin Blankertz,
// The Constant Q Transform
// see
// http://wwwmath1.uni-muenster.de/logik/org/staff/blankertz/constQ/constQ.pdf
//
// mods by p.j.leonard
// 0 - default centered quantized freq.
// 1 -
// 3 - j centered samples
// 4 - original

public class DeltaFFTCQ {

	double q; // Constant Q

	int k; // Number of output bands

	int fftlen; // FFT size

	double[] freqs;

	double[][] qKernel;

	int[][] qKernel_indexes;

	double deltaFFT[];
	
	FFT fft;


	public FFT getFFT() {
		return fft;
	}

	public int getFFTSize() {
		return fftlen;
	}

	public int getNumberOfOutputBands() {
		return k;
	}

	double sampleRate = 44100;

	double minFreq = 100;

	double maxFreq = 3000;

	double binsPerOctave = 12;

	double threshold = 0.001; // Lower number, better quality !!! 0 = best

	double spread = 1.0;

	String kernelsident;

	private double deltaSamples;


	
	public DeltaFFTCQ(double sampleRate, double minFreq, double maxFreq,
			double binsPerOctave) {
		this.sampleRate = sampleRate;
		this.minFreq = minFreq;
		this.maxFreq = maxFreq;
		this.binsPerOctave = binsPerOctave;

		init();
	}

	public DeltaFFTCQ(double sampleRate, double minFreq, double maxFreq,
			double binsPerOctave, double threshold,double  deltaSamples) {
		this.sampleRate = sampleRate;
		this.minFreq = minFreq;
		this.maxFreq = maxFreq;
		this.binsPerOctave = binsPerOctave;
		this.threshold = threshold;
		this.deltaSamples=deltaSamples;
	//	this.spread = spread;
		init();
	}

	

	private void init() {

		// Calculate Constant Q
	//	System.out.println("Spread = " + spread + " Key:" + key);
		
		q = 1.0 / (Math.pow(2, 1.0 / binsPerOctave) - 1.0) / spread;

		// Calculate number of output bins
		k = (int) Math.ceil(binsPerOctave * Math.log(maxFreq / minFreq)
				/ Math.log(2));

		// Calculate length of FFT
		double calc_fftlen = Math.ceil(q * sampleRate / minFreq)+deltaSamples;
		fftlen = (int) Math.pow(2, Math.ceil(Math.log(calc_fftlen)
				/ Math.log(2)));

		// Create FFT object
		fft = new FFT(fftlen);
		deltaFFT=new double[fftlen];
		for (int j=0;j<fftlen/2;j++) {
			double fact=j*2*Math.PI*deltaSamples/fftlen;
			deltaFFT[2*j]=Math.cos(fact);
			deltaFFT[2*j+1]=Math.sin(fact);
		}
		
		
		qKernel = new double[k][];
		qKernel_indexes = new int[k][];
		freqs = new double[k];

		// Calculate Constant Q kernels

		double[] temp = new double[fftlen * 2];
		double[] ctemp = new double[fftlen * 2];
		int[] cindexes = new int[fftlen];

		for (int i = 0; i < k; i++) {
			double[] sKernel = temp;
			// Calculate the frequency of current bin
			freqs[i] = minFreq * Math.pow(2, i / binsPerOctave);

			double len = q * sampleRate / freqs[i];

			// double halflen= len*0.5;
			// window is symmetric around center point of frame
			// calculate second half of the kernel

			for (int j = 0; j < fftlen ; j++) {

				double aa;

				aa = (double) (j) / len;

				if (aa < 1.0) {
					double a = 2.0 * Math.PI * aa;
					double window = 0.5 * (1.0 - Math.cos(a)); // Hanning

					window /= len;

					// Calculate kernel
					double x = 2.0 * Math.PI * freqs[i] * (j) / sampleRate;

					sKernel[j * 2] = window * Math.cos(x);
					sKernel[j * 2 + 1] = -window * Math.sin(x);
				} else {
					sKernel[j * 2] = 0.0;
					sKernel[j * 2 + 1] = 0.0;
				}

			}

			// Perform FFT on kernel

			fft.calc(sKernel, -1);

			// Remove all zeros from kernel to improve performance
			double[] cKernel = ctemp;

			int k = 0;
			for (int j = 0, j2 = sKernel.length - 2; j < sKernel.length / 2; j += 2, j2 -= 2) {
				double absval = Math.sqrt(sKernel[j] * sKernel[j]
						+ sKernel[j + 1] * sKernel[j + 1]);
				absval += Math.sqrt(sKernel[j2] * sKernel[j2] + sKernel[j2 + 1]
						* sKernel[j2 + 1]);
				if (absval > threshold) {
					cindexes[k] = j;
					cKernel[2 * k] = sKernel[j] + sKernel[j2];
					cKernel[2 * k + 1] = sKernel[j + 1] + sKernel[j2 + 1];
					k++;
				}
			}

			sKernel = new double[k * 2];
			int[] indexes = new int[k];

			for (int j = 0; j < k * 2; j++)
				sKernel[j] = cKernel[j];
			for (int j = 0; j < k; j++)
				indexes[j] = cindexes[j];

			// Normalize fft output
			for (int j = 0; j < sKernel.length; j++)
				sKernel[j] /= fftlen;

			// Perform complex conjugate on sKernel
		//	for (int j = 0; j < sKernel.length; j += 2)
		//		sKernel[j] = -sKernel[j];

			qKernel_indexes[i] = indexes;
			qKernel[i] = sKernel;
			
			

		}

		// writeKernels(file); //new File(kernelsident));
	}


	public void calc(double[] buff_in, double[] buff_out) {
		fft.calcReal(buff_in, -1);
		for (int i = 0; i < qKernel.length; i++) {
			double[] kernel = qKernel[i];
			int[] indexes = qKernel_indexes[i];
			double t_r = 0;
			double t_i = 0;
			for (int j = 0, l = 0; j < kernel.length; j += 2, l++) {
				int jj = indexes[l];
				double b_r = buff_in[jj];
				double b_i = buff_in[jj + 1];
				double k_r = kernel[j];
				double k_i = kernel[j + 1];
				// COMPLEX: T += B * K
				t_r += b_r * k_r - b_i * k_i;
				t_i += b_r * k_i + b_i * k_r;
			}
			buff_out[i * 2] = t_r;
			buff_out[i * 2 + 1] = t_i;
		}
	}


	public void calcShifted(double[] buff_in, double[] buff_out) {
		fft.calcReal(buff_in, -1);
		
		
		for (int i=0;i<fftlen/2;i++){
			double t_r=buff_in[2*i];
			double t_i=buff_in[2*i+1];
			buff_in[2*i]= deltaFFT[2*i]*t_r -deltaFFT[2*i+1]*t_i;
			buff_in[2*i+1]= deltaFFT[2*i]*t_i + deltaFFT[2*i+1]*t_r;
		}
		
		buff_in[0]=buff_in[1]=0.0;
		
		for (int i = 0; i < qKernel.length; i++) {
			double[] kernel = qKernel[i];
			int[] indexes = qKernel_indexes[i];
			double t_r = 0;
			double t_i = 0;
			for (int j = 0, l = 0; j < kernel.length; j += 2, l++) {
				int jj = indexes[l];
				double b_r = buff_in[jj];
				double b_i = buff_in[jj + 1];
				double k_r = kernel[j];
				double k_i = kernel[j + 1];
				// COMPLEX: T += B * K
				t_r += b_r * k_r - b_i * k_i;
				t_i += b_r * k_i + b_i * k_r;
			}
			buff_out[i * 2] = t_r;
			buff_out[i * 2 + 1] = t_i;
		}
	}

	
	public int getFFTlength() {
		return fftlen;
	}

}
