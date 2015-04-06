/*
 * Created on Apr 15, 2007
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

import uk.org.toot.audio.core.AudioBuffer;

import com.frinika.global.FrinikaConfig;

public class OscillatorNode implements Oscillator {

	double freq1;

	double freq2;

	double amp1;

	double amp2;

	double phaseRef;

	double phase;

	double dphase1;

	double dphase2;

	public boolean active = false;

	boolean steady = false;

	static final double twoPI = Math.PI * 2.0;

	void start(double freq, double amp, double phaseRef) {
		this.amp1 = 0.0;
		this.amp2 = amp;
		this.freq1 = this.freq2 = freq;
		this.dphase1 = this.dphase2 = 2.0 * Math.PI * freq
				/ FrinikaConfig.sampleRate;
		phase = phaseRef;
		if (phase < 0)
			phase += twoPI;
		active = true;
	}

	public OscillatorNode() {
		active = false;
		steady = false;
	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public double getAmp() {
		return amp2;
	}

	public double getFreq() {
		return freq2;
	}

	public void open() {
		// TODO Auto-generated method stub

	}

	public int processAudio(AudioBuffer buffer) {

		float buff[] = buffer.getChannel(0);
		int n = buffer.getSampleCount();

		if (steady) {
			for (int i = 0; i < n; i++) {
				phase += dphase2;
				if (phase >= twoPI) {
					phase -= twoPI;
				}
				// buff[i] += amp * Math.sin(); // TODO
				buff[i] += amp2 * FloatSinTable.sinFast(phase); // TODO
			}
		} else {
			double ddphase=(dphase2-dphase1)/n;
			double ddamp = (amp2-amp1)/n;
			for (int i = 0; i < n; i++) {
				phase += dphase1;
				dphase1 += ddphase;
				
				if (phase >= twoPI) {
					phase -= twoPI;
				}
				buff[i] += amp1 * FloatSinTable.sinFast(phase); // TODO
				amp1 += ddamp;
			}	
		}
		steady=true;
		active = amp2 != 0;
		return AUDIO_OK;
	}

	void setNext(double freq, double amp, double phaseRef) {
		this.amp2 = amp;
		this.freq2 = freq;
		// this.phaseRef = phaseRef;
		this.dphase2 = 2.0 * Math.PI * freq2 / FrinikaConfig.sampleRate;
		steady=false;
		active =true;
	}

	@Override
	public String toString() {
		return "f:" + freq2 + "  a:" + amp2;
	}

	public boolean active() {
		return active;
	}

	public void silence() {
		this.amp2 = 0;
		steady=false;
		active =true;
	}
}
