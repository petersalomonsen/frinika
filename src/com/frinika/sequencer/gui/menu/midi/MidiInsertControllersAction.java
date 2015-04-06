/*
 * Created on Feb 10, 2007
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

package com.frinika.sequencer.gui.menu.midi;

import static com.frinika.localization.CurrentLocale.getMessage;

import com.frinika.gui.OptionsDialog;
import com.frinika.project.gui.ProjectFrame;
import com.frinika.sequencer.model.ControllerEvent;
import com.frinika.sequencer.model.PitchBendEvent;
import com.frinika.sequencer.model.NoteEvent;
import com.frinika.sequencer.model.MultiEvent;
import com.frinika.sequencer.model.MidiPart;
import com.frinika.sequencer.gui.TimeFormat;
import com.frinika.sequencer.gui.TimeSelector;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.util.*;

/**
 * Menu-action for inserting controller-events accordig to different,
 * selectable kinds of functions. In a special mode, also note-events
 * can be inserted, or velocity-values of existing notes can be changed.
 * 
 * @author Jens Gulden
 */
public class MidiInsertControllersAction extends AbstractMidiAction {
	
	protected Collection<ControllerFunction> availableFunctions;
	
	
	int controller = 7;
	long start;
	long length = 128 * 4 * 1;
	long resolution = 16;
	ControllerFunction function;
	
	public MidiInsertControllersAction(ProjectFrame frame) {
		super(frame, "sequencer.midi.insert_controllers");
		initControllerFunctions();
		function = availableFunctions.iterator().next(); // first one by default
	}
	
	private void initControllerFunctions() {
		availableFunctions = new ArrayList<ControllerFunction>();
		availableFunctions.add(new Linear());
		availableFunctions.add(new Triangle());
		availableFunctions.add(new Saw());
		availableFunctions.add(new Sine());
		availableFunctions.add(new Square());
	}
	
	public Collection<ControllerFunction> getAvailableControllerFunctions() {
		// (modeled as instance-method (non-static) for possibly later enhancements)
		return availableFunctions;
	}
	
	@Override
	public void performAction() {
		if (controller > 0) { // normal controller
			// clear all controllers of this kind in the given range
			removeAllControllers(getMidiPart(), controller, start, length);
			// insert new ones
			int lastVal = -1;
			for (long x = 0; x < length; x += resolution) {
				int val = function.value(x);
				if (controller != 64) {
					if (val < 0) val = 0; else if (val > 127) val = 127;
				}
				if (val != lastVal) {
					insertController(getMidiPart(), start + x, controller, val);
				}
				lastVal = val;
			}
		} else if (controller == 0) { // velocities (of existing notes)
			super.performAction(); // use default mechanism provided by superclass, will call modifyNoteEvent
		} else if (controller == -1) { // new notes
			for (long x = 0; x < length; x += resolution) {
				int val = function.value(x);
				if (val < 0) val = 0; else if (val > 127) val = 127;
				insertNote(getMidiPart(), start + x, val, (int)resolution - 1, 100);
			}
		}
	}
	
	@Override
	public void modifyNoteEvent(NoteEvent note) {
		// called via super.performAction() iff controller==0.
		long st = note.getStartTick();
		if ((st >= start) && (st < start + length)){
			long x = st - start;
			int val = function.value(x);
			if (val < 0) val = 0; else if (val > 127) val = 127;
			((NoteEvent)note).setVelocity(val);
		}
	}

	private void insertController(MidiPart part, long tick, int controller, int value) {
		if (controller == 64) { // pitch bend
			PitchBendEvent c = new PitchBendEvent(part, tick, value);
			part.add(c);
		} else {
			ControllerEvent c = new ControllerEvent(part, tick, controller, value);
			part.add(c);
		}
	}
	
	private void insertNote(MidiPart part, long tick, int note, int duration, int velocity) {
		NoteEvent n = new NoteEvent(part, tick, note, 100, part.getMidiChannel(), duration);
		part.add(n);
	}
	
	private void removeAllControllers(MidiPart part, int ctrl, long start, long length) {
		for (MultiEvent evt : new ArrayList<MultiEvent>(part.getMultiEvents()) ) {
			if (evt instanceof ControllerEvent) {
				long st = evt.getStartTick();
				if ((st >= start) && (st <= start + length)) {
					if (((ControllerEvent)evt).getControlNumber() == ctrl) {
						part.remove(evt);
					}
				}
			}
		}
	}

	@Override
	protected JComponent createGUI() {
		return new MidiInsertControllersActionEditor(frame, this);
	}
	
	@Override
	protected OptionsDialog createDialog() {
		OptionsDialog d = new OptionsDialog(frame, createGUI(), getMessage(actionId)) {
			@Override
			public void repack() {
				// nop
			}
		};
		d.pack();
		d.setSize( d.getWidth() + 250 , d.getHeight() + 50 );
		return d;
	}
	
		
	// --- inner classes -----------------------------------------------------
	
	
	public interface ControllerFunction {

		public String getName();
		public Icon getIcon(int width, int height);
		public JComponent createGUI();
		public int value(long tick);
		public int iconValue(int x, int width, int height);
		
	}
	
	
	public class ControllerFunctionIcon implements Icon {
		
		private ControllerFunction function;
		private int width;
		private int height;
		
		public ControllerFunctionIcon(ControllerFunction function, int width, int height) {
			this.function = function;
			this.width = width;
			this.height = height;
		}

		public int getIconHeight() {
			return width;
		}

		public int getIconWidth() {
			return height;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			int v = f(0);
			g.setColor(java.awt.Color.black);
			for (int i = 1; i < width; i++) {
				int newV = f(i);
				g.drawLine(x+i-1, y+height-v, x+i, y+height-newV);
				v = newV;
			}
		}
		
		private int f(int x) {
			return function.iconValue(x+height/8, width, height-height/4);
		}
		
	}

		
	public abstract class AbstractControllerFunction implements ControllerFunction {
		
		protected String name;
		
		protected AbstractControllerFunction() {
			// nop
		}
		
		protected AbstractControllerFunction(String name) {
			this();
			setName(name);
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
		public Icon getIcon(int width, int height) { 
			return new ControllerFunctionIcon(this, width, height);	
		}

	}
	
	
	abstract class AbstractCyclicControllerFunction extends AbstractControllerFunction {
		
		int min = 0;
		int max = 127;
		long phase = 128 * 4 * 2;
		long shift = 0;
		
		protected AbstractCyclicControllerFunction(String name) {
			super(name);
		}
		
		public JComponent createGUI() {
			//JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JPanel panel = new JPanel(new FlowLayout());
			panel.add(new JLabel("Between"));
			panel.add(spinner(min, 0, 127, new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					((SpinnerNumberModel)((JSpinner)e.getSource()).getModel()).setMaximum( (controller == 64) ? 16383 : 127); // hack to allow use of pitch bend, too
					min = (Integer)((JSpinner)e.getSource()).getValue();
				}
			}));
			panel.add(new JLabel("and"));
			panel.add(spinner(max, 0, 127, new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					((SpinnerNumberModel)((JSpinner)e.getSource()).getModel()).setMaximum( (controller == 64) ? 16383 : 127); // hack to allow use of pitch bend, too
					max = (Integer)((JSpinner)e.getSource()).getValue();
				}
			}));
			createGUIExtra(panel);
			return panel;
		}
		
		protected void createGUIExtra(JPanel panel) {
			panel.add(new JPanel()); // spacer
			panel.add(new JLabel("Interval"));
			final TimeSelector phaseTimeSelector = new TimeSelector(phase, frame.getProjectContainer(), TimeFormat.BAR_BEAT_TICK);
			phaseTimeSelector.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					phase = phaseTimeSelector.getTicks();
				}
			});
			panel.add(phaseTimeSelector);
			panel.add(new JLabel("Shift"));
			final TimeSelector shiftTimeSelector = new TimeSelector(shift, frame.getProjectContainer(), TimeFormat.BAR_BEAT_TICK);
			phaseTimeSelector.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					shift = shiftTimeSelector.getTicks();
				}
			});
			panel.add(shiftTimeSelector);
		}		

	}	
	
	
	class Linear extends AbstractCyclicControllerFunction {
		
		Linear() {
			super(getMessage("sequencer.midi.controllerfunction.linear"));
		}
		
		public int value(long x) {
			return min + Math.round(((max - min) * x) / (float)length);
		}

		public int iconValue(int x, int width, int height) {
			return (x * height) / width;
		}

		@Override
		public void createGUIExtra(JPanel panel) {
			// nop
		}
	}
	
	
	class Triangle extends AbstractCyclicControllerFunction {
		
		Triangle() {
			super(getMessage("sequencer.midi.controllerfunction.triangle"));
		}

		public int value(long x) {
			int ph = (int)((x + shift) % phase);
			int diff = max - min;
			int val = (int)(Math.round((max + diff - min) * ph / (float)phase)) + min;
			if (val > max) {
				int d = val - max;
				val = max - d;
			}
			return val;
		}

		public int iconValue(int x, int width, int height) {
			int y = (x * 4) % (height * 2);
			int d = y - height;
			if (d > 0) y = height - d;
			return y;
		}
	}

	
	class Saw extends AbstractCyclicControllerFunction {
		
		Saw() {
			super(getMessage("sequencer.midi.controllerfunction.saw"));
		}

		public int value(long x) {
			int ph = (int)((x + shift) % phase);
			int diff = max - min;
			int val = (int)Math.round(min + (diff * ph / (float)phase));
			return val;
		}

		public int iconValue(int x, int width, int height) {
			return (x*2) % height;
		}
	}

	
	class Sine extends AbstractCyclicControllerFunction {
		
		Sine() {
			super(getMessage("sequencer.midi.controllerfunction.sine"));
		}

		public int value(long x) {
			double amplitude = (max - min)/2.0;
			double center = min + amplitude;
			double sin = Math.sin( ( ((x + shift ) * 2.0 * Math.PI) / (double)phase ) + (1.5 * Math.PI) );
			int val = (int) Math.round( ( sin * amplitude ) + center );
			return val;
		}

		public int iconValue(int x, int width, int height) {
			int h = height / 2;
			int y = (int) Math.round( Math.sin( ( x / 3f ) + (1.75 * Math.PI) )  * h * 0.75 ) + h;
			return y;
		}
	}

	
	class Square extends AbstractCyclicControllerFunction {
		
		Square() {
			super(getMessage("sequencer.midi.controllerfunction.square"));
		}

		public int value(long x) {
			return min + (int)(((x + shift ) / phase) % 2) * (max - min);
		}

		public int iconValue(int x, int width, int height) {
			return ( ((x / 8) % 2) != 0  ? height-height/4 : height/4 );
		}
	}


	private static JSpinner spinner(int dflt, int min, int max, ChangeListener l) {
		JSpinner s = new JSpinner(new SpinnerNumberModel(dflt, min, max, 1));
		s.addChangeListener(l);
		return s;
	}
	
}
