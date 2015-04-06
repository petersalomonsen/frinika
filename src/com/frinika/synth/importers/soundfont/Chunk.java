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
class Chunk
{
	String name;
	int length;
	FileInputStream fis;
	
	public Chunk(FileInputStream fis, String name) throws Exception
	{
		this.fis = fis;
		byte[] b = new byte[4];

		while(!(new String(b).equals(name)) & fis.available()>0)
		{
			b[0] = b[2];
			b[1] = b[3];
			fis.read(b,2,2);
		}
		this.name = name;
		
		fis.read(b);
		
		length = (b[0] & 0xff)+ 
		((b[1] & 0xff) << 8 ) +
		((b[2] & 0xff) << 16 ) +
		((b[3] & 0xff) << 24 );
	}
	
	public String readString(int length) throws Exception
	{
		byte[] b = new byte[length];
		fis.read(b);
		int n;
		for(n=0;n<length && b[n]!=0;n++);
		return(new String(b,0,n));
	}

	public int readInt16() throws Exception
	{
		byte[] b = new byte[2];
		fis.read(b);
		
		return((b[0] & 0xff)+ 
		((b[1] & 0xff) << 8 ));
	}

	public int readInt32() throws Exception
	{
		byte[] b = new byte[4];
		fis.read(b);
		
		return((b[0] & 0xff)+ 
		((b[1] & 0xff) << 8 ) +
		((b[2] & 0xff) << 16 ) +
		((b[3] & 0xff) << 24 ));
	}
	
	public char readChar() throws Exception
	{
		return((char)fis.read());
	}

	public byte readByte() throws Exception
	{
		return((byte)fis.read());
	}

}
