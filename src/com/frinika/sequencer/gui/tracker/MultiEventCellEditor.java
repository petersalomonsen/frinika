/*
 * Created on Sep 18, 2004
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
package com.frinika.sequencer.gui.tracker;
import javax.swing.*;


/**
 * @author Peter Johan Salomonsen
 *
 */
public class MultiEventCellEditor extends DefaultCellEditor
{		
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MultiEventCellEditor(TrackerPanel trackerPanel)
	{
		super(new MultiEventCellComponent(trackerPanel));
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		try
		{
			return ((MultiEventCellComponent)this.getComponent()).getEventValue();
		}
		catch(Exception e)
		{
			return "";
		}
	}	
}
