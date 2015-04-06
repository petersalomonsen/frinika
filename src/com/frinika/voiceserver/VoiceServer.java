/*
 * Created on Feb 10, 2005
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.voiceserver;

import java.util.Vector;

import javax.swing.JFrame;

import com.frinika.project.FrinikaAudioSystem;
import com.frinika.global.FrinikaConfig;

/**
 * The VoiceServer terminates the audio output of all voices, by polling them sequentially for small buffers
 * of data. The buffer size also determines the latency of the server, thus also for all the VoiceGenerators 
 * (softsynths) it is hosting. The VoiceServer is an abstract class which should be extended and integrated
 * with a sound hardware interface. The voiceserver however provides all functionality needed for timing and
 * synchronization of the voice generators.
 * 
 * The VoiceServer has a realtime and a non-realtime mode. Realtime mode is used when playing live - and timing
 * mechanisms has to be accurate. Non realtime mode is used when creating a rendered version of a sequence of
 * sound. For example when exporting a song to a wav file.
 * 
 * Routing mechanisms are available using Voice.nextVoice
 * @author Peter Johan Salomonsen
 * 
 * PJL - changed call to FrinikaConfig.getOSLatency() to FrinikaAUdioSYstem.getOutputLatency()
 */

public abstract class VoiceServer 
{
    private int bufferSize = 512; // Float buffer size - adjusts automatically - do not modify
    private int sampleRate = (int) FrinikaConfig.sampleRate;
    private long audioStartTime = System.nanoTime();
    private long frameBufferPos = 0;
    private boolean isRealtime = true;
    
    protected Vector<Voice> audioOutputGenerators = new Vector<Voice>();
    Vector<Voice> removedTransmitters = new Vector<Voice>();
    Vector<Voice> addedTransmitters = new Vector<Voice>();

    /**
     * Returns the current sample rate
     * @return samplerate
     */
    public final int getSampleRate()
    {
        return(sampleRate);
    }

    /**
     * Change current sample rate
     * @param sampleRate
     */
    public final void setSampleRate(int sampleRate)
    {
        this.sampleRate = sampleRate;
    }

    /**
     * @return Returns the bufferSize in number of frames
     */
    public final int getBufferSize() {
        return bufferSize;
    }

    /**
     * Get position of where in frame to start a generator
     * 
     * PJL made public so AudioRecording can see the framePos 
     * @return
     */
    private final long getFramePos()
    {       
        return((long) (  
                ( (System.nanoTime() - audioStartTime) *
                        (sampleRate / 1000000000.0) )
                        ) );    
    }
    
    /**
     * Update framebuffer pos for each frame
     *
     */
    private final void updateFrameBufferPos()
    {
        frameBufferPos = getFramePos()-(getBufferSize()/2);        
    }
        
    /**
     * Used by synths to add a new generator (midi note)
     * @param transmitter - the generator to add
     */
    public final void addTransmitter(Voice transmitter)
    {
        if(isRealtime)
            transmitter.startFramePos = getFramePos();
        addedTransmitters.add(transmitter);
    }
    
    /**
     * Used by synths to trigger a change in an existing generator. Ie. a release of a midi note.
     * 
     * @param transmitter
     * @param interrupt
     */
    public final void interruptTransmitter(Voice transmitter, VoiceInterrupt interrupt)
    {
        if(isRealtime)
            interrupt.interruptFramePos = getFramePos();
        transmitter.interrupts.add(interrupt);
    }
    
    /**
     * Called by the generator itself when it knows that it has finished its processing.
     * @param transmitter
     */
    public final void removeTransmitter(Voice transmitter)
    {
        removedTransmitters.add(transmitter);
    }
    
    /**
     * Before each frame generator tables must be updated. Remove transmitters that are
     * finished, and add pending.
     *
     */
    private final void updateGenerators()
    {
        while(removedTransmitters.size()>0)
            audioOutputGenerators.remove(removedTransmitters.remove(0));
        
        while(addedTransmitters.size()>0)
        {
            Voice transmitter = addedTransmitters.remove(0);
            if(transmitter.nextVoice!=null)
            {
                int nIndex = audioOutputGenerators.indexOf(transmitter.nextVoice);
                if(nIndex>-1)
                    audioOutputGenerators.add(nIndex,transmitter);
                else
                    audioOutputGenerators.add(transmitter);
            }
            else
                audioOutputGenerators.add(transmitter);            
        }
    }
    
    /**
     * Read a frame into the given buffer of floats
     * @param floatBuffer
     */
    private final void floatRead(float[] floatBuffer)
    {
        bufferSize = floatBuffer.length;
        updateGenerators();
        
        for(Voice audioGen : audioOutputGenerators)
        {
            int bufferPos = 0;
            if(audioGen.startFramePos > 0)  // Don't need this on wav export
            {   
                bufferPos = (int)(2 * (audioGen.startFramePos - frameBufferPos));
                if(bufferPos < 0)
                    bufferPos = 0;
            }
            
            if(bufferPos<floatBuffer.length)
            {
                int endBufferPos = 0;
                
                while(audioGen.interrupts.size()>0)
                {
                    VoiceInterrupt interrupt = audioGen.interrupts.get(0);
                    if(interrupt.interruptFramePos > 0) 
                    {
                        endBufferPos = (int)(2 * (interrupt.interruptFramePos - frameBufferPos));
                        if(endBufferPos < 0)
                            endBufferPos = 0; 
                    }
                    
                    if(endBufferPos<=floatBuffer.length)
                    {
                        audioGen.fillBuffer(bufferPos,endBufferPos,floatBuffer);
                        interrupt.doInterrupt();
                        bufferPos=endBufferPos;
                        audioGen.interrupts.remove(0);
                    }
                    else
                        break;
                }

                audioGen.fillBuffer(bufferPos,floatBuffer.length,floatBuffer);
                audioGen.startFramePos = 0;
            }
        }
        
        processFinalOutput(floatBuffer);
    }
    
    
    /**
     * Override this method to add processing to the final output
     * @param buffer
     */
    public void processFinalOutput(float[] buffer) {
		
	}

	/**
     * Read a frame into the given buffers of bytes, and floats
     * @param outBuffer
     * @param floatBuffer
     */
    private final void byteRead(byte[] outBuffer, float[] floatBuffer)
    {               
        floatRead(floatBuffer);

        for(int n = 0;n<outBuffer.length;)
        {                   
            //Left
            float floatSample = floatBuffer[n/2];
            short sample;

            if(floatSample>=1.0f)
                sample=0x7fff;
            else if(floatSample<=-1.0f)
                sample=-0x8000;
            else
                sample = (short)(floatSample*0x8000);

            outBuffer[n++] = (byte)((sample & 0xff00) >> 8);
            outBuffer[n++] =(byte)(sample & 0xff);

            //Right

            floatSample = floatBuffer[n/2];
            
            if(floatSample>=1.0f)
                sample=0x7fff;
            else if(floatSample<=-1.0f)
                sample=-0x8000;
            else
                sample = (short)(floatSample*0x8000);

            outBuffer[n++] = (byte)((sample & 0xff00) >> 8);
            outBuffer[n++] =(byte)(sample & 0xff);
        }
    }   

    /**
     * Read a frame into the given buffer of floats
     * @param floatBuffer
     */
    public final void read(float[] floatBuffer)
    {
        if(isRealtime)
        {
            updateFrameBufferPos();
            floatRead(floatBuffer);
        }
        else
        	floatRead(floatBuffer);
    }

    /**
     * Read a frame into the given buffers of bytes, and floats
     * @param outBuffer
     * @param floatBuffer
     */
    public final void read(byte[] outBuffer, float[] floatBuffer)
    {               
        if(isRealtime)
        {   
            updateFrameBufferPos();
            byteRead(outBuffer,floatBuffer);
        }
    }

    /**
     * Read a frame into the given buffers of bytes, and floats. Used in non realtime mode
     * i.e. when exporting audio to a file.
     * @param outBuffer
     * @param floatBuffer
     */
    public final void readNonRealtime(byte[] outBuffer, float[] floatBuffer)
    {               
        if(!isRealtime)
            byteRead(outBuffer,floatBuffer);
    }

    public final void realtimeOn()
    {
        isRealtime=true;
    }
    
    public final void realtimeOff()
    {
        isRealtime=false;
    }
    
    public abstract void configureAudioOutput(JFrame frame);

    /**
     * Returns the latency in microseconds
     * @return
     */
	public final long getLatency() {
		return (1000000L * (bufferSize/2)) / sampleRate;
	}

    /**
     * Returns the latency in frames
     * @return
     */
	public final int getLatencyAsFrames() {
		return (bufferSize/2);
	}

	/**
	 * Returns the voiceServer latency plus the operating system latency in microseconds
	 * @return
	 */
	public final long getTotalLatency()
	{
	//	return getLatency()+(FrinikaConfig.getOSLatencyMillis()*1000);
		return (long) (getLatency()+(FrinikaAudioSystem.getAudioServer().getOutputLatencyMillis()*1000));
	}
	
	/**
	 * Returns the total latency in frames
	 * @return
	 */
	public final int getTotalLatencyAsFrames()
	{
		return (int)(getSampleRate()*(getTotalLatency())/1000000);
	}
}
