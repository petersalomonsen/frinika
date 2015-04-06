/*
 * Created on Jan 19, 2006
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
package com.frinika.sequencer.gui.pianoroll;

import com.frinika.sequencer.gui.MyAbstractTableModel;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.MultiEventChangeRecorder;
import com.frinika.sequencer.model.NoteEvent;

/**
 * @author Peter Johan Salomonsen
 */
public class NoteEventTableModel extends MyAbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String[] ColumnNames = { "Time", "Note", "Vel", "Len" };

	static final int COLUMN_TIME = 0; // Relative time to rows

	static final int COLUMN_NOTEORCC = 1;

	static final int COLUMN_VELORVAL = 2; // Velocity or CC value

	static final int COLUMN_LEN = 3; // Note length

	static final int COLUMNS = ColumnNames.length;

	NoteEvent note;;

	long startTick;

	int ticksPerBeat;

	int quantize;

	public NoteEventTableModel(NoteEvent note, int quantize, int ticksPerBeat) {
		this.note = note;
		this.quantize = quantize;
		this.ticksPerBeat = ticksPerBeat;
	}

	void setNote(NoteEvent note) {
		this.note = note;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int columnIndex) {

		switch (columnIndex) {
		case COLUMN_TIME:
			return Double.class;
		case COLUMN_LEN:
			return Double.class;
		default:
			return Integer.class;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		return (ColumnNames[column]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return (COLUMNS);
	}

	public int getColumnWidth(int column) {
		switch (column) {
		case COLUMN_TIME:
			return 4;
		case COLUMN_NOTEORCC:
			return 3;
		case COLUMN_VELORVAL:
			return 2;
		case COLUMN_LEN:
			return 4;
		default:
			return 10;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int columnIndex) {
		if (note == null)
			return null;
		switch (columnIndex) {
		case COLUMN_TIME:
			return (double) note.getStartTick() / ticksPerBeat;
		case COLUMN_NOTEORCC:
			return note.getNoteName();
		case COLUMN_VELORVAL:
			return note.getVelocity();
		case COLUMN_LEN:
			return (double) note.getDuration() / ticksPerBeat;
		default:
			return (null);
		}
	}

	@Override
	public void setValueAt(final Object value, final int row, int columnIndex) {
		if (note == null)
			return;
		NoteEvent me = note;
		switch (columnIndex) {
		case COLUMN_TIME:
			final long newTick = (long) ((Double) value * ticksPerBeat);
			new MultiEventChangeRecorder("move event", me) {
				public void doChange(MultiEvent me) {
					note.setStartTick(newTick);
				}
			};
			break;
		case COLUMN_VELORVAL:
			new MultiEventChangeRecorder("change velocity", me) {
				public void doChange(MultiEvent me) {
					((NoteEvent) me).setVelocity(((Integer) value).intValue());
				}
			};

			break;

		case COLUMN_LEN:
			new MultiEventChangeRecorder("change duration", me) {
				public void doChange(MultiEvent me) {
					note.setDuration((long) ((Double) value * ticksPerBeat));
				}
			};
			break;
		}

		System.out.println(value + " " + row);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

}
