/*
 * Created on 27.2.2007
 *
 * Copyright (c) 2007 Karl Helgason
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
package com.frinika.main;

import com.frinika.gui.util.WindowUtils;
import com.frinika.project.dialog.AboutDialog;
import com.frinika.sequencer.gui.ProjectFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

public class WelcomeDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private boolean darkMode = WindowUtils.isDarkMode();

    Map<Integer, ActionListener> listeners = new HashMap<>();

    private int selectedOption = 0;
    private int ix = 0;

    public int getSelectedOption() {
        return selectedOption;
    }

    public WelcomeDialog() {
        this(new Object[0]);
    }

    public WelcomeDialog(Object[] options) {
        init(options);
    }

    private void init(Object[] options) {
        setUndecorated(true);
        setModal(true);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(darkMode ? Color.BLACK : Color.WHITE);
        Icon welcome = new javax.swing.ImageIcon(ProjectFrame.class.getResource(darkMode ? "/frinika-dark.png" : "/frinika.png"));
        JLabel label = new JLabel(welcome);
        label.setHorizontalTextPosition(SwingConstants.CENTER);
        label.setVerticalTextPosition(SwingConstants.BOTTOM);
        label.setFont(label.getFont().deriveFont(Font.PLAIN));
        label.setText(AboutDialog.MAIN_TITLE);
        label.setBorder(BorderFactory.createEmptyBorder(25, 5, 5, 5));
        panel.add(label, BorderLayout.NORTH);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel contentpane = new JPanel();
        contentpane.setBorder(BorderFactory.createLineBorder(darkMode ? Color.WHITE : Color.BLACK, 2));
        contentpane.setLayout(new BorderLayout());
        contentpane.add(panel);
        setContentPane(contentpane);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        for (int i = 0; i < options.length; i++) {
            ix = i;
            JButton button = new JButton(options[i].toString());
            button.setOpaque(false);
            button.addActionListener(new ActionListener() {
                int index = ix;

                @Override
                public void actionPerformed(ActionEvent e) {

                    ActionListener listener = listeners.get(index);
                    if (listener != null) {
                        listener.actionPerformed(e);
                        return;
                    }

                    selectedOption = index;
                    setVisible(false);
                }
            });
            if (i == 0) {
                button.setDefaultCapable(true);
                getRootPane().setDefaultButton(button);
            }

            buttonPanel.add(button);
        }
        panel.add(buttonPanel, BorderLayout.CENTER);

        JPanel copyrightpanel = new JPanel();
        copyrightpanel.setOpaque(false);

        JLabel line = new JLabel(AboutDialog.COPYRIGHT_NOTICE);
        line.setHorizontalTextPosition(SwingConstants.CENTER);
        line.setFont(line.getFont().deriveFont(10f).deriveFont(Font.PLAIN));

        copyrightpanel.add(line);

        panel.add(copyrightpanel, BorderLayout.SOUTH);

        setTitle("Welcome");

        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        panel.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedOption = -1;
                setVisible(false);
            }
        },
                stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();

        Rectangle windowSize;
        Insets windowInsets;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
        if (gc == null) {
            gc = getGraphicsConfiguration();
        }

        if (gc != null) {
            windowSize = gc.getBounds();
        } else {
            windowSize = new java.awt.Rectangle(toolkit.getScreenSize());
        }

        Dimension size = getSize();
        Point parent_loc = getLocation();
        setLocation(parent_loc.x + windowSize.width / 2 - (size.width / 2),
                parent_loc.y + windowSize.height / 2 - (size.height / 2));
    }

    public void addButtonActionListener(int id, ActionListener listener) {
        listeners.put(id, listener);
    }

    public static int showModal(Object[] options) {
        WelcomeDialog panel = new WelcomeDialog(options);
        panel.setVisible(true);
        return panel.selectedOption;
    }
}
