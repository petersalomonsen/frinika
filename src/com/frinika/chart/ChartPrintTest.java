/*
 * Created on 16.4.2007
 *
 * Copyright (c) 2006-2007 Karl Helgason
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
package com.frinika.chart;

import com.frinika.chart.Chart.Bar;
import com.frinika.chart.Chart.Chord;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.frinika.notation.NotationGraphics.Note;
import java.util.List;

public class ChartPrintTest extends JPanel {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {

        JFrame frame = new JFrame();
        frame.setSize(1024, 800);
        frame.setLocation(100, 100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       	String keyRoot="C";
	String scale="Major";
        Chart chart=new Chart();
       chart.setDefaultKey(keyRoot,scale); 
        chart.appendBar("Bm7b6 Ab6+5",4);
        chart.appendBar("C",4);
        
        final ChartPrintTest s = new ChartPrintTest(chart);
        s.setLayout(new BorderLayout());

        frame.add(s);
        frame.setVisible(true);

        PrinterJob job = PrinterJob.getPrinterJob();

        JFrame f = new JFrame("Print");
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        JButton printButton = new JButton("Print");
        printButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PrinterJob job = PrinterJob.getPrinterJob();


                job.setPrintable(new Printable() {

                    public int print(Graphics g, PageFormat pf, int page)
                            throws PrinterException {
                        if (page > 0) {
                            return NO_SUCH_PAGE;
                        }
                        double scaleY = pf.getHeight() / s.getHeight();
                        double scaleX = pf.getWidth() / s.getWidth();

                        double scale = Math.min(scaleX, scaleY);

                        Graphics2D g2d = (Graphics2D) g;
                        g2d.scale(scale, scale);
                        g2d.translate(pf.getImageableX(), pf.getImageableY());

                        s.paint(g);
                        return PAGE_EXISTS;
                    }
                });
                boolean ok = job.printDialog();
                if (ok) {
                    try {
                        job.print();
                    } catch (PrinterException ex) {
                        /* The job did not successfully complete */
                    }
                }
            }
        });
        f.add("Center", printButton);
        f.pack();
        f.setVisible(true);

    }
    private Chart chart;
    private int barPerLine;
    private int pageWidth;
    private int lineHeight;
    private int barWidth;

    public ChartPrintTest(Chart chart) {
        setOpaque(false);
        this.chart = chart;
        this.barPerLine = 4;
        this.pageWidth = 600;
        this.lineHeight = 40;
    }

//	NotationGraphics ng = new NotationGraphics();
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        // g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
        // RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 40, 2048, 600);
        g2.setColor(Color.BLACK);

        barWidth = pageWidth / barPerLine;

        int x = 0;
        int y = 0;
        int cnt = 0;
        g.drawLine(0, y, pageWidth, y);

        for (Bar bar : chart.getBars()) {
            if (cnt % barPerLine == 0) {
                y += lineHeight;
                g.drawLine(0, y, pageWidth, y);
                x = 0;
            }
            paintBar(bar, g2, x, y);
            x += barWidth;
        }

        for (int i=0;i<=barPerLine;i++) {
            g.drawLine(i*barWidth, 0, i*barWidth, y);
        }

    }

    public int print(Graphics arg0, PageFormat arg1, int arg2)
            throws PrinterException {
        // TODO Auto-generated method stub
        return 0;
    }

    private void paintBar(Bar bar, Graphics2D g2, int x, int y) {
        List<Chord> chords = bar.getChords();
        int chordWid = barWidth / chords.size();
        for (Chord chord : chords) {
            paintChord(chord, g2, x, y);
            x += chordWid;
        }

    }

    private void paintChord(Chord chord, Graphics2D g2, int x, int y) {
             g2.drawString(chord.name,x,y);
    }
}