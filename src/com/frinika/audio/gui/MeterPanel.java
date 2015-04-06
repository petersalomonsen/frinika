/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.frinika.audio.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * A very simple meter panel.
 *
 *
 *
 * @author pjl
 */
@SuppressWarnings("serial")
public class MeterPanel extends JPanel {

    double val = 0.0;
    Color color = null;
    int redcount = 0;

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(3, 200);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(3, 200);
    }

    public void updateMeter(double val, Color col) {
        this.val = val;
        if (color == null || col == Color.RED) {
            color = col;
        }
        repaint();

    }

    @Override
    public void paintComponent(Graphics g) {

        int w = getWidth();
        int h = getHeight();

        if (val <= 0.0) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, w, h);
        } else {
            int h2 = (int) ((1.0 - val) * h);
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 0, w, h2);

            if ((redcount + 1) % 4 != 0) {
                g.setColor(color);
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillRect(0, h2, w, h);
        }
        if (color == Color.RED) {
            redcount++;
            if (redcount > 20) {
                color = null;
                redcount = 0;
            }
        } else {
            color = null;
        }
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, w - 1, h - 1);
    }
}
