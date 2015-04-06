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

package com.frinika.audio.analysis.constantq;

import com.frinika.audio.io.LimitedAudioReader;
import java.awt.Dimension;
import java.io.IOException;

import rasmus.interpreter.sampled.util.FFT;

import com.frinika.audio.analysis.DataBuilder;
import com.frinika.audio.analysis.SpectrogramDataListener;
import com.frinika.audio.analysis.SpectrumDataBuilder;
import com.frinika.audio.analysis.StaticSpectrogramSynth;
import uk.org.toot.audio.core.AudioBuffer;


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
public class ConstantQSpectrogramDataBuilder  extends DataBuilder implements SpectrumDataBuilder  {



	private LimitedAudioReader reader;

	private float[][] magnArray;

	private float[][] phaseArray;

	private float[][] dPhaseFreqHz;

	float freqArray[];

	int chunkPtr = 0;

	private int sizeInChunks;

	private int nBin;

	double dt = .01;

	double Fs;

	int nFrame;

	double minF;

	double maxF;

	int binsPerOctave = 48;

	double thresh = 0.02;

	int chunksize;

	Dimension size;

	double spread;

	private int chunkStartInSamples;
	FFTConstantQ fftCQ;
	
	public ConstantQSpectrogramDataBuilder() {
	}

//	/**
//	 * 
//	 * @param reader
//	 * @param minF
//	 * @param nOctave
//	 * @param binsPerOctave
//	 */
//	ConstantQSpectrogramDataBuilder(AudioReader reader, double minF, double maxF,
//			int binsPerOctave, double thresh, double spread, double dt) {
//
//		setParameters(reader, minF, maxF, binsPerOctave, thresh, spread, dt);
//	}

	public void setParameters(LimitedAudioReader reader, double minF,
			double maxF, int binsPerOctave, double thresh, double spread,
			double dt) {

		abortConstruction();

		this.reader = reader;

		if (minF == this.minF && maxF == this.maxF
				&& binsPerOctave == this.binsPerOctave && thresh == this.thresh
				&& this.spread == spread && this.dt == dt)
			return;

		this.dt = dt;// = .01;
		Fs = reader.getSampleRate(); 


		nFrame = (int) reader.getEnvelopedLengthInFrames();

		this.minF = minF;
		// maxF = Math.pow(2, nOctave) * minF;
		this.maxF = maxF;
		this.binsPerOctave = binsPerOctave;
		this.thresh = thresh;
		this.spread = spread;
		chunksize = (int) (Fs * dt);
		sizeInChunks = nFrame / chunksize;

		startConstruction();

	}


	public void addSizeObserver(SpectrogramDataListener o) {
		sizeObservers.add(o);
	}

	void notifySizeObservers() {
		for (SpectrogramDataListener o : sizeObservers)
			o.notifySizeChange(size);

	}
	
	void notifyMoreDataObservers() {
		for (SpectrogramDataListener o : sizeObservers)
			o.notifyMoreDataReady();

	}

	public int getSizeInChunks() {
		return sizeInChunks;

	}

	public int getChunkRenderedCount() {
		return chunkPtr;
	}

	public int getBinCount() {
		return nBin;
	}

	public float[][] getMagnitude() {
		return magnArray;
	}

	protected void doWork() {

		chunkPtr=-1;   // make invalid
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			System.out.println(" Interrupted before I even started !! ");
			e.printStackTrace();
			return;
		}

		dt = chunksize / Fs;

		fftCQ = new FFTConstantQ(Fs, minF, maxF, binsPerOctave,
				thresh, spread);

		int fftsize = fftCQ.getFFTSize();

		double freq[] = fftCQ.freqs;

		/*
		 * Here the size of arrays changes any user should synchronize with me
		 * here
		 */
		synchronized (this) {

			freqArray = new float[freq.length];
			for (int i = 0; i < freq.length; i++) {
				freqArray[i] = (float) freq[i];
			}

			System.out.println(" fftsize/chunkSIze = " + fftsize + "/"
					+ chunksize);

			nBin = fftCQ.getNumberOfOutputBands();

			size = new Dimension(sizeInChunks, nBin);

			dPhaseFreqHz = new float[sizeInChunks][nBin];

			magnArray = new float[sizeInChunks][nBin];
			phaseArray = new float[sizeInChunks][nBin];
		}

	
		double twoPI = 2 * Math.PI;

		// Phase change (radians) of each bin due to chunksize translation
		double dPhaRef[] = new double[nBin];
		for (int i = 0; i < nBin; i++) {
			dPhaRef[i] = (twoPI * freq[i] * dt); // >0
		}

		double fftOut[] = new double[nBin * 2];
		double fftIn[] = new double[fftsize];
		double input[] = new double[fftsize];

		try {
			reader.seekEnvelopeStart(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		double testF = minF * 2;
		// reader = new SInDoubleSource(testF,Fs);

		int ch = reader.getChannels();

		int nRead=0;
		
		AudioBuffer buffer = new AudioBuffer("TEMP",ch,chunksize,44100);

		chunkPtr = -fftsize / chunksize / 2;

		int extraChunks= -chunkPtr;
		
		notifySizeObservers();

		chunkStartInSamples=0;
		
		do {
			if (Thread.interrupted()) {
				return;
			}
			if (fftsize != chunksize) {
				for (int i = 0; i < fftsize - chunksize; i++)
					input[i] = input[i + chunksize];
			}

			buffer.makeSilence();
			reader.processAudio(buffer);
			nRead += chunksize;
			
			// System.out.println(reader.getCurrentFrame());

			float left[]=buffer.getChannel(0);
			
			for (int i = fftsize - chunksize, j = 0; i < fftsize; i++, j++) {
			//	if (ch == 2)
			//		input[i] = buffer[2 * j + 1];
		//		else
					input[i] = left[j];
			}

			if (chunkPtr < 0) {
				chunkPtr++;
				chunkStartInSamples += chunksize;
				continue;
			}
			for (int i = 0; i < fftsize; i++) {
				fftIn[i] = input[i];
			}

			fftCQ.calc(fftIn, fftOut);

			for (int i = 0; i < nBin; i++) {
				double real = fftOut[2 * i];
				double imag = fftOut[2 * i + 1];

				magnArray[chunkPtr][i] = (float) Math.sqrt(real * real + imag
						* imag);

				phaseArray[chunkPtr][i] = (float) Math.atan2(imag, real); // -PI
																			// PI

				double phaLast;
				if (chunkPtr > 0) {
					phaLast = phaseArray[chunkPtr - 1][i];
				} else {
					phaLast = 0.0;
				}

				double dpha = phaseArray[chunkPtr][i] - phaLast;

				// make it in the range [-PI PI]
				dpha = -((dPhaRef[i] - dpha + Math.PI + twoPI) % twoPI - Math.PI);
				dPhaseFreqHz[chunkPtr][i] = (float) (freq[i] + dpha / twoPI
						/ dt);
			}

			chunkPtr++;
			// System.out.println(vertPtr[0]);
			// bar.setValue(pix);
			if (chunkPtr % 50 == 0) {
				notifyMoreDataObservers();
			}
	//	} while (!reader.eof() && chunkPtr < sizeInChunks);
		}while (chunkPtr < sizeInChunks);
		System.out.println(" DATA BUILT ");
		notifyMoreDataObservers();
	}

	public float[] getFreqArray() {
		return freqArray;
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
		if (phaseArray == null)
			return null;

		// int pix = (int) (framePtr / chunksize);
		if (chunkPtr >= phaseArray.length || chunkPtr < 0)
			return null;
		return phaseArray[(int) chunkPtr];
	}

	public float[] getPhaseFreqAt(long chunkPtr) {
		if (dPhaseFreqHz == null)
			return null;

		// int pix = (int) (framePtr / chunksize);
		if (chunkPtr >= dPhaseFreqHz.length)
			return null;
		return dPhaseFreqHz[(int) chunkPtr];
	}

	public long getLengthInFrames() {
		return reader.getEnvelopedLengthInFrames();
	}

	public long chunkStartInSamples(long chunkPtr) {
	
		return chunkStartInSamples + chunkPtr*chunksize;
	}

	public int getChunkAtFrame(long framePtr) {
		
		int chunkPtr=(int) ((framePtr-chunkStartInSamples)/chunksize);
		
		return chunkPtr;
	}

	public boolean validAt(long chunkPtr2) {
		return chunkPtr2>=0  && chunkPtr2 < this.chunkPtr;
	}

	public StaticSpectrogramSynth getSynth() {
		return  new StaticSpectrogramSynth(this);
	}

	public FFT getFFT() {
		return fftCQ.getFFT();
	}

//	public float[] getSMagnitudeAt(long chunkPtr) {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	public float[][] getSMagnitude() {
//		// TODO Auto-generated method stub
//		return getMagnitude();
//	}


}
