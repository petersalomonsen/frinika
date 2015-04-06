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

/**
 *  A node in a patch tree
 * 
 *  Patches can be defined in groups. 
 *  
 *  A node either has data which is a set of child nodes or 
 *  data which is a MyPatch.
 * 
 * @author Paul John Leonard
 *
 */
public class Node implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	Serializable data;
	String[] keynames = null;

	public Node(String name, Serializable data) {
		this.name = name;
		this.data = data;
	}
	
	public String[] getKeyNames()
	{
		return keynames;
	}

	public String toString() {
		return name;
	}
	
	public Serializable getData() {
		return data;
	}
	

}
