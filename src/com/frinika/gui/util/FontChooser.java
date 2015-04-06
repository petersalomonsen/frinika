/*
 * Created on Jun 30, 2007
 *
 * Copyright (c) 2006-2007 Jens Gulden
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

package com.frinika.gui.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * FontChooser by Noah w., modifications by Jens Gulden
 * Original from http://forum.java.sun.com/thread.jspa?forumID=57&threadID=195067.
 */
public class FontChooser extends JDialog {
	String[] styleList = new String[] { "Plain", "Bold", "Italic", "Bold-Italic" };

	String[] sizeList = new String[] { "3", "4", "5", "6", "7", "8", "9", "10",
			"11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "22",
			"24", "27", "30", "34", "39", "45", "51", "60" };

	NwList StyleList;

	NwList FontList;

	NwList SizeList;

	static JLabel Sample = new JLabel();

	boolean ob = false;

	private FontChooser(Frame parent, boolean modal, Font font) {
		super(parent, modal);
		initAll();
		setTitle("Font Chooser");
		if (font == null)
			font = Sample.getFont();
		FontList.setSelectedItem(font.getName());
		SizeList.setSelectedItem(font.getSize() + "");
		StyleList.setSelectedItem(styleList[font.getStyle()]);

	}

	public static Font showDialog(Frame parent, String s, Font font) {
		FontChooser fd = new FontChooser(parent, true, font);
		if (s != null)
			fd.setTitle(s);
		fd.setVisible(true);
		Font fo = null;
		if (fd.ob)
			fo = Sample.getFont();
		fd.dispose();
		return (fo);
	}

	private void initAll() {
		getContentPane().setLayout(null);
		setBounds(150, 150, 425, 425);
		addLists();
		addButtons();
		Sample.setBounds(10, 320, 415, 25);
		Sample.setForeground(Color.black);
		getContentPane().add(Sample);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				setVisible(false);
			}
		});
	}

	private void addLists() {
		FontList = new NwList(GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getAvailableFontFamilyNames());
		StyleList = new NwList(styleList);
		SizeList = new NwList(sizeList);
		FontList.setBounds(10, 10, 260, 295);
		StyleList.setBounds(280, 10, 80, 295);
		SizeList.setBounds(370, 10, 40, 295);
		getContentPane().add(FontList);
		getContentPane().add(StyleList);
		getContentPane().add(SizeList);
	}

	private void addButtons() {
		JButton ok = new JButton("Ok");
		//ok.setMargin(new Insets(0, 0, 0, 0));
		JButton ca = new JButton("Cancel");
		//ca.setMargin(new Insets(0, 0, 0, 0));
		ok.setBounds(260, 350, 70, 20);
		//ok.setFont(new Font(" ", 1, 11));
		ca.setBounds(340, 350, 70, 20);
		//ca.setFont(new Font(" ", 1, 12));
		getContentPane().add(ok);
		getContentPane().add(ca);
		getRootPane().setDefaultButton(ok);
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				ob = true;
			}
		});
		ca.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				ob = false;
			}
		});
	}

	private void showSample() {
		int g = 0;
		try {
			g = Integer.parseInt(SizeList.getSelectedValue());
		} catch (NumberFormatException nfe) {
		}
		/*String st = StyleList.getSelectedValue();
		int s = Font.PLAIN;
		if (st.equalsIgnoreCase("Bold"))
			s = Font.BOLD;
		if (st.equalsIgnoreCase("Italic"))
			s = Font.ITALIC;
		if (st.equalsIgnoreCase("Bold-Italic"))
			s = Font.BOLD | Font.ITALIC;
		*/
		String st = StyleList.getSelectedValue();
		int s = 0;
		int i = 0;
		for (String stl : styleList) {
			if (stl.equalsIgnoreCase(st)) {
				s = i;
			}
			i++;
		}
		Sample.setFont(new Font(FontList.getSelectedValue(), s, g));
		Sample.setText("The quick brown fox jumped over the lazy dog.");
	}
	
	// ////////////////////////////////////////////////////////////////////
	public class NwList extends JPanel {
		JList jl;

		JScrollPane sp;

		JLabel jt;

		String si = " ";

		public NwList(String[] values) {
			setLayout(null);
			jl = new JList(values);
			sp = new JScrollPane(jl);
			jt = new JLabel();
			jt.setBackground(Color.white);
			jt.setForeground(Color.black);
			jt.setOpaque(true);
			jt.setBorder(new JTextField().getBorder());
			jt.setFont(getFont());
			jl.setBounds(0, 0, 100, 1000);
			jl.setBackground(Color.white);
			jl.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					jt.setText((String) jl.getSelectedValue());
					si = (String) jl.getSelectedValue();
					showSample();
				}
			});
			add(sp);
			add(jt);
		}

		public String getSelectedValue() {
			return (si);
		}

		public void setSelectedItem(String s) {
			jl.setSelectedValue(s, true);
		}

		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, y, w, h);
			sp.setBounds(0, y + 12, w, h - 23);
			sp.revalidate();
			jt.setBounds(0, 0, w, 20);
		}

	}
}
