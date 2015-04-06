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

package com.frinika.gui;

/**
 * This interface is intended to be used in combination with JPanels that get set
 * as content of a dialog and contain user-editable GUI elements.
 * If a Dialog's content panel is an OptionsEditor, then refresh() will be called
 * every time the dialog is shown, and update() will be called if the user
 * has pressed OK. 
 *  
 * @author Jens Gulden
 */
public interface OptionsEditor {

	/**
	 * Refreshes the GUI so that it reflects the model's current state.
	 */
	public void refresh();
	
	/**
	 * Updates the model so that it contains the values set by the user
	 */
	public void update();
	
}
