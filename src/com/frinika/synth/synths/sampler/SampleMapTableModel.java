/*
 * Created on Dec 15, 2004
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
 *
 * Frinika is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frinika; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.frinika.synth.synths.sampler;

import javax.swing.table.AbstractTableModel;

import com.frinika.sequencer.gui.virtualkeyboard.VirtualKeyboard;
import com.frinika.synth.synths.MySampler;
import com.frinika.synth.synths.sampler.settings.SampledSoundSettings;

/**
 * Table model for sample map
 * @author Peter Johan Salomonsen
 *
 */
public class SampleMapTableModel extends AbstractTableModel {
	MySampler sampler;
	public SampleMapTableModel(MySampler sampler)
	{
		this.sampler = sampler;
	}
	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return 96;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 128;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0)
		{
			return(VirtualKeyboard.getNoteString(96-rowIndex));
		}
		else
		{
			try
			{
				return(sampler.sampledSounds[96-rowIndex][128-columnIndex]);
			} catch(NullPointerException e)	{
				return(null);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		sampler.insertSample((SampledSoundSettings)aValue,96-rowIndex,128-columnIndex);
		super.setValueAt(aValue, rowIndex, columnIndex);
	}
}
