/*
 * Created on Feb 14, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

import com.frinika.util.math.MyMath;

public class PeakAnalyst {
	double peakShort;

	double peakLong;

	double decayShort;

	double decayLong;

	double riseShort;

	double riseLong;

	double decayShort1;

	double decayLong1;

	double riseShort1;

	double riseLong1;

	double riseInf;

	double decayInf;

	double riseInf1;

	double decayInf1;

	double average;

	double noiseLevel;

	// double hsyterFact; //
	// boolean hold;
	double rate;

	int frameSize;

	Thread pulseWaitThread;

	int pulseTimeInSamples;

	long sampleCount;

	long pulseCount;

	static PeakAnalyst the;

	/**
	 * 
	 * @param feed
	 *            FramedFeed
	 * @param shortT
	 *            double decay time to smooth out freq time scale stuff.
	 * @param longT
	 *            double decay Time to suss out pulses in the music.
	 */

	static public PeakAnalyst the() {
		return the;
	}

//	public PeakAnalyst(FramedFeed feed) {
	public PeakAnalyst() {
		assert (the == null);
		the = this;

		//this.rate = feed.getSampleRate();

	//	feed.addFramedFeedListener(this);

		this.decayShort = MyMath.halfLifeToLambda(this.rate / 100);
		this.riseShort = MyMath.halfLifeToLambda(this.rate / 500);
		this.decayLong = MyMath.halfLifeToLambda(this.rate * 0.2);
		this.riseLong = MyMath.halfLifeToLambda(this.rate * 0.02);
		this.decayInf = MyMath.halfLifeToLambda(this.rate * 5);
		this.riseInf = MyMath.halfLifeToLambda(this.rate * 5);

		/*
		 * System.out.println( decayShort); System.out.println( riseShort);
		 * System.out.println( decayLong); System.out.println( riseLong);
		 * System.out.println( decayInf); System.out.println( riseInf);
		 */

		this.decayShort1 = 1.0 - this.decayShort;
		this.riseShort1 = 1.0 - this.riseShort;

		this.decayLong1 = 1.0 - this.decayLong;
		this.riseLong1 = 1.0 - this.riseLong;

		this.decayInf1 = 1.0 - this.decayInf;
		this.riseInf1 = 1.0 - this.riseInf;

		//this.frameSize = feed.getWindowSize();

	}


	public void fireNewFramedFeedData(short[] v) {
		
		int n = v.length;
		for (int i = 0; i < n; i++) {
			this.sampleCount++;
			double val = Math.abs(v[i]);

			if (val < this.peakShort)
				this.peakShort = this.peakShort * this.decayShort + val
						* this.decayShort1;
			else
				this.peakShort = this.peakShort * this.riseShort + val
						* this.riseShort1;

			if (this.peakShort < this.peakLong)
				this.peakLong = this.peakLong * this.decayLong + this.peakShort
						* this.decayLong1;
			else
				this.peakLong = this.peakLong * this.riseLong + this.peakShort
						* this.riseLong1;

			if (this.peakLong < this.average)
				this.average = this.average * this.decayInf + this.peakLong
						* this.decayInf1;
			else
				this.average = this.average * this.riseInf + this.peakLong
						* this.riseInf1;

			if (this.peakLong > this.average && this.pulseWaitThread != null) {
				this.pulseCount = this.sampleCount;
				this.pulseWaitThread.interrupt();
				this.pulseWaitThread = null;
			}
		}
		// System.out.println(peakShort + " " + peakLong + " " + average);
	}

	public synchronized long waitForPulse(long milliMax) {
		assert (this.pulseWaitThread == null);
		this.pulseWaitThread = Thread.currentThread();
		try {
			wait(milliMax);
			System.out.println(" waitForPulse timed out");
			this.pulseWaitThread = null;
		} catch (InterruptedException ex) {
			this.pulseWaitThread = null;
			return (long) ((this.pulseCount * 1000.0) / this.rate);
		}
		return -1;
	}

}
