/*
 * Created on Feb 26, 2006
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

 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.sequencer.gui.menu;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import com.frinika.project.ProjectContainer;

/**
 * @author Peter Johan Salomonsen
 */
public class GuiCursorUpdateMenu extends JMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ProjectContainer project;

	public GuiCursorUpdateMenu(final ProjectContainer project) {
		this.project = project;

		int inc = project.getPixelsPerRedraw();

		setText(getMessage("sequencer.menu.settings.cursorinc"));
		ButtonGroup buts = new ButtonGroup();

		JRadioButtonMenuItem item = new JRadioButtonMenuItem(
				getMessage("sequencer.menu.settings.cursorinc.disable"));
		buts.add(item);
		add(item);
		if (inc == -1)
			item.setEnabled(true);
		item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				project.setPixelsPerRedraw(-1);
			}
		});

		item = new JRadioButtonMenuItem(
				getMessage("sequencer.menu.settings.cursorinc.fine"));
		buts.add(item);
		add(item);
		if (inc == 1) {
//			System.out.println(" Why won't you be set by me ? ");
			item.setSelected(true);
		}
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				project.setPixelsPerRedraw(1);
			}
		});

		item = new JRadioButtonMenuItem(
				getMessage("sequencer.menu.settings.cursorinc.medium"));
		buts.add(item);
		add(item);
		if (inc == 2)
			item.setEnabled(true);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				project.setPixelsPerRedraw(2);
			}
		});

		item = new JRadioButtonMenuItem(
				getMessage("sequencer.menu.settings.cursorinc.coarse"));
		buts.add(item);
		add(item);
		if (inc == 5)
			item.setEnabled(true);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				project.setPixelsPerRedraw(5);
			}
		});

		item = new JRadioButtonMenuItem(
				getMessage("sequencer.menu.settings.cursorinc.verycoarse"));
		buts.add(item);
		add(item);
		if (inc == 20)
			item.setEnabled(true);
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				project.setPixelsPerRedraw(20);
			}
		});

	}

}
