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
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;

public class MyCursors {

    private static Map<CursorType, Cursor> cursors = null;

    public static Cursor getCursor(CursorType cursorType) {
        if (cursors == null) {
            init();
        }

        return cursors.get(cursorType);
    }

    private static void init() {
        cursors = new HashMap<>();
        for (CursorType cursorType : CursorType.values()) {
            cursors.put(cursorType, createCursor(cursorType));
        }
    }

    private static Cursor createCursor(CursorType cursorType) {
        Toolkit toolKit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolKit.getBestCursorSize(16, 16);
        ImageIcon icon = null;

        try {
            switch ((int) dimension.width) {
                case 16:
                    icon = new ImageIcon(ClassLoader.getSystemResource("icons/"
                            + cursorType.imageName + ".png"));
                    break;
                case 32:
                    icon = new ImageIcon(ClassLoader.getSystemResource("icons/"
                            + cursorType.imageName + "32.png"));
                    break;
            }

            if (icon == null) {
                throw new Exception("System does not support 16x16 or 32x32 custom cursors (SOB)");
            }

            if (cursorType == CursorType.MOVE) {
                return toolKit.createCustomCursor(icon.getImage(), new Point(8, 8),
                        cursorType.imageName);
            } else {
                return toolKit.createCustomCursor(icon.getImage(), new Point(0, icon
                        .getIconHeight() - 1), cursorType.imageName);
            }
        } catch (Exception e) {
            System.out.println("Unable to create cursor: " + cursorType.name());
            e.printStackTrace();
        }

        return null;
    }

    public enum CursorType {
        PENCIL("pencil"),
        ERASER("eraser"),
        MOVE("hand"),
        GLUE("glue");

        private final String imageName;

        private CursorType(String imageName) {
            this.imageName = imageName;
        }
    }
}
