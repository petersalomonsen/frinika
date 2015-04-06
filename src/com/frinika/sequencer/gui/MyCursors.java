/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
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
package com.frinika.sequencer.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

public class MyCursors {
	static Cursor pencil;

	static Cursor eraser;

	static Cursor move;

	static Cursor glue;

	static boolean inited = false;

	static Cursor cursorFromName(String name) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension dim = tk.getBestCursorSize(16, 16);
		ImageIcon icon = null;

		try {
			if (dim.getWidth() == 16) {
				icon = new ImageIcon(ClassLoader.getSystemResource("icons/"
						+ name + ".png"));
			} else if (dim.getWidth() == 32) {
				icon = new ImageIcon(ClassLoader.getSystemResource("icons/"
						+ name + "32.png"));
			} else {
				try {
					throw new Exception(
							" System does not support 16x16 or 32x32 custom cursors (SOB) ");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (name.equals("move")) {
				return tk.createCustomCursor(icon.getImage(), new Point(8, 8),
						name);
			} else {
				return tk.createCustomCursor(icon.getImage(), new Point(0, icon
						.getIconHeight() - 1), name);

			}
		} catch (Exception e) {
			System.out.println("name was " + name );
			e.printStackTrace();
		}
		return null;
		
	}

	static void init() {		
		pencil = cursorFromName("pencil");
		eraser = cursorFromName("eraser");
		move = cursorFromName("hand");
		glue = cursorFromName("glue");
		inited = true;
	}

	public static Cursor getCursor(String name) {
		if (!inited)
			init();
		if (name.equals("pencil"))
			return pencil;
		if (name.equals("eraser"))
			return eraser;
		if (name.equals("move"))
			return move;
		if (name.equals("glue"))
			return glue;
		
		try {
			throw new Exception(" unknown name ");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
