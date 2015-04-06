/*
 *
 * Copyright (c) 2006-2007 Paul John Leonard
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
package com.frinika.sequencer.gui.menu;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.timesignature.TimeSignatureList;
import com.frinika.sequencer.model.timesignature.TimeSignatureList.TimeSignatureEvent;

public class TimeSignatureEditAction extends AbstractAction {

	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;

	private ProjectFrame project;

	private TimeSignatureList list;

	public TimeSignatureEditAction(ProjectFrame project) {
		super(getMessage("sequencer.project.edit_timesignature"), ProjectFrame
				.getIconResource("timesig.png"));
		this.project = project;
		this.list = project.getProjectContainer().getTimeSignatureList();
	}

	public void actionPerformed(ActionEvent arg0) {

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {

				JFrame frame = new JFrame();
				TableModel dataModel = new AbstractTableModel() {

					int bar;
					int nBeat;

					public int getColumnCount() {
						return 2;
					}

					public int getRowCount() {
						list.reco();
						return list.getList().size()+1;
					}

					public Object getValueAt(int row, int col) {
						if (row >= list.getList().size()) return "";
						TimeSignatureEvent ev = list.getList().elementAt(row);
						if (col == 0)
							return ev.bar;
						else
							return ev.beatsPerBar;
					}

					public boolean isCellEditable(int row, int col) {
						return true;
					}

					public void setValueAt(Object value, int row, int col) {
						boolean newE = row >= list.getList().size();

						TimeSignatureEvent ev=null;
						if (!newE) {
							ev = list.getList().elementAt(row);
						} else {
							ev = list.getList().elementAt(list.getList().size()-1);
						}
						
						bar = ev.bar;
						nBeat = ev.beatsPerBar;
							
						
						try {
							if (col == 0) {
								bar = Integer.parseInt((String) value);
			
							} else {
								nBeat = Integer.parseInt((String) value);
							}
							if (!newE )	list.remove(ev.bar);
							list.add(bar, nBeat);
							list.reco();
						} catch (Exception e) {
							e.printStackTrace();
						}
						fireTableDataChanged();
					}

				};
				JTable table = new JTable(dataModel);
				table.getColumnModel().getColumn(0).setWidth(50);
				table.getColumnModel().getColumn(1).setWidth(15);
				table.getColumnModel().getColumn(0).setHeaderValue("BAR");
				table.getColumnModel().getColumn(1).setHeaderValue("BEATS");
				
				JScrollPane scrollpane = new JScrollPane(table);
				frame.setContentPane(scrollpane);
				frame.setTitle("Time signitures");
				frame.pack();
				frame.setVisible(true);
			}
		});

	}
}