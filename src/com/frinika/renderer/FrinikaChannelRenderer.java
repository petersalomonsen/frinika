/*
 * Created on 5.3.2007
 *
 * Copyright (c) 2007 Karl Helgason
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

package com.frinika.renderer;

import java.io.File;
import java.util.ArrayList;

import uk.org.toot.audio.core.AudioBuffer;

import com.frinika.project.MidiDeviceDescriptor;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaTrackWrapper;

public class FrinikaChannelRenderer implements Runnable {
	
	public final static long DEFAULT_PACKET_LENGTH = 1000000; // 1 sec
	public final static int PREBUFFER_PACKET_COUNT = 3; // 3 sec
	
	int pipesize;
	int buffersize;
	long  packetlen;
	
	FrinikaDeviceRenderer dev_render;
	int channel;
	ArrayList<FrinikaTrackWrapper> tracks = new ArrayList<FrinikaTrackWrapper>();
	FrinikaMidiPacketProvider packetprovider;
	Thread render_thread;
	CachedRender packet_render;
	
	volatile boolean active = false;
		
	public FrinikaChannelRenderer(FrinikaDeviceRenderer dev_render, int channel)
	{
		this.dev_render = dev_render;
		this.channel = channel;	
		
		packetlen = DEFAULT_PACKET_LENGTH;		
		buffersize = ((int) (44100.0 * (DEFAULT_PACKET_LENGTH/1000000.0))) * 2;
		pipesize = PREBUFFER_PACKET_COUNT;
		circularbuffer = new float[pipesize][buffersize];
				
	}
	
	public void addTrack(FrinikaTrackWrapper track)
	{						
		tracks.add(track);
	}
	
	
	public void beforeStart()
	{			
		packetprovider = new FrinikaMidiPacketProvider(packetlen, dev_render.renderer.seqr, dev_render.renderer.project.getSequence(), tracks);
		
		FrinikaSequence seq = dev_render.renderer.project.getSequence();
		long recordStartTimeInMicros = dev_render.renderer.seqr.getMicrosecondPosition();
		
		process_index = (int) ( recordStartTimeInMicros / DEFAULT_PACKET_LENGTH );
		process_index_bufferpos = ((int) (44100.0 * ((recordStartTimeInMicros % DEFAULT_PACKET_LENGTH)/1000000.0))) * 2; 
		
		current_render_index = (int) ( recordStartTimeInMicros / DEFAULT_PACKET_LENGTH );
		current_index = -1;
		current_next_index = current_render_index;
		
		File projectfile = dev_render.renderer.project.getFile();
		String path = projectfile.getPath();
		if(path.toLowerCase().endsWith(".frinika")) path = path.substring(0, path.length() - 8);
		
		MidiDeviceDescriptor mdesc = dev_render.renderer.project.getMidiDeviceDescriptor(dev_render.dev);
		int d_id = dev_render.renderer.project.getMidiDeviceDescriptors().indexOf(mdesc);
		
		path += ".d" + d_id + "c" + channel + ".rendercache";
		File cachefile = new File(path);
		
		packet_render = new CachedRender(cachefile, packetprovider, (MidiRenderFactory)dev_render.dev, 44100, 2, dev_render.renderer.rendermode);

		active = true;
		render_thread = new Thread(this);
		render_thread.setPriority(Thread.MIN_PRIORITY);
		render_thread.start();
	}
	
	public void beforeStart2()
	{
		// Wait for pipe to be full		
		while(true)
		{
			synchronized (this) {
				if( circularbuffer_avail == pipesize ) 
					return;					
			}		
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}
		}
		
	}
	
	int process_index;
	int process_index_bufferpos;
	float[] process_buffer;
	
	int current_index;
	int current_next_index;
	int current_render_index;

	float[][] circularbuffer;
	int circularbuffer_read_pos = 0;
	int circularbuffer_write_pos = 0;
	int circularbuffer_avail = 0;
		
	public void run()
	{
		while(active)
		{
			boolean pipefull = false;
			synchronized (this) {
				pipefull = circularbuffer_avail == pipesize;					
			}
			if(pipefull)
			{
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
				continue;
			}
			
			float[] buffer = circularbuffer[circularbuffer_write_pos];
			packet_render.getPacket(current_render_index, buffer, 0, buffersize);
			current_render_index++;
			
			synchronized(this)
			{
				circularbuffer_write_pos = (circularbuffer_write_pos + 1) % pipesize;
				circularbuffer_avail++;				
			}			
		}

	}
	
	public void nextBuffer()
	{
		if(process_buffer != null)
		{
			synchronized (this) {
				circularbuffer_avail--;
			}				
			
		}
		
		boolean pipeempty;
		do
		{
			synchronized (this) {
				pipeempty = circularbuffer_avail == 0;					
			}
			if(pipeempty)
			{
				try {
					System.out.println("Render Underflow");
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}				
			}
		}
		while(pipeempty);
		
		current_index = current_next_index;
		current_next_index++;
		float[] buffer = circularbuffer[circularbuffer_read_pos];
		circularbuffer_read_pos = (circularbuffer_read_pos + 1) % pipesize;
		
		process_buffer = buffer;			
	}
	
	public void nextProcessBuffer()
	{
		nextBuffer();
		process_index++;
		process_index_bufferpos = 0;
	}
	
	public void stop()
	{				
		active = false;
		packet_render.close();
	}
	
	public void processAudio(AudioBuffer buffer)	
	{				
        float[] left = buffer.getChannel(0);
        float[] right = buffer.getChannel(1);
        
        int sampleCount = buffer.getSampleCount();
        int writeCount = 0;
        
        if(process_buffer == null) nextBuffer();
        
        while(sampleCount - writeCount != 0)
        {        	
        	if(process_index_bufferpos == buffersize) nextProcessBuffer();
        	
        	int avail = (buffersize - process_index_bufferpos)/2;
        	if(avail > sampleCount - writeCount) avail = sampleCount - writeCount;
       		for (int i = 0; i < avail; i++) {
       			left[writeCount] += process_buffer[process_index_bufferpos++];
       			right[writeCount] += process_buffer[process_index_bufferpos++];
       			writeCount++;        		
			}
        }
	}	
	
}
