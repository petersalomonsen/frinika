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

import java.awt.Dimension;
import java.io.IOException;


import com.frinika.audio.io.LimitedAudioReader;
import com.frinika.audio.analysis.DataBuilder;
import com.frinika.audio.analysis.SpectrogramDataListener;
import uk.org.toot.audio.core.AudioBuffer;

public class ChunkFeeder extends DataBuilder {
	
	
	
	FFTSpectrumClient client;
	
	
	private int chunkStartInSamples;
	int chunksize;
	int fftsize;
	
	private LimitedAudioReader reader;
	
	int chunkPtr = 0;
	int nFrame;
	private int sizeInChunks;
	private Dimension size;
	private int nBin;
	ChunkReaderProcess process;

	public void setParameters(int chunkSize, int fftsize,LimitedAudioReader reader,ChunkReaderProcess process,FFTSpectrumClient client) {

		abortConstruction();
		this.reader=reader;
		this.process=process;
		this.client=client;
		
		if (chunkSize == this.chunksize && fftsize == this.fftsize)
			return;
	
		nFrame = (int) reader.getEnvelopedLengthInFrames();
		System.out.println(" NFRAME = "+ nFrame);
		if (nFrame == 0 ) {
			
			System.out.println(" Seeting nFrame to 1000000 ");
			nFrame=100000;			
		}
	
		
		this.chunksize = chunkSize;
		this.fftsize = fftsize;
		sizeInChunks = nFrame / chunksize;
		
		System.out.println("SIZE IN CHUNKS = "+ sizeInChunks);
		double dt=chunkSize/reader.getSampleRate();
		process.setParameters(fftsize,process.getSampleRate());
		
		nBin=process.getBinCount();
		
		size = new Dimension(sizeInChunks, nBin);
		client.setSize(sizeInChunks, nBin,process.getFreqArray(),dt);
		
		startConstruction();

	}
	
	protected void doWork() {

		
	
		
		/*
		 * Here the size of arrays changes any user should synchronize with me
		 * here
		 */
	
	

		double fftOut[] = new double[fftsize*2];
		double input[] = new double[fftsize];

		try {
			reader.seekEnvelopeStart(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	//	double testF = minF * 2;
		// reader = new SInDoubleSource(testF,Fs);

		int ch = reader.getChannels();

	//	int nFrames = reader.getLengthInFrames();
		int nRead = 0;

		AudioBuffer buffer = new AudioBuffer("TEMP", ch, chunksize, 44100);
		// double buffer[] = new double[chunksize * ch];

		chunkPtr = 0;  //  -fftsize / chunksize / 2;

	//	int extraChunks = -chunkPtr;

		notifySizeObservers();

		chunkStartInSamples = 0;

		double maxV=0.0;
		do {
			if (Thread.interrupted()) {
				return;
			}
			if (fftsize != chunksize) {
				for (int i = 0; i < fftsize - chunksize; i++)
					input[i] = input[i + chunksize];
			}

			buffer.makeSilence();
			try {
				reader.processAudio(buffer);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			nRead += chunksize;

			// System.out.println(reader.getCurrentFrame());

			float left[] = buffer.getChannel(0);

			for (int i = fftsize - chunksize, j = 0; i < fftsize; i++, j++) {
				// if (ch == 2)
				// input[i] = buffer[2 * j + 1];
				// else
				input[i] = left[j];
			}

			for(int i=0;i<fftsize;i++) fftOut[i]=input[i];
			
			double spectrum[]=process.process(fftOut);

			client.process(spectrum,nBin);
			
			chunkPtr++;
			
			chunkStartInSamples += chunksize;

			
			// } while (!reader.eof() && chunkPtr < sizeInChunks);
		} while (chunkPtr < sizeInChunks);
		System.out.println(" DATA BUILT maqxV " + maxV);
		notifyMoreDataObservers();
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

	
	public long chunkStartInSamples(long chunkPtr) {

		return chunkStartInSamples + chunkPtr * chunksize;
	}

	public int getChunkAtFrame(long framePtr) {

		int chunkPtr = (int) ((framePtr - chunkStartInSamples) / chunksize);

		return chunkPtr;
	}

	public boolean validAt(long chunkPtr2) {
		return chunkPtr2 >= 0 && chunkPtr2 < this.chunkPtr;
	}
}
