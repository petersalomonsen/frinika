/*
 * Created on Jan 14, 2006
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import com.frinika.project.ProjectContainer;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.FrinikaSequence;
import com.frinika.sequencer.SongPositionListener;
import com.frinika.sequencer.SwingSongPositionListenerWrapper;

import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.midi.MidiMessageListener;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.Lane;
import com.frinika.sequencer.model.MidiLane;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.Part;
import com.frinika.sequencer.model.PitchBendEvent;
import com.frinika.sequencer.model.SysexEvent;
import com.frinika.tracker.DoubleCellRenderer;

import com.frinika.sequencer.gui.ItemRollToolBar;
import com.frinika.sequencer.gui.NoteLengthPopup;
import com.frinika.sequencer.gui.Snapable;

import com.frinika.sequencer.gui.virtualkeyboard.VirtualKeyboard;
import java.awt.Toolkit;
import java.awt.event.ItemListener;
import static com.frinika.localization.CurrentLocale.getMessage;
import java.util.EventObject;
import javax.swing.table.TableCellEditor;

/**
 * A midi editor for editing a midi part in an old-school Amiga Soundtracker/ProTracker fashion. For adapting to Midi there
 * are differences to the traditional tracker regarding handling of rows. First the row division is not locked - but can be changed at
 * any time - so that you can edit your notes using the optimum "rows per beat" resolution. In traditional trackers you were stuck to the
 * originally chosen row resolution. Also if your notes is between two rows - you can either increase the resolution - or you can use the 
 * time column which shows how much your note is in between the rows (-0.5 to 0.5 - before or after).
 *
 * All note column-sets, consist of row-time (before or after the row), actual note, velocity, and length of the note (number of rows). Notes
 * are inserted by pointing the cursor in the note column, and using the computer keyboard as a piano with two octaves. Lower octave starting
 * at Z making the twelve tone octave like this: ZSXDCVGBHNJM - and the upper octave: Q2W3ER5T6Y7U. 
 * 
 * To insert controllers you also use a note column, but hold down CTRL while typing a controller number. The velocity field is then used for
 * controller value. Pitch bend is CTRL-P in a note column and velocity of 0 is bend a note down, 64 is no bend and 127 one note up.  
 *  
 * @author Peter Johan Salomonsen
 */

public class TrackerPanel extends JPanel implements SelectionListener<Part>,SongPositionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ProjectFrame frame;
    ProjectContainer project;
	MidiPart part;
    int playingRow = 0;
    
	JTable table;

	TrackerTableModel tableModel;

	SelectionContainer<MultiEvent> multiEventSelectionContainer;

    private JScrollPane trackerScrollPane;

    private boolean followSong = true;
    private int automaticRowJump = 0;
    
    /**
     * Octave selection combobox
     */
    JComboBox octaveCombobox = new JComboBox(new Integer[] {1,2,3,4,5,6,7,8,9});
    {    
        octaveCombobox.setSelectedItem(VirtualKeyboard.Octave);
        octaveCombobox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                VirtualKeyboard.Octave = (Integer)octaveCombobox.getSelectedItem();
            }
        });
    }
    
	public TrackerPanel(FrinikaSequence sequence,ProjectFrame frame) {
		this.frame = frame;
		this.project = frame.getProjectContainer();
		this.multiEventSelectionContainer=project.getMultiEventSelection();
		initComponents();
        project.getSequencer().addSongPositionListener(new SwingSongPositionListenerWrapper(this));
	}
	
	private class MidiMessageRunnable implements Runnable
	{
		
		MidiMessage message;
		public void run()
		{
			midiMessage(message);
		}
		
		public void midiMessage(MidiMessage message) {
			if(listenlane.isRecording())
			{
				if(table.getSelectedRow() == -1 || table.getSelectedColumn() == -1) return;
				if(((table.getSelectedColumn() - 1)  % TrackerTableModel.COLUMNS) != TrackerTableModel.COLUMN_NOTEORCC) return; 
	
				
				if(message instanceof ShortMessage)
				{
					ShortMessage sms = (ShortMessage)message; 
					if(sms.getCommand() == ShortMessage.NOTE_ON || sms.getCommand() == ShortMessage.NOTE_OFF )
					{						
						int note = sms.getData1();
						int vel = sms.getData2();
						if(sms.getCommand() == ShortMessage.NOTE_OFF) vel = 0;						
						if(vel > 0)
						{
							int row = table.getSelectedRow();
							int col = tableModel.tableColumnToTrackerColumn(table.getSelectedColumn());
							
							part.getEditHistoryContainer().mark("new note");
							
							final MultiEvent me = tableModel.getCellEvent(row, col);
							if(me != null)
								part.remove(me);
							
							NoteEvent event = new NoteEvent(part,tableModel.getTickForRow(row),note,vel,1,(long)(tableModel.ticksPerRow*tableModel.getEditDuration()));
					        event.setTrackerColumn(col);
					        part.add(event);
							part.getEditHistoryContainer().notifyEditHistoryListeners();	 
							
							if(getAutomaticRowJump()>0 )
								table.changeSelection(table.getSelectedRow()+getAutomaticRowJump(),table.getSelectedColumn(),false,false);
						}						
					}
					
				}
			}
		}

	};
	
	MidiMessageListener listener = new MidiMessageListener()	
	{
		public void midiMessage(MidiMessage message) {
			MidiMessageRunnable r = new MidiMessageRunnable();
			r.message = message;
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	};

	
	MidiLane listenlane = null;
	void connectMidiListener()
	{
		if(listenlane != null) disconnectMidiListener();
		Lane lane = part.getLane();
		if(!(lane instanceof MidiLane)) return;
		listenlane = ((MidiLane)lane);
		project.getSequencer().addMidiMessageListener(listener);
		
	}
	
	void disconnectMidiListener()
	{
		if(listenlane == null) return;		
		project.getSequencer().removeMidiMessageListener(listener);
	}
    
	void initComponents() {
		setLayout(new BorderLayout());

		tableModel = new TrackerTableModel(frame);
		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

		    @Override
		    public boolean editCellAt(int row, int column, EventObject e) {
			boolean ret = super.editCellAt(row, column, e);
			TableCellEditor editor = getCellEditor();
			if(MultiEventCellEditor.class.isInstance(editor)) {			    
			    ((MultiEventCellComponent)((MultiEventCellEditor)editor).getComponent()).clearEventValue();
			}
			return ret;
		    }
			
			
			
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);			
								
				int firstcolsize = getColumnModel().getColumn(0).getWidth();
				for (int i = 0; i < getRowCount(); i++) {
					int y = getRowHeight()*i;
					g.setColor(Color.LIGHT_GRAY);
					g.drawLine(0,y-1,firstcolsize,y-1);
					if(i % tableModel.getTicksPerRow() == 0)
					{
						g.setColor(Color.BLACK);
						g.drawLine(0,y-1,firstcolsize,y-1);
						
						if(i % (tableModel.getTicksPerRow()*4) == 0)
							g.setColor(Color.BLACK);
						else
							g.setColor(Color.LIGHT_GRAY);
						g.drawLine(0,y-1,getWidth(),y-1);
					}		
				}
				
				g.setColor(Color.BLACK);
				
				int x = 0;
				for (int i = 0; i < getColumnCount(); i++) {
					if((i-1) % TrackerTableModel.COLUMNS == 0)				
					{
						g.drawLine(x-1,0,x-1, getHeight());						
					}
					x += getColumnModel().getColumn(i).getWidth();
				}												
								
				
			}
			
			
			
		    @Override
			public void repaint(long tm, int x, int y, int width, int height) {
		    	Rectangle v = getVisibleRect();
				x = v.x;
				width = getVisibleRect().width;
				super.repaint(tm, x, y, width, height);
			}

			@Override
			public void repaint(Rectangle r) {
				Rectangle v = getVisibleRect();
				r.x = v.x;
				r.width = v.width;
				super.repaint(r);
			}



			public boolean isColumnSelected(int column) {		    	
		    	if(column == 0) return false;
		    	
		    	int c = column - 1;		    	
		    	c = c - c % 4;
		    	
		    	if(columnModel.getSelectionModel().isSelectedIndex(c+1)) return true;
		    	if(columnModel.getSelectionModel().isSelectedIndex(c+2)) return true;
		    	if(columnModel.getSelectionModel().isSelectedIndex(c+3)) return true;
		    	if(columnModel.getSelectionModel().isSelectedIndex(c+4)) return true;
		    	
		    	return false;
		        //return columnModel.getSelectionModel().isSelectedIndex(column);
		    }
			
			JTable thistable = this;
			final DecimalFormat dec_format = new DecimalFormat("0.00");
			final DecimalFormat dec_format2 = new DecimalFormat("0.0");
			class TrackerTableCellRender extends DefaultTableCellRenderer
			{
				private static final long serialVersionUID = 1L;
				
				Color note_background = null;
				Color row_background = null;
				Color row_background2 = null;
				Font def_font = null;
				Font note_font = null;

				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					
					boolean hasFocusOrg = hasFocus;
					
					if(note_background == null)
					{
						float[] rgb = getGridColor().getRGBComponents(new float[4]);
						rgb[0] = 1f - (1f - rgb[0])*0.4f;
						rgb[1] = 1f - (1f - rgb[1])*0.4f;
						rgb[2] = 1f - (1f - rgb[2])*0.4f;						
						note_background = new Color(rgb[0], rgb[1], rgb[2], rgb[3]);						
						def_font = getFont();
						note_font = def_font.deriveFont(Font.BOLD);
					}
					if(row_background == null)
					{
						float[] rgb = table.getSelectionBackground().getRGBComponents(new float[4]);
						row_background = new Color(rgb[0], rgb[1], rgb[2], 0.2f);
					}
					if(row_background2 == null)
					{
						float[] rgb = table.getSelectionBackground().getRGBComponents(new float[4]);
						row_background2 = new Color(rgb[0], rgb[1], rgb[2], 0.35f);
					}				
					
					if(column == 0)
					{
						isSelected = false;
						hasFocus = false;
												
					}

					if(hasFocus)
					{
						isSelected = false;		
					}
					
					super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
							row, column);
					
					if(hasFocus)
					{
						isSelected = false;				
						hasFocus = false;
					}					
					
					if(!isSelected)
					if(!hasFocus)
					{									
						setBackground(thistable.getBackground());						
						if(column == 0) setBackground(getGridColor());							
						if((column - 1) % TrackerTableModel.COLUMNS == TrackerTableModel.COLUMN_NOTEORCC) setBackground(note_background);
					}					
										
					if((column - 1) % TrackerTableModel.COLUMNS == TrackerTableModel.COLUMN_NOTEORCC)
					{
						setFont(note_font);
						setHorizontalAlignment(SwingConstants.CENTER);
					}
					else
					{
						setFont(def_font);		
						setHorizontalAlignment(SwingConstants.RIGHT);
					}
					
					if(value instanceof Double)
					{
						setText(dec_format.format((Double)value));
					}
					if(column == 0)
					{						
						if(value.toString().length() != 0)
						setText(dec_format2.format(Double.parseDouble(value.toString())));
					}

					if(column != 0)
					if(!isSelected)
					if(!hasFocus)					
					if(row == getSelectionModel().getLeadSelectionIndex()) // getSelectedRow()
					{
						
						
						setOpaque(true);
						
						if(hasFocusOrg)
							setBackground(row_background2);
						else					
							setBackground(row_background);
						
					}

					return this;
				}
				
			}
			
			TrackerTableCellRender renderer = new TrackerTableCellRender();

			public TableCellRenderer getCellRenderer(int row, int column) {
				return renderer;
			}

		public Component prepareRenderer(TableCellRenderer renderer,
                    int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if(column == 0 && row == playingRow)
                    c.setBackground(Color.GREEN);
                else if(c.getBackground()!=null && c.getBackground().equals(Color.GREEN))
                        c.setBackground(null);
                return c;
            }
           
			/**
			 * Intercept addColumn so that correct widths and renderers/editors
			 * are applied on initialization
			 */
			@Override
			public void addColumn(TableColumn column) {
                if(getColumnCount()==0)
                {
                    // Set width of bar.beat column
                    column.setPreferredWidth(60);
                }
                else
                {
    				switch (((getColumnCount() - 1) % TrackerTableModel.COLUMNS)) {
    				case TrackerTableModel.COLUMN_TIME:
    					column.setPreferredWidth(40);
    					column.setCellRenderer(new DoubleCellRenderer());
    					break;
    				case TrackerTableModel.COLUMN_CHANNEL:
    					column.setPreferredWidth(30);
    					break;
    				case TrackerTableModel.COLUMN_NOTEORCC:
//    					column.setPreferredWidth(36);
    					column.setPreferredWidth(40);   // PJL increased to see CC93 etc
    					column.setCellEditor(new MultiEventCellEditor(
    							TrackerPanel.this));
    					break;
    				case TrackerTableModel.COLUMN_VELORVAL:
    					column.setPreferredWidth(30);
    					break;
    				case TrackerTableModel.COLUMN_LEN:
    					column.setPreferredWidth(40);
    					column.setCellRenderer(new DoubleCellRenderer());
    					break;
    				}
                }
				super.addColumn(column);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.JTable#processKeyBinding(javax.swing.KeyStroke,
			 *      java.awt.event.KeyEvent, int, boolean)
			 */
			protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
					int condition, boolean pressed) {
                                // Virtual keyboard octave selection
				if(e.getKeyCode()>=KeyEvent.VK_F1 && e.getKeyCode()<=KeyEvent.VK_F9)
                                {
                                    VirtualKeyboard.Octave = e.getKeyCode()-KeyEvent.VK_F1 +1;
                                    octaveCombobox.setSelectedItem(VirtualKeyboard.Octave);
                                    return true;
                                }    
                                else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && e.isShiftDown())
				{				
					if(getSelectedRow() != -1)
					if(getSelectedColumn() > 0)
					{
						if(!pressed) return false;
						
						if(getSelectedRow() != 0)
							setRowSelectionInterval(getSelectedRow()-1, getSelectedRow()-1);
						
						ArrayList<Integer> cols = new ArrayList<Integer>();
						for(int c : getSelectedColumns())
						{
							int col = tableModel.tableColumnToTrackerColumn(c);
							if(!cols.contains(col)) cols.add(col);
						}											
						
						part.getEditHistoryContainer().mark("move events");
						//int col = tableModel.tableColumnToTrackerColumn(table.getSelectedColumn());
						int min_row = getSelectedRow();
						int max_row = getRowCount();
						for (int col : cols)
						for (int i = min_row; i < max_row; i++) {
							MultiEvent me = tableModel.getCellEvent(i,col);
							if(me != null)
							{
								part.remove(me);
								if(i != min_row)
								{
									me.setTrackerColumn(col);							
									me.setStartTick(tableModel.getTickForRow(i-1));
									part.add(me);
								}
							}
						}
						part.getEditHistoryContainer().notifyEditHistoryListeners();
						return true;					
					}					
				}				
				
				if(e.getKeyCode() == KeyEvent.VK_INSERT)
				{				
					if(getSelectedRow() != -1)
					if(getSelectedColumn() > 0)
					{
						if(!pressed) return false;
						
						ArrayList<Integer> cols = new ArrayList<Integer>();
						for(int c : getSelectedColumns())
						{
							int col = tableModel.tableColumnToTrackerColumn(c);
							if(!cols.contains(col)) cols.add(col);
						}
						
						part.getEditHistoryContainer().mark("move events");
						
						int min_row = getSelectedRow();
						for (int col : cols)
						for (int i = getRowCount() - 1; i >= min_row; i--) {
							MultiEvent me = tableModel.getCellEvent(i,col);
							if(me != null)
							{
								part.remove(me);
								me.setTrackerColumn(col);							
								me.setStartTick(tableModel.getTickForRow(i+1));
								part.add(me);
							}
						}
						part.getEditHistoryContainer().notifyEditHistoryListeners();
						
						if(getSelectedRow() < (getRowCount() - 1))
							setRowSelectionInterval(getSelectedRow()+1, getSelectedRow()+1);
						
						return true;					
					}					
				}

				if(e.getKeyCode() == KeyEvent.VK_DELETE)
				{					
					if(getSelectedColumnCount() > 1) return false;
					if(getSelectedRowCount() > 1) return false;
					
					if(getSelectedRow() != -1)
					if(table.getSelectedColumn() > 0)
					//if(((table.getSelectedColumn() - 1)  % TrackerTableModel.COLUMNS) == TrackerTableModel.COLUMN_NOTEORCC) 
					{
						if(!pressed) return false;

				        int col = tableModel.tableColumnToTrackerColumn(table.getSelectedColumn());
				        final MultiEvent me = tableModel.getCellEvent(getSelectedRow(),col);
				        if(me!=null)
				        {
	                        if(me instanceof NoteEvent)
	                            part.getEditHistoryContainer().mark("delete note");
	                        else if(me instanceof ControllerEvent)
	                        	part.getEditHistoryContainer().mark("delete control change");
	                        else if(me instanceof PitchBendEvent)
	                        	part.getEditHistoryContainer().mark("delete pitch bend");
	                        else if(me instanceof SysexEvent) // Jens
	                        	part.getEditHistoryContainer().mark(getMessage("sequencer.sysex.delete_sysex"));
	                        else
	                        	part.getEditHistoryContainer().mark("delete event");
	                        part.remove(me);
	                        part.getEditHistoryContainer().notifyEditHistoryListeners();	                        
				        }
				        
						if(getAutomaticRowJump()>0 )
							table.changeSelection(table.getSelectedRow()+getAutomaticRowJump(),table.getSelectedColumn(),false,false);

						return true;
					}					
				}				
				
				if (isAccelerator(ks))
					return false;				

				/**
				 * Next similar column hotkeys feature Ctrl + Right / Ctrl + Left hotkeys 
				 * to move the cursor one entire time/note/vel/len column 
				 * right/left (i.e. to move 4 cols right/left; saves a few button hits) in tracker.
				 */
				else if(e.isControlDown() && ks.getKeyCode()==KeyEvent.VK_LEFT && pressed && condition == 1)
				{
					if(getSelectedColumn()-tableModel.COLUMNS>0)
						changeSelection(getSelectedRow(), getSelectedColumn()-tableModel.COLUMNS, false, false);	
					return false;
				}
				else if(e.isControlDown() && ks.getKeyCode()==KeyEvent.VK_RIGHT && pressed && condition == 1)
				{
					if(getSelectedColumn()+tableModel.COLUMNS<tableModel.getColumnCount())
						changeSelection(getSelectedRow(), getSelectedColumn()+tableModel.COLUMNS, false, false);
					return false;
				}
				// -----------------------------------------------------------------------------------
				else
					return super.processKeyBinding(ks, e, condition, pressed);
			}
			
			@Override
			public void tableChanged(TableModelEvent e) {
				if(e.getFirstRow()==TableModelEvent.HEADER_ROW)
				{
					int selectedCol = getSelectedColumn();
					int selectedRow = getSelectedRow();

					super.tableChanged(e);
					changeSelection(selectedRow, selectedCol, false, false);
					
				}
				else
					super.tableChanged(e);
			}
			
		};
						
		table.addFocusListener(new FocusListener()
				{
					public void focusGained(FocusEvent e) {
						connectMidiListener();
					}

					public void focusLost(FocusEvent e) {
						disconnectMidiListener();
					}
				});
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//table.setGridColor(Color.GRAY);
		table.setCellSelectionEnabled(true);
		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						if (!e.getValueIsAdjusting()) {
							updateSelection();
						}
					}
				});
		table.getColumnModel().getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						if (!e.getValueIsAdjusting()) {
							updateSelection();
						}
					}
				});
        
        trackerScrollPane = new JScrollPane(table);
		add(trackerScrollPane, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel(new FlowLayout());
		controlPanel.setOpaque(false);
                
                controlPanel.add(new JLabel("Octave"));
                // Virtual keyboard octave selection
                controlPanel.add(octaveCombobox);
		
                String[] automaticRowJumpTextField_values = {"0", "1", "2", "3", "4","8","16"};
		final JComboBox automaticRowJumpTextField = new JComboBox(automaticRowJumpTextField_values);
		automaticRowJumpTextField.setEditable(true);
		automaticRowJumpTextField.setSelectedItem("" + automaticRowJump);
		//automaticRowJumpTextField.setColumns(5);
		automaticRowJumpTextField.setToolTipText(getMessage("sequencer.tracker.automaticrowjump.tooltip"));
		
		automaticRowJumpTextField.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						try {
							automaticRowJump = Integer
									.parseInt(automaticRowJumpTextField.getSelectedItem().toString());

						} catch (Exception ex) {
							automaticRowJumpTextField.setSelectedItem(automaticRowJump
									+ "");
						}
					}		
				});
		/*
		automaticRowJumpTextField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				try {
					automaticRowJump = Integer
							.parseInt(automaticRowJumpTextField.getSelectedItem().toString());

				} catch (Exception ex) {
					automaticRowJumpTextField.setSelectedItem(automaticRowJump
							+ "");
				}
			}
		});*/
		controlPanel.add(new JLabel(
				getMessage("sequencer.tracker.automaticrowjump")));
		controlPanel.add(automaticRowJumpTextField);

		String[] editVelocityTextField_values = {"16", "32", "48", "64", "80", "100", "127"};
		final JComboBox editVelocityTextField = new JComboBox(editVelocityTextField_values);
		editVelocityTextField.setEditable(true);
		editVelocityTextField.setSelectedItem(""
				+ tableModel.getEditVelocity());
		editVelocityTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					tableModel.setEditVelocity(Integer
							.parseInt(editVelocityTextField.getSelectedItem().toString()));

				} catch (Exception ex) {
					editVelocityTextField.setSelectedItem(tableModel.getEditVelocity()
							+ "");
				}
			}
		});
		
		controlPanel.add(new JLabel(
				getMessage("sequencer.tracker.editvelocity")));
		controlPanel.add(editVelocityTextField);
		
		
		final DecimalFormat dec_format = new DecimalFormat("0.00");
		String[] editLenTextField_values = {
				dec_format.format(0.25),dec_format.format(0.5),
				dec_format.format(1),dec_format.format(2),dec_format.format(3),dec_format.format(4),
				dec_format.format(6),dec_format.format(8),
				dec_format.format(12),dec_format.format(16),
				dec_format.format(24),dec_format.format(32),
				};
		final JComboBox editLenTextField = new JComboBox(editLenTextField_values);
		editLenTextField.setEditable(true);
		editLenTextField.setSelectedItem(dec_format.format(tableModel.getEditDuration()));
		editLenTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					tableModel.setEditDuration(dec_format
							.parse(editLenTextField.getSelectedItem().toString()).doubleValue()); 

				} catch (Exception ex) {
					editLenTextField.setSelectedItem(dec_format.format(tableModel.getEditDuration())); 
				}
			}
		});
				
		controlPanel.add(new JLabel("Edit Length"));
		controlPanel.add(editLenTextField);
						
		String[] rowsPerBeatTextField_values = {"1", "2", "3", "4", "6", "8"};
		final JComboBox rowsPerBeatTextField = new JComboBox(rowsPerBeatTextField_values);
		rowsPerBeatTextField.setSelectedItem("" + tableModel.getTicksPerRow());		
		rowsPerBeatTextField.setEditable(true);
		rowsPerBeatTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					tableModel.setRowsPerBeat(Integer.parseInt(rowsPerBeatTextField
							.getSelectedItem().toString()));
					NoteLengthPopup.updateButton(quantizeSet, snapables, project.getSequence());
				} catch (Exception ex) {
					rowsPerBeatTextField.setSelectedItem(tableModel.getTicksPerRow() + "");
				}
			}
		});

		controlPanel.add(new JLabel(getMessage("sequencer.tracker.rowsperbeat")));
		controlPanel.add(rowsPerBeatTextField);

        JPanel settings = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        //settings.setBorder(BorderFactory.createEtchedBorder());
        final JToggleButton follow = ItemRollToolBar.makeFollowSongButton(null,settings);
        follow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                followSong = follow.isSelected();
                
            }});
        follow.setSelected(followSong);
        Insets insets = new Insets(0, 0, 0, 0);
        follow.setMargin(insets);       

        snapables = new Vector<Snapable>();
        snapables.add(new Snapable() {

            public double getSnapQuantization() {
       //     	System.out.println("TrackerPanel ticks per row=" + tableModel.getTicksPerRow());
                return (double)project.getSequence().getResolution() / (double)tableModel.getTicksPerRow();
            }

            public void setSnapQuantization(double quant) {
                int rowsPerBeat = (int)(project.getSequence().getResolution() / quant);
                if(rowsPerBeat <= 0) rowsPerBeat = 1;
                tableModel.setRowsPerBeat(rowsPerBeat);
                rowsPerBeatTextField.setSelectedItem(""+rowsPerBeat);        
            }});
        quantizeSet = ItemRollToolBar.makeSnapToButton(snapables,settings,project.getSequence());
        quantizeSet.setMargin(insets);

        controlPanel.add(settings);
        
        JToolBar toolbar = new JToolBar();
        toolbar.add(controlPanel);
		add(toolbar, BorderLayout.NORTH);
	}
	
    Vector<Snapable> snapables = null;
    JButton quantizeSet = null;		
	

    /**
     * Update the selection container according to the selection of rows/columns in the tracker.
     *
     */
	private void updateSelection() {
		multiEventSelectionContainer.clearSelection();
		multiEventSelectionContainer.setSelectionStartTick(tableModel
				.getTickForRow(table.getSelectedRow()));
		multiEventSelectionContainer.setSelectionLeftColumn(tableModel.tableColumnToTrackerColumn(table.getSelectedColumn()));
        					
		for (int row : table.getSelectedRows())			
		{
			
			int lasttrack = -1;
			for (int col : table.getSelectedColumns()) {
				
				if(col == 0) continue;
				int track = (col - 1) / TrackerTableModel.COLUMNS;
				if(lasttrack == track) continue;
				lasttrack = track;
				
				// if (((col - 1) % TrackerTableModel.COLUMNS) == TrackerTableModel.COLUMN_NOTEORCC) {
					MultiEvent selectedEvt = tableModel.getMultiEventAt(row,
							col);
					if (selectedEvt != null)
						multiEventSelectionContainer
								.addSelected(selectedEvt);
				//}
			}
		}
		multiEventSelectionContainer.notifyListeners(); // PJL
	}

	public void dispose() {
		tableModel.dispose();
        project.getSequencer().removeSongPositionListener(this);
	}

	public MidiPart getPart() {
		return part;
	}

	public void setPart(MidiPart part) {
		if(part!=this.part)
		{
			tableModel.setMidiPart(part);
			this.part = part;
		}
	}

	// TODO repair following stuff PJL

	public void partSelectionCleared() {
		// TODO Auto-generated method stub
		
	}

	public void partsRemovedFromSelection(Collection<Part> parts) {
		// TODO Auto-generated method stub
		
	}

	
	void setPartToFocus() {
		Part focus=project.getPartSelection().getFocus();
		if (focus instanceof MidiPart) 
			setPart((MidiPart) focus);
		else
			setPart(null);
	}
	
	

	public void selectionChanged(SelectionContainer<? extends Part> src) {
				
		setPartToFocus();

	}
    
    public void notifyTickPosition(long tick) {
        int lastPlayingRow = playingRow;
        
        playingRow = tableModel.getPlayingRow();
        if(playingRow!=lastPlayingRow)
        {   
            if(lastPlayingRow>=0 && lastPlayingRow<table.getRowCount())
                tableModel.fireTableCellUpdated(lastPlayingRow,0);
            if(playingRow>=0 && playingRow<tableModel.getRowCount())
                tableModel.fireTableCellUpdated(playingRow,0);
            
            //TODO: Autoscroll - should be configurable from gui
            if(followSong && playingRow>=0 && playingRow<tableModel.getRowCount())
            {
                Rectangle playRowRect = table.getCellRect(playingRow,0,true);
                if(!trackerScrollPane.getViewport().getViewRect().intersects(table.getCellRect(playingRow,0,true)))
                {
                    trackerScrollPane.getViewport().setViewPosition(new Point(playRowRect.x,playRowRect.y));
                    table.repaint();
                }
            }
        }        
    }

    public boolean requiresNotificationOnEachTick() {
        return false;
    }

	/**
	 * @return the table
	 */
	public JTable getTable() {
		return table;
	}

	/**
	 * 
	 * @return the trackerTableModel
	 */
	public TrackerTableModel getTableModel() {
		return tableModel;
	}
	
	/**
	 * Return how many rows to automatically jump when a note is hit (default is 0)
	 * @return
	 */
	public int getAutomaticRowJump() {
		return automaticRowJump;
	}

        /**
         * Keystrokes that should not be handled by this panel or the table
         * 
         * cut, copy, paste, undo, redo, save
         * @param ks
         * @return
         */
        private boolean isAccelerator(KeyStroke ks) {
		return (ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
                        || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
                        || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
                        || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
                        || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
                        || ks.equals(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()))
		);
	}

}
