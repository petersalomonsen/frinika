/*
 * Created on Mar 7, 2006
 *
 * Copyright (c) 2006 P.J.Leonard
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

package com.frinika.sequencer.gui;

import static com.frinika.sequencer.gui.menu.midi.MidiStepRecordAction.formatNote;
import static com.frinika.sequencer.gui.menu.midi.MidiStepRecordAction.parseNote;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.model.util.TimeUtils;

public class LabelFieldEditor extends JPanel {

	// changed to use TimeSelectors (JSpinners), NoteSelector (JComboBox) and JSpinners, Jens 2007-03-11
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//private JTextField textField;
	private JComponent editor;
	private JLabel label;
	AbstractTableModel model;
	
	private int column;
	private TimeUtils timeUtil;
	private String textBackup = null;
	
	public LabelFieldEditor(final MyAbstractTableModel model,final int column, ProjectContainer project) {
		timeUtil = new TimeUtils(project);
//System.out.println("COLUMN-CLASS: #"+column+" "+model.getColumnClass(column).getName());		
		setLayout(new FlowLayout(FlowLayout.CENTER,2,0));
		this.model=model;
		this.column=column;		
		add(label=new JLabel(model.getColumnName(column)));
		/*
		textField=new JTextField();	
		textField.setColumns(model.getColumnWidth(column));
		add(textField);
		textField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				model.setValueAt(textField.getText(), 0, column);			
			}
			
		});
		*/
		editor = createEditor(model, column, project);
		update();
		add(editor);
	}
	
	boolean isUpdateing = false;
	
	protected JComponent createEditor(final MyAbstractTableModel model, int columnIndex, ProjectContainer project) {
		// hard-code different types of editors by index-number... not too beautiful, but effective
		switch (column) {
			case 0: // start-tick
			case 3: // length
				final TimeSelector ts = new TimeSelector(project, (column==0) ? TimeFormat.BAR_BEAT_TICK : TimeFormat.BEAT_TICK);
				ts.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if(isUpdateing) return;
						Long ticks = ts.getTicks();
						if (ticks == null) return;
						model.setValueAt(timeUtil.tickToBarBeatTick(ticks), 0, column);			
					}
				});
				return ts;
				
			case 2: // velocity/ctrl-value
				final JSpinner spinner = new JSpinnerDraggable(new SpinnerNumberModel(0, 0, 127, 1));		
				spinner.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						if(isUpdateing) return;
						model.setValueAt(spinner.getValue().toString(), 0, column);			
					}
				});
				return spinner;
				
			case 1: // note
				final NoteSelector ns = new NoteSelector();
				ns.addItem("-"); // no. 128
				ns.setSelectedIndex(128);
				ns.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) {
						if(isUpdateing) return;
						int sel = ns.getSelectedIndex();
						if (sel <= 127) {
							model.setValueAt( formatNote(127 - sel), 0, column );			
						}
					}
				});				
				return ns;
			default: assert false; return null;
		}
	}
	
	public void update() {
		try
		{
			
		isUpdateing = true;
		Object o=model.getValueAt(0, column);
		if ((o == null) || (o.toString().equals("null"))) {
			JComponent e = editor;
			if (e instanceof TimeSelector) {
				e = (JComponent)e.getComponent(0);
			} 
			if (e instanceof JSpinner) {
				JTextField tf = ((JSpinner.DefaultEditor)((JSpinner)e).getEditor()).getTextField();
				String t = tf.getText();
				if ((t != null) && (t.length() > 0)) { // don't set backup twice if updated with null-value more than once
					textBackup = t;
				}
				tf.setText("");
			} else if (e instanceof NoteSelector) {
				((NoteSelector)e).setSelectedIndex(128);
			}
			//textField.setText("");
			editor.setEnabled(false);
			label.setText("N.A.");

		}
		else {
			long value = 0;
			switch (column) {
				case 0: // fallthrough
				case 3:
					value = timeUtil.barBeatTickToTick(o.toString());
					break;
				case 1:
					value = parseNote(o.toString());
					break;
				case 2:
					value = Integer.parseInt(o.toString());
					break;
			}
			if (editor instanceof TimeSelector) {
				JComponent e = (JComponent)editor.getComponent(0);
				restoreTextBackup((JSpinner)e);
				((JSpinner)e).setValue(value);   
			} else if (editor instanceof JSpinner) {
				restoreTextBackup((JSpinner)editor);
				((JSpinner)editor).setValue((int)value);   
				//JTextField tf = ((JSpinner.DefaultEditor)((JSpinner)editor).getEditor()).getTextField();
				//tf.setText("");
			} else if (editor instanceof NoteSelector) {
				int i = (127 - (int)value);
				if (((NoteSelector)editor).getSelectedIndex() != i) {
					((NoteSelector)editor).setSelectedIndex(i);
				}
			}
			editor.setEnabled(true);
			//textField.setText(o.toString());
			label.setText(model.getColumnName(column));
		}
		
		}
		finally
		{
			isUpdateing = false;
		}
	}

	private void restoreTextBackup(JSpinner e) {
		// restore string backup if textfield had been emptied before
		// this is necessary because new (numerical) values can only be set if valid strings are currently set
		if (textBackup != null) {
			JTextField tf = ((JSpinner.DefaultEditor)e.getEditor()).getTextField();
			String s = tf.getText();
			if ((s == null) || (s.length() == 0)) {
				tf.setText(textBackup);
			}
		}
	}
}
