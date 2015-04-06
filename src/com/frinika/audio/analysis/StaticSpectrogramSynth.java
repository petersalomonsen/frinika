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

package com.frinika.audio.analysis;

import java.awt.Dimension;
import java.util.Arrays;

import com.frinika.audio.analysis.gui.CursorObserver;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;

/**
 *
 *  An attempt at resynthesis from the spectral data.
 *
 *  Use the AudioProcess interface to grab the sound.
 *
 *  The CursorObserver selects the bin to resynthesize.
 *
 *  Doesn't work that well !!!!!
 *
 * @author pjl
 */
public class StaticSpectrogramSynth implements AudioProcess, CursorObserver,
		SpectrogramDataListener {

	private SpectrumDataBuilder data;

	private int nBins;

	private MagFreq magFreq[];

	int peaks[];

	private OscillatorNode oscNext[];

	OscillatorNode oscBank[];   // Array of osccillators.

    /**
     * Contruct a synth that will attempt to resynthesize using a set of oscillators.
     *
     *
     * @param data   Spectral Data
     */
	public StaticSpectrogramSynth(SpectrumDataBuilder data) {
		this.data = data;
		data.addSizeObserver(this);
	}

    /**
     *
     *
     * @return   The current oscillators
     */
	public OscillatorNode[] getOscillatorBank(){
			return oscBank;
	}

	private final void activateOscillatorsAtChunk(
			long chunkPtr) {

		if (!data.validAt(chunkPtr)) {
			System.out.println(" Data not ready at " + chunkPtr);
			for (OscillatorNode osc:oscBank) {
				osc.active=false;
			}
			return;
		}

		float freqs[] = data.getFreqArray();

		// TODO sort out newing

		float magn[] = data.getMagnitudeAt(chunkPtr);
		float pfreq[] = data.getPhaseFreqAt(chunkPtr);
		float phase[] = data.getPhaseFreqAt(chunkPtr);
		// long sampleTime = data.chunkStartInSamples(chunkPtr);

		int cnt = findPeaks(magn, peaks, 0.0f);

		for (int i = 0; i < cnt; i++) {
			magFreq[i].set(peaks[i], magn[peaks[i]]); 
		}

		Arrays.sort(magFreq, 0, cnt);

		System.out.println(" Found " + cnt + " peaks ");

		Arrays.fill(oscNext, null);

		for (int i = 0; i < cnt; i++) {
			int ifreq = magFreq[i].ifreq;
			if (!isMasked(ifreq, magn[ifreq])) {
				int inear = findOscNear(ifreq);
				if (inear != ifreq)
					swap(inear, ifreq);
				oscBank[ifreq].setNext(pfreq[ifreq], magn[ifreq], phase[ifreq]);
				oscNext[ifreq]=oscBank[ifreq];
			}
		}

		
		for (int i=0;i<nBins;i++) {
			if (oscNext[i] == null && oscBank[i].active)  oscBank[i].silence();
		}
		
		// final Vector<OscillatorNode> tmpNextOsc = activeOsc;
		//
		// synchronized(activeOsc) {
		// activeOsc=tmpOsc;
		// tmpOsc=tmpNextOsc;
		// }

		// OscillatorNode ttt[]=oscPrev;
		// oscPrev = oscNext;
		// oscNext = ttt;
//		for (int i=0;i<nBins;i++) {
//			if (oscBank[i].active()) tmpOsc.add(oscBank[i]);
//		}
//		return tmpOsc;
	}

	private void swap(int inear, int ifreq) {
		OscillatorNode nn=oscBank[inear];
		oscBank[inear]=oscBank[ifreq];
		oscBank[ifreq]=nn;
	}

	private int findOscNear(int ifreq) {
		OscillatorNode osc = oscBank[ifreq];
		if (osc.active()) {
			return ifreq;
		}

		if (ifreq < nBins) {
			osc = oscBank[ifreq + 1];
			if (osc.active()) {
				return ifreq + 1;
			}
		}

		if (ifreq > 0) {
			osc = oscBank[ifreq - 1];
			if (osc != null) {
				return ifreq - 1;
			}
		}
		return ifreq;
	}

	final private boolean isMasked(int ifreq, float f) {
		int i1 = ifreq - 2;
		int i2 = ifreq + 2;

		if (i1 < 0)
			i1 = 0;
		if (i2 >= nBins)
			i2 = nBins - 1;

		for (int i = i1; i <= i2; i++) {
			if (oscNext[i] != null)
				return true;
		}

		return false;
	}

	private final int findPeaks(float a[], int peaks[], float threshold) {

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

	// class AmpComparator implements Comparator<OscillatorNode> {
	//
	// public int compare(OscillatorNode arg0, OscillatorNode arg1) {
	// int i = Double.compare(arg0.amp, arg1.amp);
	// if (i != 0)
	// return i;
	// System.out.println(" magnitudes are the same ");
	// if (arg0.hashCode() > arg1.hashCode())
	// return 1;
	// return -1;
	// }
	//
	// }

	public void close() {
	}

	public void open() {
	}

	/**
	 * Probably best to call this when not running ?
	 * 
	 * @param ptr
	 */
	void setChunkPtr(long ptr) {
		// System.out.println("OscPlayer set frame " + ptr );
		activateOscillatorsAtChunk(ptr);
		System.out.println("OscPlayer set frame " + ptr);
//		for (Oscillator osc : tmpOsc) {
//			System.out.println(osc.toString());
//		}

	}

	public int processAudio(AudioBuffer buffer) {
		// int size = buffer.getSampleCount();
		buffer.makeSilence();
		for (OscillatorNode osc : oscBank) {
			if (osc.active) osc.processAudio(buffer);
		}
		return AUDIO_OK;
	}

	public void notifyCursorChange(int pix,float dmy) {
		setChunkPtr(pix);
	}

	public void notifyMoreDataReady() {

	}

    /**
     *
     * @param d
     */
	public void notifySizeChange(Dimension d) {
		System.out.println(" Synth SIZE SET ");

		nBins = d.height;
		oscNext = new OscillatorNode[nBins];
		magFreq = new MagFreq[nBins];
		oscBank = new OscillatorNode[nBins];
		for (int i = 0; i < nBins; i++) {
			magFreq[i] = new MagFreq(i, 0.0f);
			oscBank[i] = new OscillatorNode();
		}
		peaks = new int[nBins];
	}


	final class MagFreq implements Comparable {
		int ifreq;

		float magn;

		public MagFreq(int i, float f) {
			ifreq = i;
			magn = f;
		}

		final public int compareTo(Object o) {
			return Float.compare(magn, ((MagFreq) o).magn);
		}

		final public void set(int i, float f) {
			ifreq = i;
			magn = f;
		}
	};

}
