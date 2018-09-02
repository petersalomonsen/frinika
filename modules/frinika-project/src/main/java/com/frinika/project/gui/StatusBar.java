/*
 * Copyright (c) 2004-2008 Paul John Leonard
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
package com.frinika.project.gui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * @author pjl
 */
public class StatusBar extends JPanel {

    private JLabel text;

    public StatusBar() {
        setLayout(new BorderLayout());
        add(text = new JLabel("status bar"), BorderLayout.CENTER);
        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

    public void setMessage(String msg) {
        text.setText(msg);
    }
}
