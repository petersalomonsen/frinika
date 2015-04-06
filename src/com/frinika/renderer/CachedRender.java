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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class CachedRender {

	File cache;
	MidiPacketProvider provider;
	MidiRenderFactory factory;
	MidiPacketsRenderer render = null;
	float samplerate;
	int channels;
	int rendermode;
	
	public CachedRender(File cache, MidiPacketProvider provider, MidiRenderFactory factory, float samplerate, int channels, int rendermode)
	{
		this.cache = cache;
		this.provider = provider;
		this.factory = factory;
		this.samplerate = samplerate;
		this.channels = channels;
		this.rendermode = rendermode;
		
		try {
			raf = new RandomAccessFile(cache, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			raf = null;
		}
		
	}
	
	RandomAccessFile raf;
	
	public void checkFile(int packetsize)
	{
		this.packetsize = packetsize;
		try
		{
			boolean fileok = false;
			if(raf.length() >= 13)
			{
				raf.seek(0);
				byte[] magic = new byte[4];
				raf.read(magic);  
				int version = raf.readInt(); 
				boolean bigendian = raf.readBoolean();
				int read_packetsize = raf.readInt();
				
				if(new String(magic, "lain1").equals("MRCF"))
				if(version == 1)
				if(bigendian == ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN))
				if(read_packetsize == packetsize)
				{
					fileok = true;
				}
			}
			
			if(!fileok)
			{
				raf.setLength(0);
				raf.write(new String("MRCF").getBytes("latin1"));
				raf.writeInt(1);
				raf.writeBoolean(ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN));
				raf.writeInt(packetsize);
			}
		}
		catch(IOException e)
		{
			
		}		
	}
	
	int headersize = 4 + 4 + 1 + 4;
	int packetsize = -1;	
	
	boolean file_checked = false;
	
	byte[] byteBuffer = null;
	FloatBuffer floatBuffer;
	
	boolean dirty_rendered = false;
	
	// Random access packet rendering
	public void getPacket(int index, float[] buffer, int start, int end)
	{		
		if(!file_checked)
		{			
			packetsize = (end - start) * 4;
			packetsize += 1; // Packet Header (status flag), 0=not rendered, 1=rendered, 2=dirty rendered
			packetsize += 4; // Packet Header (midi checksum), 32 bit long, CRC32
			packetsize += 1; // Packet Header (eof flag)
			checkFile(packetsize);
			file_checked = false;
			
			ByteBuffer bytebuffer = ByteBuffer.allocate((end - start)*4).order(ByteOrder.nativeOrder());
			byteBuffer = bytebuffer.array();
			floatBuffer = bytebuffer.asFloatBuffer();
		}
		
		MidiPacket packet = provider.get(index);
		int packet_chekcsum;
		if(packet == null)
			packet_chekcsum = 0;
		else
		    packet_chekcsum = packet.checksum();
		
		boolean eof_packet = packet == null;
		
		long packet_offset = headersize + (((long)packetsize)*index);
		boolean packetfound = false;
		if(rendermode != 2)
		{
		try
		{
			if(raf.length() >= packet_offset+packetsize)
			{
				raf.seek(packet_offset);				
				int status = raf.readByte();
				int read_checksum = raf.readInt();
				boolean eof = raf.readBoolean();
				if(!(eof && render != null))
				if(status != 0) 
				{
					packetfound = (read_checksum == packet_chekcsum);					
				}
			}
			
			if(packetfound)
			{				
				if(render != null)
				{
					render.close();
					render = null;
				}
				
				if(rendermode == 1)
				{
					Arrays.fill(buffer, start, end, 0);
					return;
				}
				
				System.out.println("read packet : " + packet_chekcsum);
				
				raf.read(byteBuffer);
				floatBuffer.position(0);
				floatBuffer.get(buffer, start, end);
				return;
			}
			
		}
		catch(IOException e)
		{			
			e.printStackTrace();
			packetfound = false;
		}
		}
				
		if(render == null)
		{
			if(eof_packet) // Don't start rendering if curent packet is eof
			{
				Arrays.fill(buffer, start, end, 0);
				return;
			}
			render = new MidiPacketsRenderer(factory, samplerate, channels, packet);
			dirty_rendered = index != 0;
		}						
		
		System.out.println("render packet : " + packet_chekcsum);
		
		render.render(packet, buffer, start, end);
		
		try {
						
			if(raf.length() < packet_offset)
			{
				raf.seek(raf.length());
				while(raf.length() < packet_offset)
				{				
					if((packet_offset - raf.length()) > 2048)
						raf.write(emptybuffer);
					else
						raf.write(emptybuffer, 0, (int)(packet_offset - raf.length()));
				}
			}
			
			raf.seek(packet_offset);
			
			
			
			if(dirty_rendered)
				raf.writeByte(2); // Dirty rendered
			else
				raf.writeByte(1); // rendered			
			raf.writeInt(packet_chekcsum);
			raf.writeBoolean(eof_packet);
			
			floatBuffer.position(0);
			floatBuffer.put(buffer, start, end);
			
			raf.write(byteBuffer);
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		
	}
	
	byte[] emptybuffer = new byte[2048];
	{
		Arrays.fill(emptybuffer, (byte)0);
	}
	
	public void close()
	{		
		if(render != null)
		{
			render.close();
		}
		try {
			if(raf != null)	raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
