/*
 * Created on Apr 11, 2007
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


// Attempt
package com.frinika.audio.analysis.constantq;


// ATTEMPT INVERSE CONSTANT Q  (not much good)

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;

import com.frinika.audio.analysis.Oscillator;
import com.frinika.audio.analysis.OscillatorNode;
import com.frinika.audio.analysis.SpectrogramDataListener;

public class SpectrogramToWave implements SpectrogramDataListener, Observer {

	public ConstantQSpectrogramDataBuilder data;

	int dataPtr = 0;

	private int nChunk;

	private int nBin;

	Thread buildThread;

	int renderCount;

	boolean rebuild = false;

	TreeSet<Oscillator> activeOscillator = new TreeSet<Oscillator>();
	

	
	public SpectrogramToWave(ConstantQSpectrogramDataBuilder data) {
		this.data = data;
		data.addSizeObserver(this);
	
	}

	int peaks[];

	void doWork() {

		int ptr = 0;

		synchronized (data) {
			int end = data.getChunkRenderedCount();

			for (; renderCount < end; renderCount++) {
				getPeaks(renderCount);
			}
		}
	}


	final TreeSet<OscillatorNode> vertCache = new TreeSet<OscillatorNode>(); //new AmpComparator());

	//public Object oscillators;

	public TreeSet<? extends Oscillator> getPeaks(long chunkPtr) {
		vertCache.clear();
		if (!data.validAt(chunkPtr)) {
			System.out.println(" Data not ready at " + chunkPtr);
			return vertCache;
			
		}
		
		float freqs[] = data.getFreqArray();
		peaks = new int[freqs.length];

		float magn[] = data.getMagnitudeAt(chunkPtr);
		float pfreq[] = data.getPhaseFreqAt(chunkPtr);
		float phase[] = data.getPhaseAt(chunkPtr);
		long sampleTime=data.chunkStartInSamples(chunkPtr);
		
		int cnt = getPeaks(magn, peaks, 0.1f);
		for (int i = 0; i < cnt; i++) {
			int ifreq = peaks[i];
			//vertCache.add(new OscillatorNode(sampleTime,ifreq, freqs[ifreq], magn[ifreq],
			//		pfreq[ifreq], phase[ifreq]));
		}
		return vertCache;
	}

	private int getPeaks(float a[], int peaks[], float threshold) {

		float max = 0.0f;
		int imax = -1;
		int cnt = 0;
		for (int i = 1; i < a.length - 1; i++) {
			if (a[i] < threshold)
				continue;
			if (a[i] >= a[i - 1] && a[i] > a[i + 1]) {
				peaks[cnt++] = i;
			}
		}
		return cnt;

	}

	public void update(Observable arg0, Object arg) {
		dataPtr = (Integer) arg;
	}

	public void notifySizeChange(Dimension d) {
		nChunk = d.width;
		nBin = d.height;
		rebuild = true;
	}

	public void notifyMoreDataReady() {
		doWork();
	}

//	class OscillatorNode implements Oscillator{
//		
//		long sampleTime;
//		int index;
//		double freq;
//		double amp;
//		double pfreq;
//		double phaseRef;
//
//		double phase;
//		double dphase;
//		
//		OscillatorNode(long sampleTime,int index, double freq, double amp, double pfreq,
//				double phaseRef) {
//			this.sampleTime=sampleTime;
//			this.index = index;
//			this.freq = freq;
//			this.amp = amp;
//			this.pfreq = pfreq;
//			this.phaseRef = phaseRef;
//			this.dphase = 2.0*Math.PI*freq/FrinikaConfig.sampleRate;
//		}
//
//		public String toString() {
//			return "" + freq + " " + amp + "" + pfreq;
//		}
//
//		public void close() {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public void open() {
//			// TODO Auto-generated method stub
//			
//		}
//
//		public int processAudio(AudioBuffer buffer) {
//			float buff[]=buffer.getChannel(0);
//			int n=buffer.getSampleCount();
//			for (int i=0;i<n;i++) {
//				buff[i] += amp*Math.sin(phase+=dphase);    // TODO 
//			}
//			return AUDIO_OK;
//		}
//
//	}

//	class AmpComparator implements Comparator<OscillatorNode> {
//
//		public int compare(OscillatorNode arg0, OscillatorNode arg1) {
//			int i = Double.compare(arg0.amp, arg1.amp);
//			if (i != 0)
//				return i;
//			System.out.println(" magnitudes are the same ");
//			if (arg0.hashCode() > arg1.hashCode())
//				return 1;
//			return -1;
//		}
//
//	}

	public TreeSet<? extends Oscillator> getPeaksAtFrame(long framePtr) {
		int chunkPtr=data.getChunkAtFrame(framePtr);
		return getPeaks(chunkPtr);
	}


	
}
