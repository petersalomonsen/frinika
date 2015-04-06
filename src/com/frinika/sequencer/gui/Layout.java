/*
 * Created on Mar 7, 2006
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

package com.frinika.sequencer.gui;

public class Layout {
	public static int timePanelHeight = 20;
	
	
	

	public static int noteHeightIndex=1;
	public static int noteItemHeights[] ={ 7 , 14 , 21, 28 };
	public static int getNoteItemHeight() {
		return noteItemHeights[noteHeightIndex];
	}
	
//	public static int cursorInc=1;
	

	public static int laneHeightIndex=1;
	public static int laneItemHeights[] ={  20 , 30, 40, 50, 60,70,80,90,100,110 };
	public static int getLaneHeightScale() {
		return laneItemHeights[laneHeightIndex];
	}
	
}
