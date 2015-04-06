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

import com.frinika.audio.analysis.FFTMagnitude;
import com.frinika.audio.analysis.FFTWorker;
import java.awt.Dimension;
import java.util.Vector;


import com.frinika.audio.analysis.gui.CyclicSpectrogramDataListener;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;


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
public class CyclicBufferFFTSpectrogramDataBuilder 	implements CyclicSpectrumDataBuilder {

	Vector<CyclicSpectrogramDataListener> sizeObservers = new Vector<CyclicSpectrogramDataListener>();

	private AudioProcess reader;

    FFTWorker fftWorker;


    private float [][] data;
	private float[][] magnArray;

    double fftBuffer[][];

	//private float[][] smoothMagnArray; 
 
	//private float[][] phaseArray;

	//private float[][] dPhaseFreqHz;



	int chunkPtr = 0;

	private int sizeInChunks;


	double Fs;

	//int nFrame;

	int chunksize;

	int fftsize;

	double dt;

	Dimension size;

	private int chunkStartInSamples;

	private int totalFramesRendered;

	
	//private double[] dPhaRef;


	private double[] input;

//	private double logMagn[];
	
//	private double twoPI;

	private AudioBuffer buffer;

	boolean abortFlag=false;

	private boolean running=false;

	private Thread runThread=null;

	private Thread abortWaiter;
    private FFTMagnitude fftMagn;
	
	/**
	 * 
	 * @param minF
	 * @param nOctave
	 * @param binsPerOctave
	 */
	public CyclicBufferFFTSpectrogramDataBuilder(AudioProcess reader,
			int bufferSize,double Fs) {
		this.reader = reader;
		this.sizeInChunks = bufferSize;
//		twoPI = 2 * Math.PI;
        fftWorker=new FFTWorker(Fs,true);
        fftMagn=new FFTMagnitude();
        data=new float[1][];
    }

	synchronized public void setParameters(int chunksize, int fftsize,float Fs) {

		
		System.err.println(" AAAA ");
		
		this.Fs=Fs;
		if (chunksize == this.chunksize && fftsize == this.fftsize)	return;		
		
		System.out.println(" ABORT REQUEST "+System.currentTimeMillis());
		
		abortFlag=true;
		abortWaiter=Thread.currentThread();
		
		while(running) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		abortWaiter=null;
		
		System.out.println(" ABORT DONE "+System.currentTimeMillis());

		
		buffer = new AudioBuffer("TEMP", 1, chunksize,Fs);
	

		this.chunksize = chunksize;
		this.fftsize = fftsize;

		dt = chunksize / Fs;

		System.out.println(" RESIZE FFT REQUEST "+System.currentTimeMillis());
		resize();
		System.out.println(" RESIZE FFT DONE "+System.currentTimeMillis());
		
		Runnable runner= new Runnable() {
			public void run() {
				doWork();
				runThread=null;
			}
		};
		
		runThread = new Thread(runner);
		runThread.start();
	}

	
	public void abortConstruction() {

	
	}
	
	
	public void addSizeObserver(CyclicSpectrogramDataListener o) {
		sizeObservers.add(o);
	}

	void notifyMoreDataObservers(float buff[]) {
		for (CyclicSpectrogramDataListener o : sizeObservers)
			o.notifyMoreDataReady(buff);

	}

	public int getSizeInChunks() {
		return sizeInChunks;
	}

	public int getChunkRenderedCount() {
		return totalFramesRendered;
	}

//	public int getBinCount() {
//		return nBin;
//	}

//	public float[][] getMagnitude() {
//		return magnArray;
//	}
//
//
//	public float[][] getSmoothMagnitude() {
//		return smoothMagnArray;
//	}

	synchronized void resize() {
        fftWorker.resize(fftsize);
        
	

		/*
		 * Here the size of arrays changes any user should synchronize with me
		 * here
		 */

        int nBin=fftWorker.getSizeInBins();
        
		for (int i = 0; i < nBin; i++) {
//			freq[i] = (i * Fs / nBin);
//			freqArray[i] = (float) freq[i];

			// System.out.println(" fftsize/chunkSIze = " + fftsize + "/"
			// + chunksize);

			size = new Dimension(sizeInChunks, nBin);

//			dPhaseFreqHz = new float[sizeInChunks][nBin];

			magnArray = new float[sizeInChunks][nBin];
            fftBuffer = new double[sizeInChunks][fftsize];

//			smoothMagnArray = new float[sizeInChunks][nBin];
//			logMagn = new double[nBin*2];
//			phaseArray = new float[sizeInChunks][nBin];
		}

	
		// Phase change (radians) of each bin due to chunksize translation
//		dPhaRef = new double[nBin];
//		for (int i = 0; i < nBin; i++) {
//			dPhaRef[i] = (twoPI * freq[i] * dt); // >0
//		}

	
		input = new double[fftsize];
		
		System.out.println(" Resized " + fftsize);


	}

	protected void doWork() {

		running =true;
		abortFlag=false;
		chunkPtr = 0; // make invalid
	
	//	int nRead = 0;

		chunkStartInSamples = 0;
	//	float phaLast[] = phaseArray[0];

		
		totalFramesRendered = 0;
	//	notifySizeObservers();
		
		do {
			
			if (abortFlag) break;
			if (fftsize != chunksize) {
				for (int i = 0; i < fftsize - chunksize; i++)
					input[i] = input[i + chunksize];
			}

			buffer.makeSilence();                    
            reader.processAudio(buffer);

			float left[] = buffer.getChannel(0);

			// System.out.println(" mm =" + left[0]);

			for (int i = fftsize - chunksize, j = 0; i < fftsize; i++, j++) {
				// if (ch == 2)
				// input[i] = buffer[2 * j + 1];
				// else
				input[i] = left[j];
			}

			if (chunkPtr < 0) {
				chunkPtr++;
				chunkStartInSamples += chunksize;
				continue;
			}


            fftWorker.process(input,fftBuffer[chunkPtr]);

            data[0]=magnArray[chunkPtr];
            fftMagn.getData(data, fftBuffer[chunkPtr]);
       
			// System.out.println(" maqxV " + maxV);

			notifyMoreDataObservers(magnArray[chunkPtr]);

			chunkPtr++;
			totalFramesRendered++;
			if (chunkPtr >= sizeInChunks)
				chunkPtr = 0;
			// System.out.println(vertPtr[0]);
			// bar.setValue(pix);
			// if (chunkPtr % 50 == 0) {

			// }
			// nFrame = (int) reader.getLengthInFrames();
			// } while (!reader.eof() && chunkPtr < sizeInChunks);
		} while (true);

		running=false;
		abortFlag=false;
		if (abortWaiter != null) abortWaiter.interrupt();
		System.out.println(" ABORTED ");

	}

	public float[] getFreqArray() {
		return fftWorker.getFreqArray();
	}

	public float[] getMagnitudeAt(long chunkPtr) {
		if (magnArray == null)
			return null;

		// int pix = (int) (framePtr / chunksize);
		if (chunkPtr >= magnArray.length || chunkPtr < 0)
			return null;
		return magnArray[(int) chunkPtr];
	}
//
//	public float[] getPhaseAt(long chunkPtr) {
//		if (phaseArray == null)
//			return null;
//
//		// int pix = (int) (framePtr / chunksize);
//		if (chunkPtr >= phaseArray.length || chunkPtr < 0)
//			return null;
//		return phaseArray[(int) chunkPtr];
//	}
//
//	public float[] getPhaseFreqAt(long chunkPtr) {
//		if (dPhaseFreqHz == null)
//			return null;
//
//		// int pix = (int) (framePtr / chunksize);
//		if (chunkPtr >= dPhaseFreqHz.length)
//			return null;
//		return dPhaseFreqHz[(int) chunkPtr];
//	}

	// public long getLengthInFrames() {
	// return reader.getLengthInFrames();
	// }

	public long chunkStartInSamples(long chunkPtr) {

		return chunkStartInSamples + chunkPtr * chunksize;
	}

	public int getChunkAtFrame(long framePtr) {

		int chunkPtr1 = (int) ((framePtr - chunkStartInSamples) / chunksize);

		return chunkPtr1;
	}

	public boolean validAt(long chunkPtr2) {
		return chunkPtr2 >= 0 && chunkPtr2 < this.chunkPtr;
	}

	public double getSampleRate() {
		// TODO Auto-generated method stub
		return Fs;
	}

    public int getBinCount() {
        return fftWorker.getSizeInBins(); //throw new UnsupportedOperationException("Not supported yet.");
    }

    public float[][] getMagnitude() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
