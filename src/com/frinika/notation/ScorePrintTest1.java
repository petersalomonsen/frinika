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
package com.frinika.notation;

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

public class ScorePrintTest1 extends JPanel {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setSize(1024, 800);
		frame.setLocation(100, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final ScorePrintTest1 s = new ScorePrintTest1();
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
						if (page > 0)
							return NO_SUCH_PAGE;

						double scaleY = pf.getHeight()/s.getHeight();
						double scaleX = pf.getWidth()/s.getWidth();
						
						double scale=Math.min(scaleX,scaleY);
						
						Graphics2D g2d = (Graphics2D) g;
						g2d.scale(scale,scale);
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

	public ScorePrintTest1() {
		setOpaque(false);
		ng.setSize(32);

	}

	NotationGraphics ng = new NotationGraphics();

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
		ng.setGraphics(g2);

		ng.absolute(2);
		ng.absoluteLine(14);
		ng.drawStaff(2048.0f);
		ng.drawBarLine();

		ng.absolute(3);
		g2.setColor(Color.RED);
		ng.drawBarLine();
		g2.setColor(Color.BLACK);

		ng.drawClef(NotationGraphics.CLEF_F);

		ng.relative(3);
		g2.setColor(Color.RED);
		ng.drawBarLine();
		g2.setColor(Color.BLACK);

		ng.relative(1);
		ng.drawTimeSignature(3, 4);

		ng.absolute(13);
		g2.setColor(Color.RED);
		ng.drawBarLine();
		g2.setColor(Color.BLACK);

		ng.drawClef(NotationGraphics.CLEF_G);

		ng.relative(3);
		g2.setColor(Color.RED);
		ng.drawBarLine();
		g2.setColor(Color.BLACK);

		ng.absolute(23);
		g2.setColor(Color.RED);
		ng.drawBarLine();
		g2.setColor(Color.BLACK);
		ng.drawClef(NotationGraphics.CLEF_C);

		ng.relative(3);
		g2.setColor(Color.RED);
		ng.drawBarLine();
		g2.setColor(Color.BLACK);

		ng.absolute(33);
		g2.setColor(Color.RED);
		ng.drawBarLine();
		g2.setColor(Color.BLACK);
		ng.drawClef(NotationGraphics.CLEF_NEUTRAL);

		ng.relative(3);
		g2.setColor(Color.RED);
		ng.drawBarLine();
		g2.setColor(Color.BLACK);

		ng.absolute(2);
		ng.absoluteLine(24);
		ng.drawStaff(2048.0f);
		ng.drawBarLine();
		ng.relative(1);
		ng.drawClef(NotationGraphics.CLEF_G); // Draw G-Clef
		ng.relative(4);
		ng.drawTimeSignature(0);
		// ng.drawTimeSignature(4, 4);
		ng.relative(6);

		ng.startNoteGroup();

		ng.drawNote(1, 2);
		ng.relative(3);
		ng.drawNote(6, 2);

		ng.relative(3);
		ng.drawNote(6, -2);
		ng.relative(3);
		ng.drawNote(4, -1);
		ng.relative(3);
		ng.drawNote(4, 0);
		ng.relative(3);
		ng.drawNote(4, 1);
		ng.relative(3);
		ng.drawNote(4, 2);
		ng.relative(3);
		ng.drawNote(4, 3);
		ng.relative(3);
		ng.drawNote(4, 4);

		ng.relative(3);
		ng.drawNote(4, 2);
		ng.relative(3);
		ng.drawNote(4, 3);
		ng.relative(3);
		ng.drawNote(4, 4);

		ng.relative(3);
		ng.drawNote(0, 2);
		ng.drawNote(2, 2);
		ng.drawNote(4, 3);

		ng.relative(3);
		ng.drawNote(-1, 3);
		ng.relative(3);
		ng.drawNote(-2, 3);
		ng.relative(3);
		ng.drawNote(-3, 3);
		ng.relative(3);
		ng.drawNote(-4, 3);
		ng.relative(3);

		ng.drawNote(12, 3, 0);
		ng.relative(3);
		ng.drawNote(10, 3, 1);
		ng.relative(3);
		ng.drawNote(9, 3, 2);

		ng.relative(3);

		ng.relative(3);
		ng.drawRest(-2);
		ng.relative(3);
		ng.drawRest(-1);
		ng.relative(3);
		ng.drawRest(0);
		ng.relative(3);
		ng.drawRest(1);
		ng.relative(3);
		ng.drawRest(2,1);
		ng.relative(3);
		ng.drawRest(3);
		ng.relative(3);
		ng.drawRest(4);
		ng.relative(3);
		ng.drawRest(5);
		ng.relative(3);
		ng.drawRest(6,1);
		ng.relative(3);
		ng.drawRest(7,2);

		// g2.setColor(Color.RED);
		// ng.drawBarLine();

		// ng.drawBarLine(2);
		// ng.relative(3);
		// ng.drawBarLine(0);

		ng.endNoteGroup();

		ng.absolute(2);
		ng.absoluteLine(34);
		ng.drawStaff(2048.0f);
		ng.drawBarLine();
		ng.relative(1);
		ng.drawClef(NotationGraphics.CLEF_G); // Draw G-Clef
		ng.relative(4);

		// ng.relative(ng.drawSharpKeySignature(8,5,9,6,3,7,4)+1);
		ng.relative(ng.drawSharpKeySignature(8, 5, 9) + 1);

		ng.drawTimeSignature(4, 4);
		ng.relative(6);

		ng.drawNote(1, 2, 0,  -200);
		ng.relative(6);
		ng.drawNote(1, 2, 0, -150);
		ng.relative(6);
		ng.drawNote(1, 2, 0, -100);
		ng.relative(6);
		ng.drawNote(1, 2, 0, -50);
		ng.relative(6);
		ng.drawNote(1, 2, 0, NotationGraphics.ACCIDENTAL_NATURAL);
		ng.relative(6);
		ng.drawNote(1, 2, 0, 0);
		ng.relative(6);
		ng.drawNote(1, 2, 0, 50);
		ng.relative(6);
		ng.drawNote(1, 2, 0, 100);
		Note n1 = ng.drawNote(1,2,0,100);	
		ng.relative(6);
		Note n2 = ng.drawNote(1,2,0,150);		
		ng.drawNoteTie(n1, n2);
		
		ng.relative(6);
		ng.drawNote(2, 2, 0, 1, 200);
		ng.relative(6);

		ng.startNoteGroup();
		ng.drawNote(2, 3);
		ng.drawNote(4, 3);
		ng.drawNote(6, 3);
		ng.relative(3);
		ng.endNoteGroup();

		ng.startNoteGroup();
		ng.drawNote(2, 2);
		ng.drawNote(4, 2);
		ng.drawNote(6, 2);
		ng.relative(3);
		ng.endNoteGroup();

		ng.startNoteGroup();
		ng.drawNote(2, 1);
		ng.drawNote(4, 1);
		ng.drawNote(6, 1);
		ng.relative(3);
		ng.endNoteGroup();

		ng.startNoteGroup();
		ng.drawNote(2, 0);
		ng.drawNote(4, 0);
		ng.drawNote(6, 0);
		ng.relative(3);
		ng.endNoteGroup();

		ng.startNoteGroup();
		ng.drawNote(2,3,0,0,0,1);	
		ng.drawNote(4,3,0,0,0,1);		
		ng.drawNote(6,3,0,0,0,1);		
		ng.relative(3);
		ng.endNoteGroup();

		ng.startNoteGroup();
		ng.drawNote(8, 3);
		ng.relative(3);
		ng.drawNote(5, 4);
		ng.relative(3);
		ng.drawNote(4, 4);
		ng.relative(3);
		ng.endNoteGroup();

		ng.startNoteGroup();
		ng.drawNote(0, 2);
		ng.relative(3);
		ng.drawNote(0, 3);
		ng.relative(3);
		ng.drawNote(2, 4);
		ng.relative(3);
		ng.drawNote(6, 5);
		ng.relative(3);
		ng.drawNote(4, 1);
		ng.relative(3);
		ng.endNoteGroup();

	}

	public int print(Graphics arg0, PageFormat arg1, int arg2)
			throws PrinterException {
		// TODO Auto-generated method stub
		return 0;
	}

}