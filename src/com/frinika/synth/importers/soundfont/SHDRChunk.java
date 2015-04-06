/*
 * Created on Sep 26, 2004
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
package com.frinika.synth.importers.soundfont;
import java.io.FileInputStream;

/**
 * @author Peter Johan Salomonsen
 *
 */
public class SHDRChunk extends Chunk {
	static int RECSIZE = 46;
	
	short[][] samples;
	
	String[] names;
	int[] sampleStart;
	int[] sampleEnd;
	int[] sampleStartLoop;
	int[] sampleEndLoop;
	int[] sampleRate;
	byte[] originalPitch;
	char[] pitchCorrection;
	int[] sampleLink;
	int[] sfSampleType;
	
	int count;
	
	public SHDRChunk(FileInputStream fis) throws Exception
	{
		super(fis,"shdr");

		count = length / RECSIZE;
		samples = new short[count][];
		names = new String[count];
		sampleStart = new int[count];
		sampleEnd = new int[count];
		sampleStartLoop = new int[count];
		sampleEndLoop = new int[count];
		sampleRate = new int[count];
		originalPitch = new byte[count];
		pitchCorrection = new char[count];
		sampleLink = new int[count];
		sfSampleType = new int[count];
		
		for(int n = 0;n<count;n++)
		{
			names[n] = readString(20);
			sampleStart[n] = readInt32();
			sampleEnd[n] = readInt32();
			sampleStartLoop[n] = readInt32();
			sampleEndLoop[n] = readInt32();
			sampleRate[n] = readInt32();
			originalPitch[n] = readByte();
			pitchCorrection[n] = readChar();
			sampleLink[n] = readInt16();
			sfSampleType[n] = readInt16();
		}
	}	
}
