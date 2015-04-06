/*
 * Created on Jan 23, 2007
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

package com.frinika.audio.analysis;

import com.frinika.sequencer.model.audio.DoubleDataSource;

public class SInDoubleSource implements DoubleDataSource {

	long pos;
	private double fact;
	
	public SInDoubleSource(double freq,double fs){
		this.fact=2*Math.PI*freq/fs;
		
	}
	public int getChannels() {		
		return 1;
	}

	public void readNextDouble(double[] buffer, int offSet, int nFrame) {
		for(int i=0,j=offSet;i<nFrame;j++,i++) {
			buffer[j]=Math.sin(fact*pos++);
		}
	}

	public void seekFrame(long pos) {
		this.pos=pos;		
	}

	public boolean endOfFile() { return false; }
	public long getCurrentFrame() {
		// TODO Auto-generated method stub
		return 0;
	}
	public long getLengthInFrames() {
		// TODO Auto-generated method stub
		return 0;
	}
}
