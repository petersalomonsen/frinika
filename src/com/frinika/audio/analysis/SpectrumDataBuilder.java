/*
 * Created on 21 Dec 2007
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

package com.frinika.audio.analysis;


import rasmus.interpreter.sampled.util.FFT;


/*
 * Interface for spectrum analayis classes.
 * 
 * 
 * SpectrumData builders maintain a set 2D arrays of bin (frequency) and chunk (time)
 * 
 */
public interface SpectrumDataBuilder {

	
	// 
	void addSizeObserver(SpectrogramDataListener  synth);

	// check there is valid data at chunkPtr
	boolean validAt(long chunkPtr);

	// get the bin frequencies
	float[] getFreqArray();

	// get the magnitude at this chunk
	float[] getMagnitudeAt(long chunkPtr);
	//float[] getSMagnitudeAt(long chunkPtr);

	// get the frequency calculated using dPhase/dt  at this chunk
	float[] getPhaseFreqAt(long chunkPtr);

	
	int getChunkRenderedCount();

	int getBinCount();

	int getSizeInChunks();

	// get whole magnitude array
	float[][] getMagnitude();

	void dispose();

	StaticSpectrogramSynth getSynth();

	FFT getFFT();

	//float[][] getSMagnitude();

	

}
