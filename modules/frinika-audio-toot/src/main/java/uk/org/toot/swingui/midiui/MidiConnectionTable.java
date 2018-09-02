// Copyright (C) 2005 - 2007 Steve Taylor.
// Distributed under the Toot Software License, Version 1.0. (See
// accompanying file LICENSE_1_0.txt or copy at
// http://www.toot.org.uk/LICENSE_1_0.txt)

package uk.org.toot.swingui.midiui;

import java.util.Observable;
import java.util.Observer;

import javax.sound.midi.MidiUnavailableException;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import uk.org.toot.midi.core.*;

public class MidiConnectionTable extends JTable
{
    public MidiConnectionTable(ConnectedMidiSystem system) {
    	super(new TableModel(system));
    	setup(system);
    }

    protected void setup(ConnectedMidiSystem system) {
        try {
        	int colWidth = 128;
    	    TableColumn col = getColumn("From");
            col.setMinWidth(colWidth);
            col = getColumn("To");
            col.setMinWidth(colWidth);
			col.setCellEditor(new DefaultCellEditor(new MidiPortCombo(system, true)));
        } catch ( IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    private static class TableModel extends AbstractTableModel implements Observer
    {
    	private ConnectedMidiSystem system;
    	private final static String[] names = { "From", "To"};
    	
    	public TableModel(ConnectedMidiSystem sys) {
    		system = sys;
            system.addObserver(this);
    	}
    	
		public int getColumnCount() {
			return names.length;
		}

		public int getRowCount() {
			return system.getMidiConnections().size();
		}

		public Object getValueAt(int row, int col) {
			MidiConnection c = system.getMidiConnections().get(row);
			MidiPort port = col > 0 ? c.getMidiInput() : c.getMidiOutput();
			return port;
		}

		public void setValueAt(Object value, int row, int col) {
			MidiConnection c = system.getMidiConnections().get(row);
			try {
				if ( col > 0 ) {
					c.connectTo((MidiInput)value);
				} else {
					c.connectFrom((MidiOutput)value);
				}
			} catch ( MidiUnavailableException mue ) {
				mue.printStackTrace();
			}
		}
		
	    public String getColumnName(int col) { return names[col]; }

	    public boolean isCellEditable(int row, int col) {
	    	return col == 1;
	    }
	    
		public void update(Observable arg0, Object arg1) {
			this.fireTableDataChanged();
		}
    	
    }
}
