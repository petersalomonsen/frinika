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
public class IMODChunk extends Chunk {
	static int RECSIZE = 10;
	int count;
	
	int[] sfModSrcOper;
	int[] sfModDestOper;
	int[] modAmount;
	int[] sfModAmtSrcOper;
	int[] sfModTransOper;
	
	public IMODChunk(FileInputStream fis) throws Exception
	{
		super(fis,"imod");
	
		count = length / RECSIZE;
		
		sfModSrcOper = new int[count];
		sfModDestOper = new int[count];
		modAmount = new int[count];
		sfModAmtSrcOper = new int[count];
		sfModTransOper = new int[count];
		
		for(int n = 0;n<count;n++)
		{
			sfModSrcOper[n] = readInt16();
			//System.out.println("src:"+Integer.toHexString(sfModSrcOper[n]));
			sfModDestOper[n] = readInt16();
			//System.out.println("dest:"+Integer.toHexString(sfModDestOper[n]));
			modAmount[n] = readInt16();
			//System.out.println("amount:"+Integer.toHexString(modAmount[n]));
			sfModAmtSrcOper[n] = readInt16();
			//System.out.println("amtsrcoper:"+Integer.toHexString(sfModAmtSrcOper[n]));
			sfModTransOper[n] = readInt16();
			//System.out.println("amttransoper:"+Integer.toHexString(sfModTransOper[n]));
		}
	}
}
