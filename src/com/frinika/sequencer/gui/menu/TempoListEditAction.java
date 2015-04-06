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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.gui.TimeSelector;
import com.frinika.sequencer.model.tempo.TempoList;
import com.frinika.sequencer.model.tempo.TempoListListener;
import com.frinika.sequencer.model.util.TimeUtils;

public class TempoListEditAction extends AbstractAction {

	/**
	 * 
	 */

	private static final long serialVersionUID = 1L;

	private ProjectFrame project;

	private TempoList list;

	private TimeUtils timeUtil;

	public TempoListEditAction(ProjectFrame project) {
		super(getMessage("sequencer.project.edit_tempolist"), ProjectFrame
				.getIconResource("tempolist.png"));
		this.project = project;
		this.list = project.getProjectContainer().getTempoList();
		this.timeUtil = project.getProjectContainer().getTimeUtils();
	}

	JFrame frame;

	public void actionPerformed(ActionEvent arg0) {

		SwingUtilities.invokeLater(new Runnable() {

			@SuppressWarnings("serial")
			public void run() {

				if (frame == null) {

					frame = new JFrame();
					TableModel dataModel = new AbstractTableModel() {

						int tick;

						double bpm;

						TempoListListener listener = new TempoListListener() {

							public void notifyTempoListChange() {
								fireTableDataChanged();
							}

						};

						{
							list.addTempoListListener(listener);
						}

						public int getColumnCount() {
							return 3;
						}

						public int getRowCount() {
							list.reco();
							return list.size() + 1;
						}

						public Object getValueAt(int row, int col) {
							if (row >= list.size())
								return "";
							TempoList.MyTempoEvent ev = list.elementAt(row);
							if (col == 0)
								return timeUtil.tickToBarBeatTick(ev.getTick());
							else if (col == 1)
								return ev.getTime();
							else if (col == 2)
								return ev.getBPM();

							assert (false);
							return "";
						}

						public boolean isCellEditable(int row, int col) {
							if (row == 0 && col == 0)
								return false;
							return col == 0 || col == 2;
						}

						public void setValueAt(Object value, int row, int col) {
							boolean newE = row >= list.size();

							TempoList.MyTempoEvent ev = null;
							if (!newE) {
								ev = list.elementAt(row);
							} else {
								ev = list.elementAt(list.size() - 1);
							}

							tick = (int) ev.getTick();
							bpm = ev.getBPM();

							try {
								if (col == 0) {
									tick = (int) timeUtil
											.barBeatTickToTick((String) value);

								} else if (col == 2) {
									bpm = Double.parseDouble((String) value);
								}
								if (!newE)
									list.remove(ev.getTick(), ev.getTick() + 1);
								list.add(tick, bpm);
								list.reco();
								list.notifyListeners();
							} catch (Exception e) {
								e.printStackTrace();
							}
							fireTableDataChanged();
						}
					};

					JTable table = new JTable(dataModel);
					table.getColumnModel().getColumn(0).setWidth(50);
					table.getColumnModel().getColumn(1).setWidth(15);
					table.getColumnModel().getColumn(2).setWidth(15);

					table.getColumnModel().getColumn(0).setHeaderValue(
							"Bar.Beat:Tick");
					table.getColumnModel().getColumn(1).setHeaderValue("Time");
					table.getColumnModel().getColumn(2).setHeaderValue("BPM");

					class TimeCellEditor extends AbstractCellEditor implements
							TableCellEditor {

						TimeSelector ts = new TimeSelector(project
								.getProjectContainer());

						public Object getCellEditorValue() {
							return ts.getString();
						}

						// Implement the one method defined by TableCellEditor.
						public Component getTableCellEditorComponent(
								JTable table, Object value, boolean isSelected,
								int row, int column) {
							ts.setString((String) value);
							return ts;
						}
					}
					;

					// nearly works but a bit messy
					// table.getColumnModel().getColumn(0).setCellEditor(new
					// TimeCellEditor());

					JScrollPane scrollpane = new JScrollPane(table);
					frame.setContentPane(scrollpane);
					frame.setTitle("Tempo List");
					frame.pack();
				}
				frame.setVisible(true);
			}
		});

	}
}