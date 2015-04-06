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


import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.MyAbstractTableModel;
import com.frinika.sequencer.gui.menu.midi.MidiStepRecordAction;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.MultiEventChangeRecorder;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.util.TimeUtils;

import static com.frinika.localization.CurrentLocale.getMessage;

/**
 * @author Peter Johan Salomonsen
 */
public class MultiEventTableModel extends MyAbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String[] noteColumnNames = { "Time", "Note", "Vel", "Len" };
	private static final String[] cntrlColumnNames = { "Time", "N.A.", "Value", "N.A." };

	static final int COLUMN_TIME = 0; // Relative time to rows

	static final int COLUMN_NOTEORCC = 1;

	static final int COLUMN_VELORVAL = 2; // Velocity or CC value

	static final int COLUMN_LEN = 3; // Note length

	static final int COLUMNS = noteColumnNames.length;

	MultiEvent event;;

	long startTick;

	int ticksPerBeat;

	int quantize;
	TimeUtils time;
	
	public MultiEventTableModel(ProjectContainer project,MultiEvent note, int quantize, int ticksPerBeat) {
		this.event = note;
		this.quantize = quantize;
		this.ticksPerBeat = ticksPerBeat;
		this.time = new TimeUtils(project);
	}

	void setMultiEvent(MultiEvent ev) {
		this.event = ev;
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
		if (event instanceof NoteEvent)
		return (noteColumnNames[column]);
		else 
		return (cntrlColumnNames[column]);
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
			return 8;
		case COLUMN_NOTEORCC:
			return 3;
		case COLUMN_VELORVAL:
			return 4;
		case COLUMN_LEN:
			return 7;
		default:
			return 10;
		}
	}

	public Object getValueAt(int row, int columnIndex) {
		if (event == null ) return "null";
		switch(columnIndex) {
		case COLUMN_TIME:	
			return time.tickToBarBeatTick(event.getStartTick());
			// return (double) event.getStartTick() / ticksPerBeat;
		case COLUMN_VELORVAL:
			return event.getValueUI();
		}
		
		if (event instanceof NoteEvent) return getNoteValueAt(row,columnIndex);
		return null;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getNoteValueAt(int row, int columnIndex) {
		NoteEvent ne=(NoteEvent)event;
		if (event == null) {
			System.out.println("NULL EVENT");
			return null;
		}
		switch (columnIndex) {

		case COLUMN_NOTEORCC:
			return ne.getNoteName();
		case COLUMN_LEN:
			return time.tickToBarBeatTick(ne.getDuration());
		default:
			return (null);
		}
	}

	@Override
	public void setValueAt(final Object obj, final int row, int columnIndex) {
		if (event == null)
			return;
		assert(obj instanceof String);
		final String string=(String)obj;
		
		switch (columnIndex) {
		case COLUMN_TIME:
			final long newTick = time.barBeatTickToTick(string); //(long) (Double.parseDouble(string) * ticksPerBeat);
			new MultiEventChangeRecorder(getMessage("sequencer.eventview.move"), event) {
				public void doChange(MultiEvent me) {
					event.setStartTick(newTick);
				}
			};
			break;
		case COLUMN_VELORVAL:
			new MultiEventChangeRecorder(getMessage("sequencer.eventview.adjust_velocity"), event) {
				public void doChange(MultiEvent event) {
					event.setValueUI(Integer.parseInt(string));
				}
			};

			break;

		case COLUMN_LEN:
			if (!(event instanceof NoteEvent)) return;
			NoteEvent me = (NoteEvent)event;

			new MultiEventChangeRecorder(getMessage("sequencer.change_duration"), me) {
				public void doChange(MultiEvent e) {
	//				((NoteEvent)e).setDuration((long) (Double.parseDouble(string) * ticksPerBeat));
					
					((NoteEvent)e).setDuration((long) (time.barBeatTickToTick(string)));
					
				}
			};
			break;
			
		case COLUMN_NOTEORCC: // was this missing or intentionally left out? (Jens)
			if (!(event instanceof NoteEvent)) return;
			me = (NoteEvent)event;

			new MultiEventChangeRecorder(getMessage("sequencer.eventview.adjust_pitch"), me) {
				public void doChange(MultiEvent e) {
					((NoteEvent)e).setNote(MidiStepRecordAction.parseNote(string));
				}
			};
			break;
			
		}

		//  System.out.println(string + " " + row);

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
