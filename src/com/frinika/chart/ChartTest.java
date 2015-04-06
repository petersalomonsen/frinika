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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

public class ChartTest {

	static private FileFilter chartFileFilter=new ChartFileFilter();

	public static void main(String args[]) {
		JFrame frame = new JFrame();
		Chart chart = new Chart();

		String keyRoot="C";
		String scale="Major";
		
		final ChartPanel chartPanel = new ChartPanel(chart);

		JMenuBar bar = new JMenuBar();

		frame.setJMenuBar(bar);

		bar.add(new JMenuItem(new AbstractAction("load") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(chartFileFilter);

				int returnVal = fc.showOpenDialog(chartPanel);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						InputStream fin = new FileInputStream(file);
						ObjectInputStream in;
						in = new ObjectInputStream(fin);
						Object obj = in.readObject();
						chartPanel.setChart((Chart) obj);

					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (ClassNotFoundException e2) {
						e2.printStackTrace();
					}
				}
			}

		}));

		bar.add(new JMenuItem(new AbstractAction("save") {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileFilter(chartFileFilter);
				int returnVal = fc.showOpenDialog(chartPanel);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						OutputStream fout = new FileOutputStream(file);
						ObjectOutputStream out;
						out = new ObjectOutputStream(fout);
						out.writeObject(chartPanel.getChart());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}

		}));

		frame.setContentPane(chartPanel);

		chart.setDefaultKey(keyRoot,scale);
		chart.appendBar("C", 4);
		chart.appendBar("Gm7", 4);
		chart.appendBar("A#+5", 4);
		chart.appendBar("D / Cm /", 4);
		chart.appendBar("A / / F", 4);
		chart.appendBar("Dbmaj7(b5) / / /", 4);

		frame.setSize(500, 800);
		frame.setVisible(true);
		frame.repaint();
	}
	
	private static class ChartFileFilter extends FileFilter {
		public boolean accept(File f) {	
			if(f.isDirectory()) return true;
			if(!f.isFile()) return false;		
			if(f.getName().toLowerCase().endsWith(".chart")) return true;
			return false;
		}

		public String getDescription() {
			return "Chart Files (*.chart)";
		}
	}	
}
