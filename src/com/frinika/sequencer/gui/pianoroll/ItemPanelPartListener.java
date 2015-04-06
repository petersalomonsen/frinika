/*
 * Created on Mar 14, 2006
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

package com.frinika.sequencer.gui.pianoroll;


import com.frinika.sequencer.gui.ItemPanel;
import com.frinika.sequencer.gui.selection.SelectionContainer;
import com.frinika.sequencer.gui.selection.SelectionListener;
import com.frinika.sequencer.model.Part;

/**
 * Simple implementation that reconstructs all items.
 * 
 * @author pjl
 * 
 */

public class ItemPanelPartListener implements SelectionListener<Part> {

	ItemPanel itemPanel;

	public ItemPanelPartListener(ItemPanel pianoRoll) {
		this.itemPanel = pianoRoll;
	}


	public void selectionChanged(SelectionContainer<? extends Part> src) {
		// TODO Auto-generated method stub
		itemPanel.repaintItems();	
	}
}
