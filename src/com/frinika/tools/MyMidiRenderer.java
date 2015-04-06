/*
 * Created on Oct 3, 2004
 *
 * Copyright (c) 2005 Peter Johan Salomonsen (http://www.petersalomonsen.com)
 * 
 * http://www.frinika.com
 * 
 * This file is part of Frinika.
 * 
 * Frinika is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import uk.org.toot.audio.core.AudioBuffer;
import uk.org.toot.audio.core.AudioProcess;
import uk.org.toot.audio.mixer.AudioMixer;

import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.FrinikaTrackWrapper;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class MyMidiRenderer extends InputStream{
	long tickPosition=0;
	double ticksPerSecond;
	
	AudioMixer mixer;
	double startTimeInMicros; // PJL 
	FrinikaSequence sequence;
	FrinikaSequencer sequencer;
	double samplePos = 0;
	double tickSamples = 0;
	float sampleRate;
	
    int available;
    
	byte[] buffer;
	
	HashMap<FrinikaTrackWrapper,Integer> trackIndex = new HashMap<FrinikaTrackWrapper,Integer>();
	
	int readpos = 0;
	byte[] byteBuffer = null; 

	public MyMidiRenderer(AudioMixer mixer, FrinikaSequencer sequencer, long startTick, int ticks,float sampleRate)
	{
		this.mixer = mixer;
		this.sampleRate = sampleRate;
		this.sequence = (FrinikaSequence)sequencer.getSequence();
		this.sequencer = sequencer;
		this.tickPosition = startTick;
		sequencer.setTickPosition(startTick);
		startTimeInMicros=sequencer.getMicrosecondPosition();
		
		// TODO TEMPOLIST
        this.available =(int)(getNumberOfSamples(ticks) * 4);
		
        
        tickSamples = getNumberOfSamples(1);
		
		AudioProcess audioProcess = new AudioProcess() {

			public void close() {
				// TODO Auto-generated method stub
				
			}

			public void open() {
				// TODO Auto-generated method stub
				
			}

			public int processAudio(AudioBuffer buffer) {
				
				if(byteBuffer == null) 
					byteBuffer = new byte[buffer.getSampleCount()*2*2];
				/**
				 * Convert floats to bytes - this is for export wav
				 */
				int i = 0;
				for(int n = 0;n<buffer.getSampleCount();n++)
		        {                   
		            //Left
		            float floatSample = buffer.getChannel(0)[n];
		            short sample;

		            if(floatSample>=1.0f)
		                sample=0x7fff;
		            else if(floatSample<=-1.0f)
		                sample=-0x8000;
		            else
		                sample = (short)(floatSample*0x8000);

		            byteBuffer[i++] = (byte)((sample & 0xff00) >> 8);
		            byteBuffer[i++] = (byte)(sample & 0xff);

		            //Right

		            floatSample = buffer.getChannel(1)[n];
		            
		            if(floatSample>=1.0f)
		                sample=0x7fff;
		            else if(floatSample<=-1.0f)
		                sample=-0x8000;
		            else
		                sample = (short)(floatSample*0x8000);

		            byteBuffer[i++] = (byte)((sample & 0xff00) >> 8);
		            byteBuffer[i++] = (byte)(sample & 0xff);
		        }				
		        return AUDIO_OK;
			}
			
			
		};
		// Replace mixer output process
		mixer.getMainBus().setOutputProcess(audioProcess);

	}

    @Override
    public int available() throws IOException {
        return available;
    }

    /** 
     * TODO fix for tempo changes
     * 
     * @param ticks
     * @return
     */
    double getNumberOfSamples(int ticks)
	{
		double ticksPerSecond = (sequence.getResolution() * sequencer.getTempoInBPM()) / 60.0;
		double seconds = ticks / ticksPerSecond;
		return(seconds * sampleRate); 
	}
	
	void fillBuffer()
	{
		while (byteBuffer == null) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
                        mixer.setEnabled(true);  // fix infinite loop problem PJL
			mixer.work(-1); // TODO what is the buffer size here
		}
		for(int n=0;n<buffer.length;n++)
		{
			if(readpos == byteBuffer.length)
			{
				mixer.work(-1);			
				readpos = 0;
			}
			buffer[n] = byteBuffer[readpos++];
		}
	}
	
	int bufferPos = 0;
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if(bufferPos == 0)
		{
			
			
			double newSamplePos=samplePos+tickSamples;
			int bufferSize = (int)(newSamplePos)-(int)(samplePos);
			samplePos = newSamplePos;

			if(buffer == null || buffer.length != bufferSize*4)
				buffer = new byte[bufferSize*4];
					
			// This has to be done for each tick in case of tempo changes
			tickSamples = getNumberOfSamples(1);	
			//nextTick();
			sequencer.nonRealtimeNextTick();
			
			fillBuffer();
			
		}

		int ret = 0xff & buffer[bufferPos++];
		
		if(bufferPos == buffer.length)
			bufferPos = 0;
	
        available--;
        
		return(ret);
	}	
	
	public int read(byte[] b, int off, int len) throws IOException {
		
		for (int i = off; i < len; i++) {
			
			if(available == 0) return i - off;					
			if(bufferPos == 0)
			{
						
				double newSamplePos=samplePos+tickSamples;
				int bufferSize = (int)(newSamplePos)-(int)(samplePos);
				samplePos = newSamplePos;

				if(buffer == null || buffer.length != bufferSize*4)
					buffer = new byte[bufferSize*4];
			
				// This has to be done for each tick in case of tempo changes
				tickSamples = getNumberOfSamples(1);	
				//nextTick();
				sequencer.nonRealtimeNextTick();
			
				fillBuffer();
			
			}
			b[i] = buffer[bufferPos++];		
			if(bufferPos == buffer.length)
				bufferPos = 0;	
        	available--;        
		}
        
		return len;				
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/**
	 * PJL
	 * @return start position of rendering in microseconds
	 */
	public double getStartTimeInMicros() {
		return startTimeInMicros;
	}
}
