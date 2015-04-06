/*
 * Created on Mar 5, 2006
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

import java.awt.Color;

public class ColorScheme {
	static final int colorArraySize = 50;

	static Color colorNormal[] = new Color[colorArraySize];

	static public final Color validBackground = new Color(0xF0FEFE);
	static Color colorTrans[] = new Color[colorArraySize];

	static {
		for (int i = 0; i < colorArraySize; i++) {
			colorNormal[i] = Color
					.getHSBColor((float) ((i) * .57), 0.90f, 0.9f);
			float cc[] = colorNormal[i].getComponents(null);
			colorTrans[i] = new Color(cc[0], cc[1], cc[2], 0.8f);
			/*
			 * colorDrag[i] = Color.getHSBColor((float) ((i)*.15), 0.90f,.7f);
			 * colorTrace[i] = Color.getHSBColor((float) ((i)*.15), 0.10f,1.0f);
			 * colorSelect[i] = Color.getHSBColor((float) ((i)*.15), 0.90f,.5f);
			 */}
	}

	public static Color partViewBackground = Color.LIGHT_GRAY;


	public static Color partViewLinesBar= Color.BLACK; //BLUE.brighter();

	public static Color partViewLinesBeat = Color.GRAY;

	public static Color partViewLinesSubBeat = Color.GRAY.brighter();

	public static Color partDragColor = Color.PINK;

	public static Color tickLine = Color.PINK;

	public static Color selectRect = Color.RED;

	public static Color dragRect = Color.GREEN;

	public static Color selectedColor = Color.DARK_GRAY;

	public static Color partViewLinesHoriz = Color.DARK_GRAY;

	public static Color pianaoRollInvalid=Color.DARK_GRAY;

	
	
	public static Color getNormal(int id) {
		return colorNormal[id % colorArraySize];
	}

	public static Color getTransparent(int id) {
		return colorTrans[id % colorArraySize];
	}

	/*
	 * public static Color getDrag(int id) { return
	 * colorDrag[id%colorArraySize]; } public static Color getTrace(int id) {
	 * return colorTrace[id%colorArraySize]; }
	 */
	
	
}
