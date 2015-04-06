/*
 * Created on Dec 14, 2004
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
package com.frinika.synth.synths.sampler;

import com.frinika.voiceserver.AudioInput;
import com.frinika.voiceserver.VoiceInterrupt;
import com.frinika.global.FrinikaConfig;
import com.frinika.synth.Oscillator;
import com.frinika.synth.Synth;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import javax.sound.sampled.TargetDataLine;

/**
 * Sampler class. Use for capturing inputs and for direct monitoring.
 * @author Peter Johan Salomonsen
 *
 */
public class SamplerOscillator extends Oscillator {
	AudioInput audioInput;
	
	boolean monitoring = false;
	boolean recording = false;
	// If you use external direct monitoring you want to bypass software monitoring
	boolean directMonitoring = false;
	boolean stereo = false;
	
	byte[] inBuffer;
	int recordBufferSize;
	short[] recordBuffer;
	/**
	 * Number of frames in the buffer when starting recording (this should also be added to the latency compensation)
	 */
	int inputSkip = 0;
	
	// Time when monitoring was started in milliseconds
	long monitorStartMillis = 0;
	// Number of frames that has been read from the input
	long inputFramesReadCount = 0;
	
    private RecordProgressListener recordProgressListener;
	
	/**
	 * @param synth
	 */
	public SamplerOscillator(Synth synth) {
		super(synth);
		this.nextVoice = synth.getPostOscillator();
	}

	
	public void startMonitor(final TargetDataLine lineIn, boolean stereo) throws Exception
 	{
		stopMonitor();
        // Allocate as much memory as possible for the recording buffer

		directMonitoring = FrinikaConfig.getDirectMonitoring();
		
		this.stereo = stereo;
		
    	audioInput = new AudioInput(lineIn,FrinikaConfig.sampleRate);
    	audioInput.start();
    	audioInput.getLine().start();
    	monitorStartMillis = System.currentTimeMillis();
    	inputFramesReadCount = 0;
    	// Wait max 1 sec to see if there's data on the line
    	long waitForActiveTimeStart = System.currentTimeMillis();
    	while(audioInput.getLine().available()==0 && System.currentTimeMillis() - waitForActiveTimeStart < 1000)
    		Thread.yield();
    	
    	// If the line is active (data is on the line -> IO is active) then monitoring will start
    	if(audioInput.getLine().available()>0)
    	{        	
    		MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
            int allocate = (int)((memUsage.getMax()-memUsage.getUsed())*(0.67))/4;
            System.out.println("allocate:"+allocate);
            recordBuffer = new short[allocate];
            recordBufferSize = 0;
            
        	synth.getAudioOutput().interruptTransmitter(this, new VoiceInterrupt() {

    			@Override
    			public void doInterrupt() {
    				monitoring = true;
    			}});
    	}
    	else
    	{
    		throw new Exception("Couldn't start IO on the selected line");
    	}		
	}
	
	public void stopMonitor()
	{
		if(monitoring)
		{
			synth.getAudioOutput().interruptTransmitter(this, new VoiceInterrupt() {
	
				@Override
				public void doInterrupt() {
					try { monitoring = false;
					audioInput.stop();
					audioInput = null;
			        } catch(Exception e) {}
				}
			});
		}
	}
	
	/**
	 * Start recording
	 *
	 */
	public void startRecording()
	{
		final long recordingStartFrame = audioInput.getLine().getLongFramePosition(); //((System.currentTimeMillis()-monitorStartMillis) * synth.getAudioOutput().getSampleRate()) / 1000;
		
		synth.getAudioOutput().interruptTransmitter(this, new VoiceInterrupt() {

			@Override
			public void doInterrupt() {
				inputSkip = (int)(recordingStartFrame - inputFramesReadCount);
				
				//System.out.println("input skip:"+inputSkip+" startframe "+recordingStartFrame+" cnt "+inputFramesReadCount);
				recordBufferSize = 0;
				recording = true;
			}
		});
		System.out.println("Start recording");
	}
	
	/**
	 * Stop recording
	 * 
	 * @return
	 */
	public short[][] stopRecording()
	{
		synth.getAudioOutput().interruptTransmitter(this, new VoiceInterrupt() {

			@Override
			public void doInterrupt() {
				recording = false;
			}
		});
		// Wait for interrupt to occur
		while(recording)
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		
		/**
		 * Latency compensation is calculated as voiceserver + os latency + 2 times voiceserverlatency
		 * the latest addition is cause that two times the voiceserver latency is held standby in the
		 * input fillBuffer session
		 */
		//int latencyCompensation = (int)(synth.getAudioOutput().getTotalLatencyAsFrames() * (stereo ? 2 : 1) + ( synth.getAudioOutput().getLatencyAsFrames() *2));
		int latencyCompensation = inputSkip * (stereo ? 2 : 1) ;
		
		// Half size per buffer if stereo 
		short[] bufLeft = new short[(recordBufferSize-latencyCompensation) / (stereo ? 2 : 1)];
		short[] bufRight = null;
		
		if(stereo)
		{
			bufRight = new short[(recordBufferSize-latencyCompensation) / 2];
			// Must split the buffer if stereo
			for(int n=0;n<recordBufferSize-latencyCompensation;n+=2)
			{
				bufLeft[n/2] = recordBuffer[n-latencyCompensation]; 
				bufRight[n/2] = recordBuffer[n-latencyCompensation+1];
			}
		}
		else
			System.arraycopy(recordBuffer,latencyCompensation,bufLeft,0,recordBufferSize-latencyCompensation);
		
		recordBuffer = null;
		
		System.out.println("Stop recording");
		if(this.recordProgressListener!=null)
			recordProgressListener.finished();
		
		return(new short[][] {bufLeft,bufRight});
	}
	
	/* (non-Javadoc)
	 * @see com.petersalomonsen.mystudio.audio.IAudioOutputGenerator#fillBuffer(int, int, float[])
	 */
	public void fillBuffer(int startBufferPos, int endBufferPos, float[] buffer) {
		if(monitoring)
		{
			try
			{
				if(inBuffer == null || inBuffer.length!=buffer.length*2)
					inBuffer = new byte[buffer.length * 2];
									
				// Number of bytes to request from the line
				int numOfBytes = (endBufferPos-startBufferPos)*2;
				
				if(audioInput.getLine().available()>=numOfBytes)
				{
					long t1 = System.nanoTime();
					
                    /**
                     * If we have glitches in the output, this means that there will be more data in the input buffer
                     * than we are able to handle in this fillBuffer session. Unless we compensate here by skipping data from the input,
                     * the software monitoring would have introduced an extra delay. However we don't want to skip this data in the recording,
                     * so what we do is to put all this samples in the recordbuffer, but skip to send them to the preoscillator sample buffer.
                     */ 
                    do
                    {
					    audioInput.getLine().read(inBuffer,0,numOfBytes);
					    inputFramesReadCount+=numOfBytes / 4;
					    
						for(int n=0;n<numOfBytes;)
						{
							short sample = (short)((0xff & inBuffer[n+1]) + ((0xff & inBuffer[n+0]) * 256));
							
							if(recording)
							{
	                                if(recordBufferSize<recordBuffer.length)
	                                    recordBuffer[recordBufferSize++] = sample;
							}						
							
							// In case of external direct monitoring you may want to disable software monitoring
							if(directMonitoring)
							{
								if(stereo)
									n+=2;
								else
									n+=4;
							}
							else
							{
								synth.getPreOscillator().sampleBuffer[startBufferPos+(n/2)] = sample / 32768f;
								
								if(stereo)
								{
									n+=2;
								}
								else
								{
									synth.getPreOscillator().sampleBuffer[startBufferPos+((n/2)+1)] = sample / 32768f;
									n+=4;
								}
							}
							
							// In case of the output glitch this loop will run again - and will overwrite the data from the previous loop (hence - the previous data is skipped)
						}
						/**
						 * Our tolerance concerning input lag will be up two full buffers - if there's more than two full buffers left in the input after this processing,
						 * then read over again to catch up with the input.
						 * 
						 * Why two buffers? If it's just one and you read it, then you will experience that you've read too much from the input and there's nothing to get
						 * for the next fillbuffer. The result will be an input glip (see the else statement below) 
						 */
                    } while(audioInput.getLine().available()>inBuffer.length*2);
				}
				/**
				 * An input glip we have if there's not enough data on the input line according to what we've requested
				 */
				else
				{
					System.out.println("input glip - only "+audioInput.getLine().available()+" / "+(numOfBytes)+" bytes available");
				}
				
                if(this.recordProgressListener!=null)
                		this.recordProgressListener.updateProgress(recordBufferSize);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
            
		}
	}
	public boolean isMonitoring() {
		return monitoring;
	}

	/**
	 * @return
	 */
	public boolean isRecording() {
		return recording;
	}

    public int available() {
        return recordBuffer.length-recordBufferSize;
    }

    public void setRecordProgressListener(RecordProgressListener recordProgressListener) {
        this.recordProgressListener = recordProgressListener;
    }
}
