/*
 * Created on Nov 21, 2004
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
package com.frinika.main.action;

import com.frinika.gui.util.WindowUtils;
import com.frinika.main.panel.AboutPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * @author peter
 */
public class About {

    public static void about(JFrame parentFrame) {
        boolean darkMode = WindowUtils.isDarkMode();
        final JDialog aboutDialog = new JDialog(parentFrame);
        aboutDialog.setUndecorated(true);
        aboutDialog.setModal(true);
        aboutDialog.setTitle("About Frinika");
        aboutDialog.getRootPane().setBorder(BorderFactory.createLineBorder(darkMode ? Color.WHITE : Color.BLACK, 2));
        AboutPanel aboutPanel = new AboutPanel();
        aboutDialog.add(aboutPanel, BorderLayout.CENTER);
        aboutDialog.pack();
        centerDialog(aboutDialog);
        aboutPanel.setOkCancelListener(new WindowUtils.OkCancelListener() {
            @Override
            public void okEvent() {
                aboutDialog.setVisible(false);
            }

            @Override
            public void cancelEvent() {
                aboutDialog.setVisible(false);
            }
        });

        aboutDialog.setVisible(true);

        //aboutDialog.pack();
        // new AboutDialog(parentFrame).setVisible(true);
        /*
        JOptionPane.showMessageDialog(parentFrame,
                new AboutPanel()
         ,
				"About Frinika",JOptionPane.INFORMATION_MESSAGE);
         */
    }

    private static void centerDialog(JDialog aboutDialog) {
        Rectangle windowSize;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        if (gc == null) {
            gc = aboutDialog.getGraphicsConfiguration();
        }

        if (gc != null) {
            windowSize = gc.getBounds();
        } else {
            windowSize = new java.awt.Rectangle(toolkit.getScreenSize());
        }

        Dimension size = aboutDialog.getSize();
        Point parent_loc = aboutDialog.getLocation();
        aboutDialog.setLocation(parent_loc.x + windowSize.width / 2 - (size.width / 2),
                parent_loc.y + windowSize.height / 2 - (size.height / 2));
    }
}
