/*
 * Created on 29-May-2006
 *
 * Copyright (c) 2006 P.J.Leonard
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
package com.frinika.sequencer.patchname;

import java.io.Serializable;

public class MyPatch implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int prog;
	public int msb;
	public int lsb;

       
        
	public MyPatch(int prog, int msb, int lsb) {
		this.prog = prog;
		this.msb = msb;
		this.lsb = lsb;
	}
	
    public MyPatch(int hashCode) {
        prog=hashCode&0xff;
        msb=(hashCode&0xff00)/0x100;
        lsb=(hashCode&0xff0000)/0x10000;
}

	

	public boolean equals(Object obj) {
		if(!(obj instanceof MyPatch)) return false;		
		MyPatch c = (MyPatch)obj;
		return c.prog == prog && c.msb == msb && c.lsb == lsb;
	}

	public int hashCode() {
		return prog + msb*0x100 + lsb*0x10000;
	}
	
	
	public String toString() {
		return "prog:"+prog+" msb:"+msb + " lsb:"+lsb;
	}

}
