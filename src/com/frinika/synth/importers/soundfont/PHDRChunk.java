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
public class PHDRChunk extends Chunk {
	static int RECSIZE = 38;
	
	String[] names;
	int[] presetBagNdx;
	int[] preset;
	int[] bank;
	int count;
	
	public PHDRChunk(FileInputStream fis) throws Exception
	{
		super(fis,"phdr");
	
		count = length / RECSIZE;
		names = new String[count];
		
		presetBagNdx = new int[count];
		preset = new int[count];
		bank = new int[count];
		
		for(int n = 0;n<count;n++)
		{
			names[n] = readString(20);
			preset[n] = readInt16();
			bank[n] = readInt16();
			presetBagNdx[n] = readInt16();
//			System.out.println(presetBagNdx[n]+" "+preset[n]+" "+bank[n]+" "+names[n]);
			readString(12);
		}
	}
}
