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
package com.frinika.sequencer.gui.tracker;

import static com.frinika.localization.CurrentLocale.getMessage;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.FrinikaSequencer;
import com.frinika.sequencer.model.ChannelEvent;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.EditHistoryAction;
import com.frinika.sequencer.model.EditHistoryListener;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MovePartEditAction;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.MultiEventChangeRecorder;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.PitchBendEvent;
import com.frinika.sequencer.model.SysexEvent;
import com.frinika.sequencer.model.util.TimeUtils;

/**
 * @author Peter Johan Salomonsen
 */
public class TrackerTableModel extends AbstractTableModel implements EditHistoryListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;


    private static final String[] ColumnNames = {
        "Time",
        //"Ch",
        "Note",
        "Vel",
        "Len"
    };

    static final int COLUMN_TIME = 0;   // Relative time to rows
    static final int COLUMN_CHANNEL = -1; // MIDI Channel
    static final int COLUMN_NOTEORCC = 1; // Note or Control Change or Pitch bend
    static final int COLUMN_VELORVAL = 2; // Velocity or CC value
    static final int COLUMN_LEN = 3; // Note length

    static final int COLUMNS = ColumnNames.length;

    double ticksPerRow;     // Number of ticks per row
    FrinikaSequence sequence;
    FrinikaSequencer sequencer;
    MidiPart midiPart;
    int columnCount = 1;

    long startTick = 0;
    int beatCount = 0;
    int editVelocity = 100;
    double editDuration = 1.0;

    TimeUtils timeUtils;
    ProjectFrame frame;
    
    public TrackerTableModel(ProjectFrame frame) {
    	this.frame = frame;
    	ProjectContainer project = frame.getProjectContainer();
        this.sequence = project.getSequence();
        this.ticksPerRow = sequence.getResolution() / 4.0; // Default 1/4th notes
        this.sequencer = project.getSequencer();
        timeUtils = new TimeUtils(project);
        
        project.getEditHistoryContainer().addEditHistoryListener(this);
    }

    public void setMidiPart(MidiPart part)
    {
    		if(part==null)
    		{
    			startTick = 0;
    			beatCount = 0;
    			midiPart = null;
    		}
    		else
    		{
    			this.midiPart = part;
    			// Make sure that startTick is always on a whole beat
    			this.startTick = (part.getStartTick() - (part.getStartTick() % sequence.getResolution()));
        
    			this.beatCount = (int)((part.getEndTick() - startTick) / sequence.getResolution());
    			if(((part.getEndTick() - startTick) % sequence.getResolution())>0)
    				beatCount++;
    		}
        
        fireTableDataChanged();
    }
    
    public void setStartBeat(int startBeat)
    {
        this.startTick = startBeat * sequence.getResolution();
        fireTableDataChanged();
    }
    
    public void setBeatCount(int beatCount)
    {
        this.beatCount = beatCount;
        fireTableDataChanged();
    }
    
    public int getTicksPerRow() {
		return (int)(sequence.getResolution() / ticksPerRow);
	}

    /**
     * Set number of tracker rows to be showed for one beat
     * @param rowsPerBeat
     */
	public void setRowsPerBeat(int rowsPerBeat)
    {
    		this.ticksPerRow = sequence.getResolution() / (double)rowsPerBeat;
    		fireTableDataChanged();
    }
    
    public int getEditVelocity() {
		return editVelocity;
	}

	public void setEditVelocity(int editVelocity) {
		this.editVelocity = editVelocity;
	}
	
	
    public double getEditDuration() {
		return editDuration;
	}

	public void setEditDuration(double editDuration) {
		this.editDuration = editDuration;
	}
	

	/* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
    		return (int)((beatCount * sequence.getResolution()) / ticksPerRow);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class< ? > getColumnClass(int columnIndex) {
        if(columnIndex == 0)
            return(Integer.class);
        else
            switch((columnIndex-1)%COLUMNS)
            {
                case COLUMN_TIME:
                    return Double.class;
                case COLUMN_LEN:
                    return Double.class;
                default:
                    return Integer.class;
                
            }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int column) {
        if(column!=0)
        {
            return(ColumnNames[(column-1)%COLUMNS]);
        }
        else
        {
            return("Bar.Beat");
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        //System.out.println(columnCount);
        return((columnCount*COLUMNS)+1);
    }
    
    public final long getTickForRow(int row)
    {
        return((long)((row*ticksPerRow)+startTick));
    }

    /**
     * Return row for the given tick
     * @param tick
     * @return
     */
    public final int getRowForTick(long tick)
    {
        return((int)Math.round((tick-startTick)/ticksPerRow));
    }
    
    /**
     * Get the row of the current sequencer position
     * @return
     */
    public final int getPlayingRow()
    {
        return getRowForTick(sequencer.getTickPosition());
    }
    
    /**
     * When building the table, the getRowEvents and getCellEvent methods are called repeatedly with the same parameters. 
     * The following private variables is to cache the returned set when the same parameters are repeated.
     */  
    private int lastRow=-1;
    private Collection<MultiEvent> lastRowEvents;
    
    /**
     * Return a subset of MultiEvents for the given table row
     * @param row
     * @return
     */
    public final Collection<MultiEvent> getRowEvents(int row)
    {
    		if(row==lastRow )
    		{
    			return lastRowEvents;	
    		}
    		else
    		{
	    		long rowTick = getTickForRow(row);
    	        // System.out.println((rowTick-(quantize/2)+" "+(rowTick+(quantize/2))));
    	        
    	        SortedSet<MultiEvent> tmpRowEvents = midiPart.getMultiEventSubset( (long)(rowTick-(ticksPerRow/2)),
    	                (long)(rowTick+(ticksPerRow/2))  );
    	        
                
                // Some multievents might have fixed columns, thus we'll reorder the rowEvent set here
                Vector<MultiEvent> rowEvents = new Vector<MultiEvent>();
                for(MultiEvent multiEvent : tmpRowEvents)
                {
                
                    if(multiEvent.getTrackerColumn()!=null)
                    {
                        // If multiEvent has a fixed columnIndex
                        int columnIndex = multiEvent.getTrackerColumn();  
                        
                        while(columnIndex>rowEvents.size())
                            rowEvents.add(null);
                        
                        if(columnIndex<rowEvents.size() && rowEvents.get(columnIndex)==null)
                            rowEvents.remove(columnIndex);
                        
                        // Depending on the rows per beat setting we'll try to assign a column according to the fixed as good as possible
                        rowEvents.add(columnIndex,multiEvent);
                    }
                    else
                        rowEvents.add(multiEvent);
                }
    
                if((rowEvents.size()+1)>columnCount)
                {
                    columnCount=rowEvents.size()+1;
                    //System.out.println("colCount"+columnCount);
                    fireTableStructureChanged();
                }
    
    	        lastRow = row;
    	        lastRowEvents = rowEvents;
    	        return rowEvents;
    		}
    }
    
    /**
     * These two are cache variables to speed up the table rendering
     */
    private int lastCol=-1;
    private MultiEvent lastCellEvent;
    
    /**
     * Get the MultiEvent for a specific cell
     * @param row
     * @param col
     * @return
     */
    public final MultiEvent getCellEvent(int row,int col)
    {
		if(row == lastRow && col == lastCol)
		{
			return lastCellEvent;
		}
		else
		{
            Collection<MultiEvent> rowEvents = getRowEvents(row);
            Iterator<MultiEvent> it = rowEvents.iterator();
            MultiEvent event = null;
            int c = -1;
                    
            for(;c<col && it.hasNext();c++)
                event = it.next();
    
			lastCol = col;
    
            if(c==col)
            {  
                if(event!=null && event.getTrackerColumn()==null)
                    event.setTrackerColumn(col);
                lastCellEvent = event;
                return event;
            }
            else
            {
                lastCellEvent = null;
                return null;
            }
                
		}
    }
    
    public final int tableColumnToTrackerColumn(int tableColumn)
    {
        return (tableColumn-1)/COLUMNS;
    }
    
    public MultiEvent getMultiEventAt(int row, int column)
    {
        return getCellEvent(row,tableColumnToTrackerColumn(column));        
    }
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int columnIndex) {
        if(columnIndex == 0)
        {       
            long tick = getTickForRow(row);
            double beat = tick/(double)sequence.getResolution();
            if(beat % 1 == 0)
                return(timeUtils.tickToBarBeat(tick));
            else
                return "";
        }
        else
        {
            int col = tableColumnToTrackerColumn(columnIndex);
            int eventCol = (columnIndex-1)%COLUMNS;

            MultiEvent me = getCellEvent(row,col);
            if( me!=null ) {
            	if (eventCol == COLUMN_TIME) {
                    long relativeTick = me.getStartTick() - startTick;
                    long rowTick =(long)(row * ticksPerRow);
                    return((relativeTick - rowTick) / ticksPerRow);
            	} else {
                    if (me instanceof ChannelEvent) {
                        ChannelEvent event = (ChannelEvent)me;
                        switch(eventCol)
                        {
                            //case COLUMN_TIME:
                            //    long relativeTick = me.getStartTick() - startTick;
                            //    long rowTick =(long)(row * ticksPerRow);
                            //    return((relativeTick - rowTick) / ticksPerRow);
                            case COLUMN_CHANNEL:
                                return(new Integer(event.getChannel()));
                            case COLUMN_NOTEORCC:
                                if(event instanceof NoteEvent)
                                    return ((NoteEvent)event).getNoteName();
                                else if(event instanceof ControllerEvent)
                                    return "CC"+((ControllerEvent)event).getControlNumber();
                                else if(event instanceof PitchBendEvent)
                                    return "PB";
                                else
                                    return null;
                            case COLUMN_VELORVAL:
                                if(event instanceof NoteEvent)
                                    return ((NoteEvent)event).getVelocity();
                                else if(event instanceof ControllerEvent)
                                    return ((ControllerEvent)event).getValue();
                                else if(event instanceof PitchBendEvent)
                                    return (((PitchBendEvent)event).getValue() >> 7);
                                else
                                    return null;
                            case COLUMN_LEN:
                                if(event instanceof NoteEvent)
                                    return ((NoteEvent)event).getDuration() / ticksPerRow;
                            default:
                                return(null);       
                        }
                    } else if (me instanceof SysexEvent) {
                        //ChannelEvent event = (ChannelEvent)me;
                        switch(eventCol)
                        {
                            case COLUMN_NOTEORCC:
                                return "SYX";
                            default:
                                return(null);
                        }
                    } else {
                    	return null;
                    }
            	}
            } else {
                return null;
            }
        }
    }    
    
    @Override
    public void setValueAt(final Object value, final int row, int columnIndex) {
	if(new Integer(-2808).equals(value)) {
	    return;
	}
        int col = tableColumnToTrackerColumn(columnIndex);
        final int eventCol = (columnIndex-1)%COLUMNS;

        final MultiEvent me = getCellEvent(row,col);

        if(me==null)
        {
            // A new MultiEvent
            if(eventCol==COLUMN_NOTEORCC)
            { 
                Integer val = (Integer)value;
                if(val>0)
                {
                    midiPart.getEditHistoryContainer().mark("new note");
                    NoteEvent event = new NoteEvent(midiPart,getTickForRow(row),(Integer)value,editVelocity,1,(long)(long)(ticksPerRow*getEditDuration()));
                    addMultiEvent(event,col);
                    midiPart.getEditHistoryContainer().notifyEditHistoryListeners();
                }
                else if(val >= -127)
                {
                    midiPart.getEditHistoryContainer().mark("new control change");
                    ControllerEvent event = new ControllerEvent(midiPart,getTickForRow(row),-(Integer)value,0);
                    addMultiEvent(event,col);
                    midiPart.getEditHistoryContainer().notifyEditHistoryListeners();
                }
                else if(val == MultiEventCellComponent.EVENT_VALUE_PITCH_BEND)
                {
                    midiPart.getEditHistoryContainer().mark("new pitch bend");
                    PitchBendEvent event = new PitchBendEvent(midiPart,getTickForRow(row),0x2000);
                    addMultiEvent(event,col);
                    midiPart.getEditHistoryContainer().notifyEditHistoryListeners();                    
                }
                else if(val == MultiEventCellComponent.EVENT_VALUE_SYSEX) { // Jens
                    SysexEvent event = new SysexEvent(midiPart,getTickForRow(row));
					event.showEditorGUI(frame);
					if (event.isSuccessfullyParsed()) {
	                    midiPart.getEditHistoryContainer().mark(getMessage("sequencer.sysex.new_sysex"));
	                    addMultiEvent(event,col);
	                    midiPart.getEditHistoryContainer().notifyEditHistoryListeners();                    
					}
                }
            }
            lastRow = -1;
            fireTableRowsUpdated(row,row);
        }
        else
        {
        	try
        	{
	            switch(eventCol)
	            {
	                case COLUMN_TIME:
	                    final long newTick = (long)(getTickForRow(row) + ((Double)value * ticksPerRow));
	
	                    new MultiEventChangeRecorder("move event",me) { public void doChange(MultiEvent me) { me.setStartTick(newTick); } };
	
	                    int newRow = getRowForTick(newTick);
	
	                    if(newRow!=row)
	                    {
	                        lastRow=-1;
	                        fireTableRowsUpdated(row,row);
	                        if(newRow>=0 && newRow<getRowCount())
	                            fireTableRowsUpdated(newRow,newRow);
	                    }
	                    break;
	                case COLUMN_NOTEORCC:
	                    if(value.equals(MultiEventCellComponent.EVENT_VALUE_DELETE))
	                    {
	                        if(me instanceof NoteEvent)
	                            midiPart.getEditHistoryContainer().mark("delete note");
	                        else if(me instanceof ControllerEvent)
	                            midiPart.getEditHistoryContainer().mark("delete control change");
	                        else if(me instanceof PitchBendEvent)
	                            midiPart.getEditHistoryContainer().mark("delete pitch bend");
	                        else if(me instanceof SysexEvent) // Jens
	                            midiPart.getEditHistoryContainer().mark(getMessage("sequencer.sysex.delete_sysex"));
	                        else
	                            midiPart.getEditHistoryContainer().mark("delete event");
	                        midiPart.remove(me);
	                        midiPart.getEditHistoryContainer().notifyEditHistoryListeners();
	                    } else if (me instanceof NoteEvent) {
	                        new MultiEventChangeRecorder("change note",me) { 
	                            public void doChange(MultiEvent me) { ((NoteEvent)me).setNote((Integer)value); }};
	                    } else if (me instanceof SysexEvent) {
							((SysexEvent)me).showEditorGUI(frame);
	                    }
	                    break;
	                case COLUMN_VELORVAL:
	                    if(me instanceof NoteEvent)
	                    {
	                        new MultiEventChangeRecorder("change velocity",me) {
	                            public void doChange(MultiEvent me) {((NoteEvent)me).setVelocity(((Integer)value).intValue());}};
	                    }   
	                    else if(me instanceof ControllerEvent)
	                    {
	                        new MultiEventChangeRecorder("change controller value",me) {
	                            public void doChange(MultiEvent me) {((ControllerEvent)me).setValue(((Integer)value).intValue());}};
	                    }
	                    else if(me instanceof PitchBendEvent)
	                    {
	                        new MultiEventChangeRecorder("change pitchbend value",me) { 
	                            public void doChange(MultiEvent me) {((PitchBendEvent)me).setValue(((Integer)value).intValue() << 7);}};
	                    }
	                    break;
	                case COLUMN_LEN:                    
	                    if(me instanceof NoteEvent)
	                        new MultiEventChangeRecorder("change duration",me) { 
	                            public void doChange(MultiEvent me) {((NoteEvent)me).setDuration((long)((Double)value * ticksPerRow));}};
	                    break;
	            }
        	} catch(Exception e) { e.printStackTrace(); }
        }                       
    }
    
    private void addMultiEvent(MultiEvent event,int column) {
        event.setTrackerColumn(column);
        midiPart.add(event);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        
        if(columnIndex != 0)
            return(true);
        else
            return(false);
    }

    public void fireSequenceDataChanged(EditHistoryAction[] edithistoryActions) {
    		
        if(edithistoryActions.length>0 && edithistoryActions[0] instanceof MovePartEditAction && ((MovePartEditAction)edithistoryActions[0]).getPart()==midiPart)
            this.setMidiPart((MidiPart)((MovePartEditAction)edithistoryActions[0]).getPart());
        else
            fireTableRowsUpdated(0,getRowCount());
    }
    
    /**
     * Clean up
     *
     */
    public void dispose()
    {
        midiPart.getEditHistoryContainer().removeEditHistoryListener(this);
    }
}
