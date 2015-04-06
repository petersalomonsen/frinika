/*
 * Created on Feb 8, 2007
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

import com.frinika.project.ProjectContainer;
import com.frinika.sequencer.gui.TimeFormat;
import com.frinika.sequencer.gui.TimeSelector;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

/**
 * Abstract superclass for menu-actions that modify currently selected MIDI 
 * data, using one single time-parameter. 
 * 
 * @author Jens Gulden
 */
abstract class AbstractTimeSelectorMidiActionEditor<T extends AbstractMidiAction> extends JPanel {
	
	protected T action;

	AbstractTimeSelectorMidiActionEditor(ProjectContainer project, T action) {
		this.action = action;
		this.setLayout(new GridBagLayout());
		final TimeSelector ts = new TimeSelector(getLabel(), getTicks(), true, project, TimeFormat.BEAT_TICK);
		ts.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				setTicks(ts.getTicks());
			}
		});
		this.add(ts, new GridBagConstraints());
	}
	
	abstract protected String getLabel();
	
	abstract protected long getTicks();
	
	abstract protected void setTicks(long ticks);
	
}
