/*
 * Created on 20 Oct 2007
 *
 * Copyright (c) 2004-2007 Paul John Leonard
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.frinika.chart.Chart.Bar;
import com.frinika.chart.Chart.Chord;

public class ChartPanel extends JPanel implements Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int barsPerLine = 4;

	int chordsPerBar = 4;

	int pad = 2;

	Chart chart;

	public ChartPanel(Chart chart2) {
		this.chart = chart2;
		setBackground(Color.white);

		TableModel dataModel = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public int getColumnCount() {
				return barsPerLine;
			}

			public boolean isCellEditable(int row, int col) {
				return true;
			}

			public int getRowCount() {
				return 1 + chart.getBars().size() / barsPerLine;
			}
			
			public void setValueAt(Object value, int row, int col) {
				int index=row *barsPerLine + col;
				int n=chart.getBars().size();
				for (int i=n;i <= index ; i++) {
					chart.appendBar();
				}
				chart.setbarAt(index,(String)value);				
			}
			
			public Object getValueAt(int row, int col) {
				int index=row *barsPerLine + col;
				if (index < chart.getBars().size()) {
					return chart.getBars().get(row *barsPerLine + col).toString();
				} else {
					return "blank";
					
				}
			}

		};

		setLayout(new BorderLayout());
		JTable table = new JTable(dataModel);
		//table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		//table.setFillsViewportHeight(true);
		JScrollPane scrollpane = new JScrollPane(table);
		add(scrollpane,BorderLayout.CENTER);
	}

//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		System.out.println(" PAINT");
//		int row = 0;
//		int col = 0;
//		int barWidth = getWidth() / barsPerLine;
//		int rowHeight = 20;
//
//		for (Bar bar : chart.bars) {
//
//			int x = col * barWidth;
//			int y = row * rowHeight;
//			g.translate(x, y);
//			g.drawRect(0, 0, barWidth, rowHeight);
//			paintBar(bar, (Graphics2D) g, barWidth, rowHeight);
//
//			g.translate(-x, -y);
//			col++;
//			if (col == barsPerLine) {
//				col = 0;
//				row++;
//			}
//		}
//
//	}
//
//	public void paintBar(Bar bar, Graphics2D g, int barWidth, int rowHeight) {
//
//		int chordWidth = barWidth / chordsPerBar;
//		int x = 0;
//
//		for (Chord chord : bar.chords) {
//			paintChord(chord, g, x, rowHeight);
//			x += chordWidth;
//		}
//
//	}
//
//	public void paintChord(Chord chord, Graphics2D g, int x, int height) {
//		g.drawString(chord.name, x + pad, height - pad);
//	}

	public void update(Observable arg0, Object arg1) {
		repaint();
	}

	public void setChart(Chart chart2) {
		this.chart=chart2;
		repaint();
	}

	public Object getChart() {
		return chart;
	}

}
