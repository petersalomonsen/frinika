/*
 * Created on Mar 9, 2007
 *
 * Copyright (c) 2007 Jens Gulden
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

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import com.frinika.sequencer.model.util.TimeUtils;

/**
 * JSpinner extended to display and edit time-values of the form "bar.beat:tick".
 * 
 * @author Jens Gulden
 */
public class TickSpinner extends JSpinnerDraggable implements CaretListener {

	public TickSpinner() {
		super();
		init();
	}

	public TickSpinner(TickSpinnerModel model) {
		super(model);
		init();
	}

	public TickSpinner(TimeFormat format, long value, TimeUtils timeUtils) {
		this(format, value, false, timeUtils);
	}

	public TickSpinner(TimeFormat format, long value, boolean allowNegative, TimeUtils timeUtils) {
		this(new TickSpinnerModel(format, value, allowNegative, timeUtils));
	}

	public TickSpinner(TimeFormat format, TimeUtils timeUtils) {
		this(format, 0, timeUtils);
	}
	
	public Object getNextValue() {
		return super.getNextValue();
	}
	
	private void init() {
		JFormattedTextField ftf = (JFormattedTextField)((JSpinner.DefaultEditor)this.getEditor()).getTextField(); // ! depends on swing implementation to actually use FormattedTextField
		ftf.setColumns(((TickSpinnerModel)getModel()).format.textFieldSize);
		ftf.setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
			@Override
			public AbstractFormatter getFormatter(JFormattedTextField tf) {
				return new JFormattedTextField.AbstractFormatter() {
					@Override
					public Object stringToValue(String text) throws ParseException {
						return ((TickSpinnerModel)getModel()).stringToTicks(text);
					}
					@Override
					public String valueToString(Object value) throws ParseException {
						return ((TickSpinnerModel)getModel()).ticksToString((Long)value);
					}
				};
			}
		});
		ftf.addCaretListener(this);
	}
	
	@Override
	public void commitEdit() throws ParseException {
		final JTextField textField = ((JSpinner.DefaultEditor)this.getEditor()).getTextField();
		final int pos = textField.getCaretPosition();
		super.commitEdit();
		(new Thread() {
			public void run() {
				try {
					//Thread.sleep(50); // 50 ms ok?
					textField.setCaretPosition(pos); // reset old caret position after all event handling is done
				} catch (IllegalArgumentException ie) {
					ie.printStackTrace();
				}
			}
		}).start();
	}
	
	public void caretUpdate(CaretEvent e) {
		// depending on the position in the formatted string, set different step sizes so that up/down arrows will modify the part in which the cursor is
		TickSpinnerModel model = (TickSpinnerModel)getModel();
		JTextField textField = ((JSpinner.DefaultEditor)this.getEditor()).getTextField();
		int pos = textField.getCaretPosition();
		//int stepSize = stepSizeForPosition(model.getFormat(), textField.getText(), pos);
		model.updateStepSize(textField.getText(), pos);
		//model.setStepSize(stepSize);
	}

	/*protected JComponent createEditor(SpinnerModel model) {
		return super.createEditor(model);
	}*/
	
	/*@Override
	public void commitEdit() throws ParseException {
		super.commitEdit();
	}*/
	
}
