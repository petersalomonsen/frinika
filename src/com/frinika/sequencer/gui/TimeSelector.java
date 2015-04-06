/*
 * Created on Jan 31, 2007
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

import com.frinika.sequencer.model.util.TimeUtils;
import com.frinika.project.ProjectContainer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * GUI-element for selecting a duration/amount of time. Depending on the format parameter
 * passed to the constructor, different input mechanisms are used: 
 * 
 * <p>
 * BAR_BEAT:      a text-field displaying and parsing a "<bar>.<beat>" string
 * </p>
 * <p>
 * BEAT_TICK:     a text-field displaying and parsing a "<beat>:<tick>" string
 * </p>
 * <p>
 * BAR_BEAT_TICK: a text-field displaying and parsing a "<bar>.<beat>:<tick>" string
 * </p>
 * <p>
 * BEAT:    a text-field displaying and parsing a numeric double-value representing a number of beats
 * </p>
 * <p>
 * NOTE_LENGTH:   either a drop-down-list or a scrollable multi-line select-box for selecting
 *                       lengths as beat-fractios, uas used with the specification of note lengths, e.g.
 *                       "1/4", "1/8", "1/16", "1/8 .", "1/4 trio" etc. Use te constructor parameter
 *                       multiLine to choose between drop-down-box and multi-line list-box.
 * 
 * @see com.frinika.sequencer.model.util.TimeUtils
 * @author Jens Gulden
 */
public class TimeSelector extends JPanel {
	
	// TODO only FORMAT_BAR_BEAT_TICK and FORMAT_NOTE_LENGTH are tested	
	
	public final static String[] NOTE_LENGTH_NAMES = new String[] // \u00b7 is middot
    { "2/1", "1/1 \u00b7",   "1/1",  "1/2 \u00b7",   "1/2",   "1/4 \u00b7",   "1/4", "1/8 \u00b7", "1/8", "1/16", "1/32", "1/64", "1/2 trio", "1/4 trio", "1/8 trio", "1/16 trio" };
	
	public final static double[] NOTE_LENGTH_FACTORS = new double[]
    { 2d/1d, 1.5*1d/1d, 1d/1d, 1.5*1d/2d, 1d/2d, 1.5*1d/4d, 1d/4d, 1.5*1d/8d, 1d/8d, 1d/16d, 1d/32d, 1d/64d, (1d/2d)*2d/3d, (1d/4d)*2d/3d, (1d/8d)*2d/3d, (1d/16d)*2d/3d };
	                                                          	
	private TimeFormat format;
	private JLabel label;
	private TickSpinner spinner;
	private JComboBox comboBox;
	private JList listBox;
	private boolean multiLine;
	private TimeUtils timeUtil;
	private ProjectContainer project;
	
	public TimeSelector(String label, long defaultTicks, boolean allowNegative, ProjectContainer project, TimeFormat format, boolean multiLine) {
		this.project = project;
		this.format = format;
		this.multiLine = multiLine;
	//	timeUtil = new TimeUtils(project);
		timeUtil = project.getTimeUtils();    // PJL

		GridBagConstraints gc = null;
		if (label != null) {
			this.setLayout(new GridBagLayout());
			gc = new GridBagConstraints();
			gc.insets.left = 2;
			gc.insets.right = 5;
		
			this.label = new JLabel(label);
			add(this.label, gc);
			gc.insets.left = 0;
			
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.weightx = 1f;
		} else {
			this.setLayout(new BorderLayout(0,0));
		}
		
		if (format == TimeFormat.NOTE_LENGTH) {
			if (multiLine) {
				listBox = new JList(NOTE_LENGTH_NAMES);
				listBox.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				add(new JScrollPane(listBox));
			} else {
				comboBox = new JComboBox(NOTE_LENGTH_NAMES);
				add(comboBox);
			}
			if (defaultTicks == 0) {
				defaultTicks = 128 / 2; // better default for note-lengths
			}
			setTicks(defaultTicks);
			
		} else { // normal time selector
			/*
			String s = formatString(defaultTicks);
			//textField = new JTextField(s, FORMAT_SIZES[format]);
			textField = new JTextField(s, FORMAT_SIZES[ findIndex(TimeFormat.values(), format) ]);
			if (label != null) {
				add(textField, gc);
			} else {
				add(textField);
			}
			// change notification via FocusListener, to remain compatible with original impl
			textField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					// delegate to action-listeners
					ActionListener l[] = textField.getActionListeners();
					ActionEvent ee = new ActionEvent(e.getSource(), e.getID(), null);
					for (int i = 0; i < l.length; i++) {
						l[i].actionPerformed(ee);
					}
				}
				
			});
			*/
			spinner = new TickSpinner(format, defaultTicks, allowNegative, timeUtil);
			if (label != null) {
				add(spinner, gc);
			} else {
				add(spinner);
			}
			/*spinner.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					// delegate to action-listeners
					ActionListener l[] = textField.getActionListeners();
					ActionEvent ee = new ActionEvent(e.getSource(), e.getID(), null);
					for (int i = 0; i < l.length; i++) {
						l[i].actionPerformed(ee);
					}
				}
				
			});*/
		}
	}

	public TimeSelector(String label, long defaultTicks, ProjectContainer project, TimeFormat format, boolean multiLine) {
		this(label, defaultTicks, false, project, format, multiLine);
	}

	public TimeSelector(String label, long defaultTicks, ProjectContainer project, TimeFormat format) {
		this(label, defaultTicks, project, format, false);
	}
	
	public TimeSelector(String label, long defaultTicks, boolean allowNegative, ProjectContainer project, TimeFormat format) {
		this(label, defaultTicks, allowNegative, project, format, false);
	}
	
	public TimeSelector(String label, String defaultStr, ProjectContainer project, TimeFormat format, boolean multiLine) {
		this(null, 0l, project, format, multiLine);
		setString(defaultStr);
	}

	public TimeSelector(String label, String defaultStr, ProjectContainer project, TimeFormat format) {
		this(null, 0l, project, format, false);
	}

	public TimeSelector(String defaultStr, ProjectContainer project, TimeFormat format, boolean multiLine) {
		this(null, defaultStr, project, format, multiLine);
	}

	public TimeSelector(String defaultStr, ProjectContainer project, TimeFormat format) {
		this(null, defaultStr, project, format, false);
	}

	public TimeSelector(long defaultTicks, ProjectContainer project, TimeFormat format, boolean multiLine) {
		this(null, defaultTicks, project, format,multiLine);
	}
	
	public TimeSelector(ProjectContainer project, TimeFormat format, boolean multiLine) {
		this(0l, project, format, multiLine);
	}
	
	public TimeSelector(long defaultTicks, ProjectContainer project, TimeFormat format) {
		this(null, defaultTicks, project, format, false);
	}
	
	public TimeSelector(long defaultTicks, boolean allowNegative, ProjectContainer project, TimeFormat format) {
		this(null, defaultTicks, allowNegative, project, format, false);
	}
	
	public TimeSelector(ProjectContainer project, TimeFormat format) {
		this(0l, project, format, false);
	}
	
	public TimeSelector(ProjectContainer project) {
		this(project, TimeFormat.BAR_BEAT_TICK);
	}
	
	/*public void addActionListener(final ActionListener a) {
		if (format == TimeFormat.NOTE_LENGTH) {
			if (multiLine) {
				listBox.addListSelectionListener(new ListSelectionListener() { // wrap ListSelectionEvent to ActionEvent
					public void valueChanged(ListSelectionEvent e) {
						ActionEvent ae = new ActionEvent(listBox, 0, null);
						a.actionPerformed(ae);
					}
				});
			} else {
				comboBox.addActionListener(a);
			}
		} else {
			textField.addActionListener(a);
		}
	}*/
	
	public void addChangeListener(final ChangeListener l) {
		if (format == TimeFormat.NOTE_LENGTH) {
			if (multiLine) {
				listBox.addListSelectionListener(new ListSelectionListener() { // wrap ListSelectionEvent to ActionEvent
					public void valueChanged(ListSelectionEvent e) {
						ChangeEvent ce = new ChangeEvent(listBox);
						l.stateChanged(ce);
					}
				});
			} else {
				//comboBox.addActionListener(l);
				comboBox.addActionListener(new ActionListener() { // wrap ListSelectionEvent to ActionEvent
					public void actionPerformed(ActionEvent e) {
						ChangeEvent ce = new ChangeEvent(comboBox);
						l.stateChanged(ce);
					}
				});
			}
		} else {
			//textField.addActionListener(a);
			spinner.addChangeListener(l);
		}
	}
	
	public synchronized void setTicks(long ticks) {
		if (format == TimeFormat.NOTE_LENGTH) {
			int i = findClosest(ticks, NOTE_LENGTH_FACTORS, project.getSequence().getResolution());
			if (multiLine) {
				listBox.setSelectedIndex(i);
				listBox.ensureIndexIsVisible(i);
			} else {
				comboBox.setSelectedIndex(i);
			}
		} else {
			//String s = formatString(ticks);
			//textField.setText(s);
			spinner.setValue(ticks);
		}
	}

	public long getTicks() {
		if (format == TimeFormat.NOTE_LENGTH) {
			int i;
			if (multiLine) {
				i = listBox.getSelectedIndex();
			} else {
				i = comboBox.getSelectedIndex();
			}
			double f = NOTE_LENGTH_FACTORS[i];
			long ticks = Math.round(f * project.getSequence().getResolution() * 4);
			return ticks;
		} else {
			String s = getString();
			long ticks = parseString(s);
			return ticks;
		}
	}
	
	public synchronized void setString(String s) {
		//if (format == TimeFormat.NOTE_LENGTH) {
			long ticks = parseString(s);
			setTicks(ticks);
		//} else {
		//	textField.setText(s);
		//}
	}
	
	public String getString() {
		if (format == TimeFormat.NOTE_LENGTH) {
			return comboBox.getSelectedItem().toString();
		} else {
			//return textField.getText();
			//return ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().getText();
			return ((TickSpinnerModel)spinner.getModel()).ticksToString( (Long)spinner.getValue() );
		}
	}
	
	public long parseString(String s) {
		return parseStringImpl(s, timeUtil, format, project);
	}
	
	public String formatString(long ticks) {
		return formatStringImpl(ticks, timeUtil, format);
	}
	
	private static long parseStringImpl(String s, TimeUtils timeUtil, TimeFormat format, ProjectContainer project) {
		int sgn = 1;
		s = s.trim();
		if (s.length() == 0) return 0;
		char sgnch = s.charAt(0); 
		if (sgnch=='-') {
			sgn = -1;
			s = s.substring(1);
		} else if (sgnch=='+') {
			s = s.substring(1);
		}
		switch (format) {
			case BAR_BEAT_TICK: return sgn * timeUtil.barBeatTickToTick(s);
			case BAR_BEAT: return sgn * timeUtil.barBeatTickToTick(s+":000");
			//case BEAT_TICK: return sgn * timeUtil.barBeatTickToTick("0."+s);
			case BEAT_TICK: return sgn * timeUtil.beatTickToTick(s);
			case BEAT: try { 
				return sgn * Math.round(Double.valueOf(s) * project.getSequence().getResolution());
			} catch (NumberFormatException nfe) { 
				return 0; 
			}
			default: return 0;
		}		
	}
	
	private static String formatStringImpl(long tick, TimeUtils timeUtil, TimeFormat format) {
		String sgn;
		if (tick < 0) {
			sgn = "-";
			tick = -tick;
		} else {
			sgn = "";
		}
		switch (format) {
			case BAR_BEAT_TICK: return sgn + timeUtil.tickToBarBeatTick(tick);
			case BAR_BEAT: return sgn + timeUtil.tickToBarBeat(tick);
			case BEAT_TICK: return sgn + timeUtil.tickToBeatTick(tick);
			case BEAT: return sgn + String.valueOf(timeUtil.tickToFloatBeat(tick));
			default: return "---";
		}
	}
	
	private static int findClosest(long ticks, double[] factors, int resolution) {
		long diff = Long.MAX_VALUE;
		int result = -1;
		for (int i = 0; i < factors.length; i++) {
			long t = Math.round(factors[i] * resolution * 4);
			if (t == ticks) return i; // optimiziation, will usually be the place ot exit here
			long d = ticks - t;
			if (d < 0) d = -d;
			if (d < diff) {
				diff = d;
				result = i;
			}
		}
		return result;
	}
	
	private static int findIndex(Object[] a, Object o) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] == o) {
				return i;
			}
		}
		return -1;
	}
}
