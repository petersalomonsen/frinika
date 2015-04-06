/*
 * Created on Jan 16, 2007
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

package com.frinika.sequencer.model.audio;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import com.frinika.audio.analysis.constantq.FFTConstantQ;
import com.frinika.audio.analysis.constantq.FreqToBin;
import com.frinika.audio.analysis.gui.SpectrogramPanelOLD;

import com.frinika.global.FrinikaConfig;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.model.AudioPart;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.audio.DoubleDataSource;

public class AudioPartToMidi {

	public static MidiPart process(AudioPart part) throws IOException {
		
		double dt=.01;
		double Fs=FrinikaConfig.sampleRate;
		MidiPart midiPart = new MidiPart();
	
		int maxpixels = 1000;
		
		JFrame frame=new JFrame();
		JProgressBar bar=new JProgressBar(0,maxpixels);
		frame.setContentPane(bar);
		frame.pack();
		frame.setVisible(true);
		
	
		double minF=40.0;
		double maxF=Math.pow(2,7)*minF;
		
		
		int binsPerOctave=12;
		double thresh=0.01;

		int chunksize = (int)(Fs * dt);
		dt=chunksize/Fs;

	//	rasmus.interpreter.sampled.util.FFTConstantQ fftCQ = new rasmus.interpreter.sampled.util.FFTConstantQ(Fs, minF, maxF, binsPerOctave,thresh);
		
		
		FFTConstantQ fftCQ = new FFTConstantQ(Fs,minF , maxF, binsPerOctave,thresh,1.0);
	//	DeltaFFTCQ fftCQ = new DeltaFFTCQ(Fs,minF , maxF, binsPerOctave,thresh,chunksize);
		
		int fftsize = fftCQ.getFFTSize();
		double freq[]=fftCQ.getFreqs();
		
		
		System.out.println(" fftsize/chunkSIze = " + fftsize +"/"+ chunksize);
		
		int nBin = fftCQ.getNumberOfOutputBands();
		double twoPI=2*Math.PI;
		
		// Phase change (radians) of each bin due to chunksize translation
		double dPhaRef[]=new double[nBin];
		for (int i=0;i<nBin;i++) {
			dPhaRef[i]=(twoPI*freq[i]*dt);    // >0
		}
		
		double gbuffer[][] = new double[maxpixels][nBin];
		double dgbuffer[][] = new double[maxpixels][nBin];
		double pFreq[][]   = new double[maxpixels][nBin];
		double fftOut[] = new double[nBin * 2];
		double fftIn[] = new double[fftsize];
		double input[] = new double[fftsize];


		DoubleDataSource reader=null;
		FrinikaSequencer seq=part.getLane().getProject().getSequencer(); 
		long startFrame=(long)((seq.getMicrosecondPosition()*Fs/1000000.0));
		System.out.println("start ="+startFrame);
	//	reader= part.createClipReader();
		reader.seekFrame(startFrame);
		
		double testF=minF*2;
		//reader = new SInDoubleSource(testF,Fs);
		
		int ch = reader.getChannels();

		double buffer[] = new double[chunksize*ch];
		int pix=0;
		double realmaxdB=-10000;
		double maxdB=-40;
		double rangeDb=40;
		double satdB=20;
		
		double peak=0.0;
		double phase[]=new double[nBin];
		
		do {

			
			if (fftsize != chunksize) {
				for (int i = 0; i < fftsize-chunksize; i++)
					input[i] = input[i + chunksize];
			}


			reader.readNextDouble(buffer, 0, chunksize);

		//	System.out.println(reader.getCurrentFrame());
			
			for (int i = fftsize - chunksize,j=0; i < fftsize; i++,j++) {
				if (ch == 2)
					input[i] = buffer[2 * j+1];
				else
					input[i] = buffer[j];
				if (Math.abs(input[i])> peak) peak=Math.abs(input[i]);
			}

			for(int i=0;i< fftsize;i++) {
				fftIn[i]=input[i];
			}
			
			fftCQ.calc(fftIn, fftOut);
		//	fftCQ.calcShifted(fftIn, fftOut);
			//System.out.print(".");

			double vertPtr[] = gbuffer[pix];
			double pfPtr[] = pFreq[pix];

			for (int i = 0; i < nBin; i++) {
				double real=fftOut[2*i];
				double imag=fftOut[2*i+1];
				
				double magn  = Math.sqrt(real*real+imag*imag);  
				double pha   = Math.atan2(imag,real);  // -PI  PI 
				double dpha  =  pha - phase[i]; // +  dPhaRef[i];
				vertPtr[i]=magn;
				phase[i]=pha;
				
				// make it in the range [-PI PI]
				dpha = -( (dPhaRef[i]- dpha+ Math.PI+twoPI)%twoPI - Math.PI); //Math.rint(dpha/twoPI)*twoPI; 
				
			//	if (i == 12) System.out.println(freq[12] + "   " + dpha + "  " + dPhaRef[i]);
				pfPtr[i]= freq[i] + dpha/twoPI/dt;
				
			}
		

			pix++;
			// System.out.println(vertPtr[0]);
			bar.setValue(pix);
		} while (!reader.endOfFile() && pix < maxpixels);


		// Do this before or after log ?

	
		
	
		double dMax=0.0;
		double dMin=0.0;
		
		double state[]=new double[nBin];
		
		
		// 100 Hz resolution
		double halfLife=.01;
		double halfLifeInChunks=halfLife*Fs/chunksize;
		
		// damp^halfLifeInChunksize =0.5
		// log(damp) = log(0.5)/hlins
		// damp = e^(log(0.5)/hlins)
		
		double damp= Math.exp(Math.log(0.5)/halfLifeInChunks);
		double damp2=1.0-damp;
		
		for (int i = 0; i < maxpixels-1; i++) {		
			for (int j = 0; j <nBin; j++) {
				double stateNew=state[j]*damp + gbuffer[i][j]*damp2;
				               
				               
				dgbuffer[i][j]=stateNew-state[j]; //
				dMax= Math.max(dgbuffer[i][j],dMax);
				dMin= Math.min(dgbuffer[i][j],dMin);
				state[j]=stateNew;
			}
		}

		for (int i = 0; i < maxpixels; i++) {		
			for (int j = 0; j < nBin; j++) {
				dgbuffer[i][j] =(dgbuffer[i][j]-dMin)/(dMax-dMin);
			}
		}

		
		for (int i = 0; i < maxpixels; i++) {
			for (int j = 0; j < nBin; j++) {
				double magn=gbuffer[i][j];
				double dB=20*Math.log10(4*magn +1e-15);
				gbuffer[i][j]= dB;
				if (dB> realmaxdB ) realmaxdB=dB;
			}
		}
		
		System.out.println(" real max dB= " + realmaxdB + " peak value =" + peak);
		
		realmaxdB -= satdB;
		for (int i = 0; i < maxpixels; i++) {
			for (int j = 0; j < nBin; j++) {
				gbuffer[i][j]=(gbuffer[i][j]+rangeDb-realmaxdB)/rangeDb; 
			}
		}
			
		
		SpectrogramPanelOLD panel=new SpectrogramPanelOLD();
		panel.setData(gbuffer,pFreq,freq,dt,new FreqToBin(minF,binsPerOctave),0.3,dgbuffer);
		JScrollPane scroll=new JScrollPane(panel);
		frame.setContentPane(scroll);
		frame.pack();
	
//		frame.setSize(scroll.getPreferredSize());
//		frame.setVisible(true);
		return midiPart;
	}

}
